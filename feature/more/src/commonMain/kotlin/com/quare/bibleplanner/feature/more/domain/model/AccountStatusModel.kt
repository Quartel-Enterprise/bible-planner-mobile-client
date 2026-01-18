package com.quare.bibleplanner.feature.more.domain.model

import com.quare.bibleplanner.core.user.domain.model.UserModel

sealed interface AccountStatusModel {
    data object Loading : AccountStatusModel

    data object LoggedOut : AccountStatusModel

    data object Error : AccountStatusModel

    data class LoggedIn(
        val user: UserModel,
    ) : AccountStatusModel
}
