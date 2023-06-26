package com.basic.ui

import androidx.annotation.NonNull
import androidx.lifecycle.*
import com.basic.net.ApiResponse
import com.basic.ui.view.StateListener

/**
 * @author Peter Liu
 * @since 2022/11/11 00:46
 *
 */

inline fun <reified T : ViewModel> getViewModel(@NonNull owner: ViewModelStoreOwner): T {
    return ViewModelProvider(owner).get(T::class.java)
}

open class BaseViewModel : ViewModel() {
    var isCleared = false
        private set

    private val liveDataMap = hashMapOf<String, MutableLiveData<*>>()

    /**
     * 根据某个标识tag 创建一个 MutableLiveData
     */
    @Suppress("UNCHECKED_CAST")
    fun <T> getLiveData(
        tag: String,
        default: T? = null,
        defaultFactory: ((default: T?) -> MutableLiveData<T>)? = {
            if (default == null) MutableLiveData<T>() else MutableLiveData<T>(default)
        }
    ): MutableLiveData<T>? {
        val liveData = liveDataMap[tag]
        return if (liveData == null && defaultFactory != null) {
            synchronized(this) {
                var data = liveDataMap[tag]
                if (data == null) {
                    data = defaultFactory.invoke(default)
                    liveDataMap[tag] = data
                    data
                } else {
                    data as? MutableLiveData<T>
                }
            }
        } else {
            liveData as? MutableLiveData<T>
        }
    }

    inline fun <reified T> launchResult(
        tag: String,
        crossinline call: Executable<*>
    ): LiveData<ApiResponse<T?>> {
        val liveData = getLiveData<ApiResponse<T?>>(tag)!!
        if (isCleared) {
            return liveData
        }
        return liveData.launchResult(call)
    }

    inline fun <reified T> launchData(
        tag: String,
        crossinline call: Executable<*>
    ): LiveData<T?> {
        val liveData = getLiveData<T?>(tag)!!
        if (isCleared) {
            return liveData
        }
        return liveData.launchData(call)
    }

    inline fun <reified T> launchResult(
        lifecycleOwner: LifecycleOwner,
        requestTag: String,
        consumer: StateListener<T>,
        crossinline call: Executable<*>,
    ) {
        if (isCleared) {
            return
        }

        val liveData =
            getLiveData<ApiResponse<T>>(
                requestTag,
                defaultFactory = { OnceLiveData() }) as OnceLiveData<ApiResponse<T>>
        if (!liveData.hasObservers()) {
            liveData.observe(lifecycleOwner, consumer)
        }
        liveData.launchResult(call)
    }

    inline fun <reified T> launchData(
        lifecycleOwner: LifecycleOwner,
        requestTag: String,
        noinline consumer: OnceObserver<T>,
        crossinline call: Executable<*>,
    ) {
        if (isCleared) {
            return
        }
        val liveData =
            getLiveData<T>(requestTag, defaultFactory = { OnceLiveData() })!! as OnceLiveData<T>
        if (!liveData.hasObservers()) {
            liveData.observe(lifecycleOwner, consumer)
        }
        liveData.launchData(call)
    }

    override fun onCleared() {
        super.onCleared()
        isCleared = true
        liveDataMap.clear()
    }
}





