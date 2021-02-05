package com.hanna2100.cleannote.business.data.util

import com.hanna2100.cleannote.business.data.cache.CacheErrors.CACHE_ERROR_TIMEOUT
import com.hanna2100.cleannote.business.data.cache.CacheErrors.CACHE_ERROR_UNKNOWN
import com.hanna2100.cleannote.business.data.cache.CacheResult
import com.hanna2100.cleannote.business.data.network.ApiResult
import com.hanna2100.cleannote.business.data.network.NetworkConstants.NETWORK_TIMEOUT
import com.hanna2100.cleannote.business.data.network.NetworkErrors.NETWORK_ERROR_TIMEOUT
import com.hanna2100.cleannote.business.data.network.NetworkErrors.NETWORK_ERROR_UNKNOWN
import com.hanna2100.cleannote.business.data.util.GenericError.ERROR_UNKNOWN
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import retrofit2.HttpException
import java.io.IOException
import java.lang.Exception

/**
 * Reference: https://medium.com/@douglas.iacovelli/how-to-handle-errors-with-retrofit-and-coroutines-33e7492a912
 */

suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    apiCall: suspend () -> T?
): ApiResult<T?> {
    return withContext(dispatcher) {
        try {
            // TimeoutCancellationException 던짐
            withTimeout(NETWORK_TIMEOUT) {
                ApiResult.Success(apiCall.invoke())
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            when (throwable) {
                is TimeoutCancellationException -> {
                    val code = 408 // timeout 에러코드
                    ApiResult.GenericError(code, NETWORK_ERROR_TIMEOUT)
                }
                is IOException -> {
                    ApiResult.NetworkError
                }
                is HttpException -> {
                    val code = throwable.code()
                    val errorResponse: String? = convertErrorBody(throwable)
                    ApiResult.GenericError(
                        code,
                        errorResponse
                    )
                }
                else -> {
                    ApiResult.GenericError(
                        null,
                        NETWORK_ERROR_UNKNOWN
                    )
                }
            }
        }
    }
}

suspend fun <T> safeCacheCall(
    dispatcher: CoroutineDispatcher,
    cacheCall: suspend () -> T?
): CacheResult<T?> {
    return withContext(dispatcher) {
        try {
            // TimeoutCancellationException 던짐
            withTimeout(NETWORK_TIMEOUT) {
                CacheResult.Success(cacheCall.invoke())
            }
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
            when (throwable) {
                is TimeoutCancellationException -> {
                    CacheResult.GenericError(CACHE_ERROR_TIMEOUT)
                }
                else -> {
                    CacheResult.GenericError(CACHE_ERROR_UNKNOWN)
                }
            }
        }
    }
}

fun convertErrorBody(throwable: HttpException): String? {
    return try {
        throwable.response()?.errorBody()?.string()
    } catch (e: Exception) {
        ERROR_UNKNOWN
    }
}
