package com.basic.net

import java.io.Serializable

/**
 * @author Peter Liu
 * @since 2023/3/21 00:03
 *
 */

data class ApiResponse<T> @JvmOverloads constructor(
    val result: T?,
    val code: String = "",
    val msg: String = "",
    val ext: Map<String, Any> = emptyMap()
) : Serializable {

    fun isSuccess(): Boolean {
        return code == HttpException.SUCCESS || code == "200"
    }
}

fun <T> ApiResponse<T>?.isSuccess(): Boolean {
    return this != null && this.isSuccess()
}

fun <T> ApiResponse<T>?.onData(onSuccess: (T?) -> Unit): ApiResponse<T>? {
    if (this.isSuccess()) {
        onSuccess.invoke(this!!.result)
    }
    return this
}

fun <T> ApiResponse<T>?.onFailed(
    showToast: Boolean = false,
    onFailed: ((ApiResponse<T>?) -> Unit)? = null
): ApiResponse<T>? {
    if (!this.isSuccess()) {
        if(showToast){
            //todo show toast
        }
        onFailed?.invoke(this)
    }
    return this
}