package com.basic.ui.view

import com.basic.net.ApiResponse
import com.basic.ui.OnceObserver

/**
 * @author Peter Liu
 * @since 2023/6/19 23:47
 *
 */
interface IStateView {
    fun showError(code: String? = null, msg: String? = null)
    fun showLoading(show: Boolean = true)
    fun showEmpty(msg: String? = null)

    fun showEmpty(result: ApiResponse<*>) {
        showEmpty(result.msg)
    }

    fun showError(result: ApiResponse<*>) {
        showError(result.code, result.msg)
    }
}

fun interface ViewUpater<T> {
    fun showData(data: T)
}

typealias OnceObserver<T> = (T?) -> Unit


interface StateListener<T> : OnceObserver<ApiResponse<T>>, IStateView, ViewUpater<T> {
    override fun invoke(result: ApiResponse<T>?) {
        this.showLoading(false)
        if (result == null) {
            this.showError()
        } else if (result.isSuccess()) {
            if (result.isDataEmpty()) {
                this.showEmpty(result)
            } else {
                showData(result.result!!)
            }
        } else {
            this.showError(result)
        }
    }
}