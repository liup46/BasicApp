package com.basic.ui

import androidx.lifecycle.MutableLiveData
import com.basic.net.ApiResponse

typealias Executable<CALL> = () -> CALL

interface Executor<in Call : Any> {
    fun <T> executeForResult(call: Call, apiLiveData: MutableLiveData<ApiResponse<T>>)
    fun <T> executeForData(call: Call, apiLiveData: MutableLiveData<T>)
}

//Todo need implement
object ExecutorManager {
    val futureExecutorMap: HashMap<Class<*>, Executor<*>?> = hashMapOf()

    inline fun <reified CALL : Any> register(reqExe: Executor<CALL>) {
        futureExecutorMap.put(CALL::class.java, reqExe)
    }

    fun <CALL : Any> register(clz: Class<CALL>, reqExe: Executor<CALL>) {
        futureExecutorMap.put(clz, reqExe)
    }

    fun <T> executeForResult(call: Any, apiLiveData: MutableLiveData<ApiResponse<T>>) {
        val execute = findExecutor(call::class.java) as? Executor<Any>
        execute?.executeForResult(call, apiLiveData)
    }

    fun <T> executeForData(call: Any, apiLiveData: MutableLiveData<T>) {
        val execute = findExecutor(call::class.java) as? Executor<Any>
        execute?.executeForData(call, apiLiveData)
    }

    private fun findExecutor(clazz: Class<*>): Executor<*>? {
        var clz = clazz
        while (futureExecutorMap[clz] == null) {
            clz = clz.superclass
        }
        return futureExecutorMap[clz]
    }
}

//
// class RequestLauncher<CALL, T>(var request: RequestFun<CALL>) : MutableLiveData<T>() {
//     var errorHandler: ((ApiResult<T>?) -> Unit)? = null
//
//     fun successData(): LiveData<T> {
//         return Transformations.map(this) {
//             if (it != null && it.success) {
//                 it.result
//             } else {
//                 null
//             }
//         }
//     }
//
//     override fun setValue(value: T?) {
//         super.setValue(value)
//         if (this.value?.success != true) {
//             errorHandler?.invoke(value)
//         }
//     }
//
//     fun onFail(errorHandler: (T?) -> Unit): RequstLauncher<T> {
//         this.errorHandler = errorHandler
//         if (this.value != null) {
//             errorHandler.invoke(this.value)
//         }
//         return this
//     }
// }

