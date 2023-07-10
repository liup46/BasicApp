package com.basic.net

import kotlinx.coroutines.*

/**
 * @author Peter Liu
 * @since 2023/3/23 15:49
 *
 */

typealias RequestCall<T> = suspend () -> ApiResponse<T>?

suspend inline fun <T> CoroutineScope.request(
    crossinline requestCall: RequestCall<T>,
): ApiResponse<T>? {
    return try {
        requestCall()
    } catch (e: Exception) {
        return if (e is HttpException) {
            ApiResponse(null, e.code, e.msg)
        } else {
            ApiResponse(null, HttpException.ERROR_CODE_UNKNOW, HttpException.ERROR_MSG_UNKNOWN)
        }
    }
}

