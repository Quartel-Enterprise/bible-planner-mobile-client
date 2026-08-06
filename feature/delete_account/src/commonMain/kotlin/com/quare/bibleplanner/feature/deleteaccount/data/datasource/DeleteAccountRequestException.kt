package com.quare.bibleplanner.feature.deleteaccount.data.datasource

class DeleteAccountRequestException(
    statusCode: Int,
) : IllegalStateException("The delete-account function responded with status $statusCode")
