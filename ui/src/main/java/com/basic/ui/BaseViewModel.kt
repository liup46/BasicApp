package com.basic.ui

import androidx.annotation.NonNull
import androidx.lifecycle.*
import com.basic.net.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

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

    fun <V> liveData(default: V? = null) = liveData<BaseViewModel, V>(this, default)

    override fun onCleared() {
        super.onCleared()
        isCleared = true
        liveDataMap.clear()
    }
}

fun <T> ApiResponse<T>?.mapLiveData(mutableLiveData: MutableLiveData<ApiResponse<T>>): MutableLiveData<ApiResponse<T>> {
    if (this != null) {
        mutableLiveData.value = this
    }
    return mutableLiveData
}

inline fun <T> BaseViewModel.request(
    requestTag: String,
    crossinline requestCall: RequestCall<T>
): MutableLiveData<ApiResponse<T>> {
    return request(getLiveData(requestTag)!!, requestCall)
}

inline fun <T> BaseViewModel.request(
    liveData: MutableLiveData<ApiResponse<T>>,
    crossinline requestCall: RequestCall<T>,
): MutableLiveData<ApiResponse<T>> {
    if (isCleared) {
        return liveData
    }
    viewModelScope.launch {
        request {
            requestCall.invoke()
        }.mapLiveData(liveData)
    }
    return liveData
}




