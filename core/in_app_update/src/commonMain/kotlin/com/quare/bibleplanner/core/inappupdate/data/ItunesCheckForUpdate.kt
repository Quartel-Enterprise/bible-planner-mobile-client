package com.quare.bibleplanner.core.inappupdate.data

import com.quare.bibleplanner.core.inappupdate.data.dto.ItunesLookupResponseDto
import com.quare.bibleplanner.core.inappupdate.domain.model.UpdateAvailability
import com.quare.bibleplanner.core.inappupdate.domain.usecase.CheckForUpdate
import com.quare.bibleplanner.core.inappupdate.generated.InAppUpdateBuildKonfig
import com.quare.bibleplanner.core.network.data.handler.RequestHandler
import com.quare.bibleplanner.core.utils.version.VersionComparator
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlin.random.Random

internal class ItunesCheckForUpdate(
    private val requestHandler: RequestHandler,
    private val deviceRegionProvider: DeviceRegionProvider,
) : CheckForUpdate {
    override suspend fun invoke(): UpdateAvailability {
        val storeVersion = findStoreVersion().getOrElse { return UpdateAvailability.CheckFailed }
            ?: return UpdateAvailability.NotAvailable
        return if (VersionComparator.compare(storeVersion, InAppUpdateBuildKonfig.APP_VERSION) > 0) {
            UpdateAvailability.Available(versionName = storeVersion)
        } else {
            UpdateAvailability.NotAvailable
        }
    }

    private suspend fun findStoreVersion(): Result<String?> {
        val regionCode = deviceRegionProvider.getRegionCode() ?: return fetchStoreVersion(country = null)
        val regionalVersion = fetchStoreVersion(country = regionCode)
        /*
         * Why: the device region can differ from the App Store account's storefront, so an app the
         * regional store doesn't list is looked up again in the default storefront.
         */
        val isMissingFromRegionalStore = regionalVersion.isSuccess && regionalVersion.getOrNull() == null
        return if (isMissingFromRegionalStore) fetchStoreVersion(country = null) else regionalVersion
    }

    private suspend fun fetchStoreVersion(country: String?): Result<String?> = requestHandler
        .call<ItunesLookupResponseDto> {
            get(LOOKUP_URL) {
                parameter(
                    key = COUNTRY_PARAMETER,
                    value = country,
                )
                /*
                 * Why: Apple's CDN keeps serving the previous version of a repeated lookup for hours
                 * after a release, and its max-age keeps it in the device URL cache for a day; a
                 * unique query skips both.
                 */
                parameter(
                    key = CACHE_BUSTER_PARAMETER,
                    value = Random.nextLong(),
                )
            }
        }.map { response -> response.results?.firstOrNull()?.version }

    private companion object {
        const val LOOKUP_URL = "https://itunes.apple.com/lookup?id=6756151777"
        const val COUNTRY_PARAMETER = "country"
        const val CACHE_BUSTER_PARAMETER = "t"
    }
}
