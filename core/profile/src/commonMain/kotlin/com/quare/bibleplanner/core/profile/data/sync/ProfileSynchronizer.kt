package com.quare.bibleplanner.core.profile.data.sync

import co.touchlab.kermit.Logger
import com.quare.bibleplanner.core.provider.connectivity.NetworkConnectivityObserver
import com.quare.bibleplanner.core.provider.room.dao.ProfileDao
import com.quare.bibleplanner.core.provider.room.entity.ProfileEntity
import com.quare.bibleplanner.core.sync.domain.Synchronizer
import com.quare.bibleplanner.core.user.domain.usecase.GetAuthenticatedUserId
import com.quare.bibleplanner.core.utils.suspendRunCatching
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

internal class ProfileSynchronizer(
    private val delegate: Synchronizer,
    private val profileDao: ProfileDao,
    private val avatarRemoteStore: AvatarRemoteStore,
    private val networkConnectivityObserver: NetworkConnectivityObserver,
    private val getAuthenticatedUserId: GetAuthenticatedUserId,
) : Synchronizer by delegate {
    private val logger = Logger.withTag(LOG_TAG)
    private val initialBackoff: Duration = 2.seconds
    private val maxBackoff: Duration = 60.seconds

    override suspend fun runPushLoop() = coroutineScope {
        launch { runAvatarUploadLoop() }
        delegate.runPushLoop()
    }

    override suspend fun pushPendingOnce() {
        profileDao.getPending().firstPendingAvatar()?.let { entity ->
            suspendRunCatching { upload(entity) }
        }
        delegate.pushPendingOnce()
    }

    private suspend fun runAvatarUploadLoop() {
        combine(
            profileDao.getPendingFlow().map { it.firstPendingAvatar() },
            networkConnectivityObserver.observe(),
        ) { pendingAvatar, isOnline -> pendingAvatar.takeIf { isOnline } }
            .distinctUntilChangedBy { entity -> entity?.id to entity?.updatedAt }
            .collectLatest { entity ->
                if (entity != null) uploadWithRetry(entity)
            }
    }

    private suspend fun uploadWithRetry(entity: ProfileEntity) {
        var backoff = initialBackoff
        repeat(MAX_UPLOAD_ATTEMPTS) { attempt ->
            suspendRunCatching { upload(entity) }
                .onSuccess { return }
                .onFailure { error ->
                    if (attempt == MAX_UPLOAD_ATTEMPTS - 1) {
                        logger.e(error) { "Giving up on the avatar upload after $MAX_UPLOAD_ATTEMPTS attempts" }
                        return
                    }
                    logger.e(error) { "Failed to upload the avatar; retrying in $backoff" }
                    delay(backoff)
                    backoff = (backoff * BACKOFF_FACTOR).coerceAtMost(maxBackoff)
                }
        }
    }

    private suspend fun upload(entity: ProfileEntity) {
        val userId = getAuthenticatedUserId() ?: return
        if (entity.id != userId) return
        val url = avatarRemoteStore.upload(
            userId = userId,
            bytes = requireNotNull(entity.pendingAvatarBytes),
        )
        profileDao.onAvatarUploaded(
            id = userId,
            avatarUrl = "$url?v=${entity.updatedAt}",
            pendingUpdatedAt = entity.updatedAt,
        )
    }

    private fun List<ProfileEntity>.firstPendingAvatar(): ProfileEntity? = firstOrNull { it.pendingAvatarBytes != null }

    private companion object {
        const val LOG_TAG = "ProfileSync"
        const val BACKOFF_FACTOR = 2
        const val MAX_UPLOAD_ATTEMPTS = 5
    }
}
