package com.quare.bibleplanner.core.profile.domain.usecase

import com.quare.bibleplanner.core.profile.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

fun interface ObserveUserProfile {
    operator fun invoke(): Flow<UserProfile?>
}
