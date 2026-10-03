package com.quare.bibleplanner.core.user.domain.usecase

import com.quare.bibleplanner.core.user.domain.model.UserModel
import kotlinx.coroutines.flow.Flow

fun interface ObserveCurrentUser {
    operator fun invoke(): Flow<UserModel?>
}
