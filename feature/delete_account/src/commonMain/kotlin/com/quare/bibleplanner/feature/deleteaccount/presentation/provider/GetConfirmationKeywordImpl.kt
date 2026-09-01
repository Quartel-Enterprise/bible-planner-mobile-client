package com.quare.bibleplanner.feature.deleteaccount.presentation.provider

import bibleplanner.feature.delete_account.generated.resources.Res
import bibleplanner.feature.delete_account.generated.resources.delete_account_confirmation_keyword
import org.jetbrains.compose.resources.getString

internal class GetConfirmationKeywordImpl : GetConfirmationKeyword {
    override suspend fun invoke(): String = getString(Res.string.delete_account_confirmation_keyword)
}
