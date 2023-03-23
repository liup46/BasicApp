package com.basic.net

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

/**
 * @author Peter Liu
 * @since 2023/3/23 15:49
 *
 */

inline fun <T> CoroutineScope.safeRequest(
    requestCall: () -> ApiResponse<T>?,
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

