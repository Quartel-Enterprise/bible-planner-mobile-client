package com.quare.bibleplanner.feature.profile.domain.model

import com.quare.bibleplanner.core.profile.domain.model.UserProfile

sealed interface AccountStatusModel {
    data object Loading : AccountStatusModel

    data object LoggedOut : AccountStatusModel

    data object Error : AccountStatusModel

    data class LoggedIn(
        val profile: UserProfile,
    ) : AccountStatusModel
}
