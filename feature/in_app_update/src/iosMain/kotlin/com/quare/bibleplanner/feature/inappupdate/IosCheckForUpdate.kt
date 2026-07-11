package com.quare.bibleplanner.feature.inappupdate

import com.quare.bibleplanner.core.network.data.handler.RequestHandler
import com.quare.bibleplanner.core.utils.version.VersionComparator
import com.quare.bibleplanner.feature.inappupdate.data.dto.ItunesLookupResponseDto
import com.quare.bibleplanner.feature.inappupdate.domain.model.UpdateAvailability
import com.quare.bibleplanner.feature.inappupdate.domain.usecase.CheckForUpdate
import com.quare.bibleplanner.feature.inappupdate.generated.InAppUpdateBuildKonfig
import io.ktor.client.request.get

internal class IosCheckForUpdate(
    private val requestHandler: RequestHandler,
) : CheckForUpdate {
    override suspend fun invoke(): UpdateAvailability {
        val response = requestHandler.call<ItunesLookupResponseDto> {
            get(LOOKUP_URL)
        }
        val storeVersion = response
            .getOrNull()
            ?.results
            ?.firstOrNull()
            ?.version
            ?: return UpdateAvailability.NotAvailable
        return if (VersionComparator.compare(storeVersion, InAppUpdateBuildKonfig.APP_VERSION) > 0) {
            UpdateAvailability.Available(versionName = storeVersion)
        } else {
            UpdateAvailability.NotAvailable
        }
    }

    private companion object {
        const val LOOKUP_URL = "https://itunes.apple.com/lookup?id=6756151777"
    }
}
