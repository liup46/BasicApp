package com.basic.ui.vproperty

import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.basic.ui.LifecycleInit
import kotlin.properties.ReadOnlyProperty
import kotlin.properties.ReadWriteProperty
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

typealias Func0 = () -> Unit
typealias Func1<T> = (t: T?) -> Unit
typealias Func2<V, T> = V?.(t: T?) -> Unit

interface Getter<V> {
    fun get(): V?
}

interface Observable<V> {
    fun observer(observer: Func1<V>)
}

interface Setter<V> : Getter<V?>, Func1<V> {
    fun set(data: V?)
}

interface ObserverSetter<V> : Getter<V?>, Func1<V>, Observable<V> {
    fun set(data: V?)
}

class DefaultSetter<V>(var value: V? = null) : Setter<V> {
    override fun set(data: V?) {
        value = data
    }

    override fun get(): V? {
        return value
    }

    override fun invoke(p1: V?) {
        set(data = p1)
    }
}

class DefaultObserverSetter<V>(var value: V? = null, private val observer: Func1<V>? = null) :
    ObserverSetter<V> {
    override fun set(data: V?) {
        value = data
        observer?.invoke(data)
    }

    override fun get(): V? {
        return value
    }

    override fun observer(observer: Func1<V>) {
        throw IllegalStateException("Unsupported method.")
    }

    override fun invoke(p1: V?) {
        set(data = p1)
    }
}

internal class DefaultObserverSetterProperty<T, V>(
    var default: V?= null,
    inline var observer: Func2<T, V>? = null
) : ReadWriteProperty<T, ObserverSetter<V>> {

    var observerSetter: ObserverSetter<V>? = null

    override fun getValue(thisRef: T, property: KProperty<*>): ObserverSetter<V> {
        if (observerSetter == null) {
            observerSetter = DefaultObserverSetter(default) {
                observer?.invoke(thisRef, it)
            }
        }
        return observerSetter!!
    }


    override fun setValue(thisRef: T, property: KProperty<*>, value: ObserverSetter<V>) {
        observer?.invoke(thisRef, value.get())
    }
}

class LiveDataSetter<V>(var lifecycleOwner: LifecycleOwner, var liveData: MutableLiveData<V>) :
    ObserverSetter<V> {
    private var observered = false

    override fun set(data: V?) {
        if (Looper.getMainLooper().isCurrentThread) {
            liveData.value = data
        } else {
            liveData.postValue(data)
        }
    }

    override fun get(): V? {
        return liveData.value
    }

    override fun invoke(p1: V?) {
        set(p1)
    }

    override fun observer(observer: Func1<V>) {
        if (!observered) {
            observered = true
            liveData.observe(lifecycleOwner, observer)
        }
    }
}

internal class LiveDataSetterProperty<V>(var default: V?) :
    ReadOnlyProperty<LifecycleInit, ObserverSetter<V>> {

    var value: ObserverSetter<V>? = null

    override fun getValue(thisRef: LifecycleInit, property: KProperty<*>): ObserverSetter<V> {
        if (value == null) {
            val livedata = thisRef.getViewModel().getLiveData(property.name, default)
            value = LiveDataSetter(thisRef.getLifecycleOwner(), livedata!!)
        }
        return value!!
    }
}