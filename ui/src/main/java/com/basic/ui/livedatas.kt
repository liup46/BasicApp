package com.basic.ui

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.basic.net.ApiResponse
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * @author Peter Liu
 * @since 2022/11/24 00:42
 *
 */

inline fun <reified CALL : Any, T> MutableLiveData<ApiResponse<T>>.launchResult(
    crossinline request: Executable<CALL?>
): LiveData<ApiResponse<T>> {
    return apply {
        val call = request.invoke()
        if (call != null) {
            ExecutorManager.executeForResult(call, this)
        }
    }
}

inline fun <reified CALL : Any, T> MutableLiveData<T>.launchData(
    crossinline request: Executable<CALL?>
): LiveData<T> {
    return apply {
        val call = request.invoke()
        if (call != null) {
            ExecutorManager.executeForData(call, this)
        }
    }
}

typealias OnceObserver<T> = (T?) -> Unit

class OnceLiveData<T> : MutableLiveData<T>() {
    var consumer: OnceObserver<T>? = null

    @Volatile
    var hasConsumer = false
    var observer: Observer<T>? = null

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner, observer)
    }

    fun observe(owner: LifecycleOwner, consumer: OnceObserver<T>) {
        this.consumer = consumer
        if (!hasConsumer) {
            hasConsumer = true
            this.observer = Observer {
                this.consumer?.invoke(it)
            }
            observe(owner, this.observer!!)
        }
    }

    override fun removeObserver(observer: Observer<in T>) {
        super.removeObserver(observer)
        if (observer == this.observer) {
            this.observer = null
            this.consumer = null
            hasConsumer = false
        }
    }
}
