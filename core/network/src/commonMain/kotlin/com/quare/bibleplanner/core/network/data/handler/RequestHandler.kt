package com.quare.bibleplanner.core.network.data.handler

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class RequestHandler(
    val httpClient: HttpClient,
) {
    val tag = this::class.simpleName.orEmpty()

    suspend inline fun <reified T> call(
        noinline onError: ((errorCode: Int) -> Throwable?)? = null,
        crossinline block: suspend HttpClient.() -> HttpResponse,
    ): Result<T> = withContext(Dispatchers.IO) {
        try {
            val response = httpClient.block()
            if (response.status.isSuccess()) {
                val body = response.body<T>()
                Result.success(body)
            } else {
                val errorMessage = "Unexpected status code: ${response.status}"
                Logger.e(
                    tag = tag,
                    messageString = errorMessage,
                )
                Result.failure(
                    exception = onError
                        ?.invoke(response.status.value)
                        ?: Exception(errorMessage),
                )
            }
        } catch (exception: Exception) {
            Logger.e(
                tag = tag,
                throwable = exception,
                messageString = exception.message.orEmpty(),
            )
            Result.failure(exception)
        }
    }
}
