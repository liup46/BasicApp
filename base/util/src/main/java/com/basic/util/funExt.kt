package com.basic.util

/**
 * @author Peter Liu
 * @since 2023/3/23 22:37
 *
 */

inline fun <T:Any> safeCall(default: T? = null, crossinline call: () -> T?): T? {
    return try {
        call.invoke()
    } catch (e: Exception) {
        return default
    }
}

