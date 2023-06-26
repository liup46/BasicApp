package com.basic.ui.vproperty

import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.basic.ui.LifecycleInit
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * 实现类似Mobx的功能，使用方式
 * TestActivity{
 *    var textView1:TextView?
 *    var textView2:TextView?
 *
 *    val testState1 by liveState("1")
 *    val testState2 by liveState<String>()
 *
 *    fun initView(){
 *      textView1.vText = testState1
 *      textView2.vText = testState2
 *
 *    }
 *
 *    //after http request end or click event , then set state value
 *    fun action(){
 *      testState1.set("2sss")
 *      testState2.set("2")
 *    }
 *
 *
 * @author Peter Liu
 * @since 2023/6/27 01:34
 *
 */

interface Getter<E> {
    fun get(observer: (E?) -> Unit)
}

interface Setter<E> : Getter<E> {
    fun set(data: E?)
}

class DefaultSetter<E>(var observer: (E?) -> Unit) : Setter<E> {
    override fun get(observer: (E?) -> Unit) {
    }

    override fun set(data: E?) {
        observer.invoke(data)
    }
}

class LiveDataSetter<E>(var lifecycleOwner: LifecycleOwner, var liveData: MutableLiveData<E>) :
    Setter<E> {
    private var observered = false

    override fun set(data: E?) {
        if (Looper.getMainLooper().isCurrentThread) {
            liveData.value = data
        } else {
            liveData.postValue(data)
        }
    }

    override fun get(observer: (E?) -> Unit) {
        if (!observered) {
            observered = true
            liveData.observe(lifecycleOwner, observer)
        }
    }
}

internal class LiveDataSetterProperty<V>(var default: V?) :
    ReadOnlyProperty<LifecycleInit, Setter<V>> {

    var value: Setter<V>? = null

    override fun getValue(thisRef: LifecycleInit, property: KProperty<*>): Setter<V> {
        if (value == null) {
            val livedata = thisRef.getViewModel().getLiveData(property.name, default)
            value = LiveDataSetter(thisRef.getLifecycleOwner(), livedata!!)
        }
        return value!!
    }
}