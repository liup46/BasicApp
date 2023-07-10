package com.basic.ui.vproperty

import android.os.Looper
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.basic.ui.LifecycleInit
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * 基于LiveData实现的IObserverSetter，用于set(value), or .(value)
 * LiveDataSetterProperty用于在LiveDataSetterProperty 中定义属性
 *
 * @author Peter Liu
 * @since 2023/7/11 01:37
 *
 */

class LiveDataSetter<V>(var lifecycleOwner: LifecycleOwner, var liveData: MutableLiveData<V>) :
    IObserverSetter<V> {
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

fun <V> liveState(default: V? = null): LiveDataSetterProperty<V> {
    return LiveDataSetterProperty(default)
}

class LiveDataSetterProperty<V>(var default: V?) :
    ReadOnlyProperty<LifecycleInit, IObserverSetter<V>> {

    var value: IObserverSetter<V>? = null

    override fun getValue(thisRef: LifecycleInit, property: KProperty<*>): IObserverSetter<V> {
        if (value == null) {
            val livedata = thisRef.getViewModel().getLiveData(property.name, default)
            value = LiveDataSetter(thisRef.getLifecycleOwner(), livedata!!)
        }
        return value!!
    }
}