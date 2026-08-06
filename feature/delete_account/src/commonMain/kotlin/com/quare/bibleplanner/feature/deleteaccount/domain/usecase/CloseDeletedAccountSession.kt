package com.quare.bibleplanner.feature.deleteaccount.domain.usecase

fun interface CloseDeletedAccountSession {
    suspend operator fun invoke(): Result<Unit>
}
