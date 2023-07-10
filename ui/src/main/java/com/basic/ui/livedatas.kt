package com.basic.ui

import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.basic.net.ApiResponse
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 支持 livedata = value
 *
 * Useage:
 * TestViewModel{
 *  var livepros:String by LiveDataProperty(this)
 *  livepros = "11"
 *
 *  fun test(){
 *      print(livepros)
 * }
 *
 *
 * @author Peter Liu
 * @since 2022/11/24 00:42
 *
 */

fun <T, V> liveData(viewModel: BaseViewModel, default: V? = null): LiveDataProperty<T, V?> {
    return LiveDataProperty(viewModel, default)
}
open class LiveDataProperty<T, V>(var viewModel: BaseViewModel, var default: V? = null) :
    ReadWriteProperty<T, V?> {
    @Volatile
    private var liveData: MutableLiveData<V>? = null
    override fun getValue(thisRef: T, property: KProperty<*>): V? {
        return getLiveData(property.name, default).value
    }

    private fun getLiveData(name: String, default: V?): MutableLiveData<V> {
        if (liveData == null) {
            liveData = viewModel.getLiveData(name, default)
        }
        return liveData!!
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V?) {
        val livedata = getLiveData(property.name, default)
        if (Looper.getMainLooper().isCurrentThread) {
            livedata.value = value
        } else {
            livedata.postValue(value)
        }
    }

    fun getLiveData(): MutableLiveData<V>? {
        return liveData
    }
}

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
