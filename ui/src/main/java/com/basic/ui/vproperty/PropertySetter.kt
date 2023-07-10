package com.basic.ui.vproperty

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * 实现类似Mobx的功能，使用方式 参考[TestUiModeActivity]
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

interface Setter<V> : Func1<V> {
    fun set(data: V?)
}

interface IObserverSetter<V> : Func1<V>, Observable<V>,Getter<V?> {
    fun set(data: V?)
}

class DefaultSetter<V>(var value: V? = null) : Setter<V>, Getter<V?> {
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

/**
 * 用来作为定义属性
 */
internal class ObserverSetter<V>(
    private var value: V? = null,
    private val observer: Func1<V>? = null
) : IObserverSetter<V> {
    override fun set(data: V?) {
        if (value != data) {
            value = data
            observer?.invoke(data)
        }
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

/**
 * PropertyObserverSetter的属性委托类
 */
internal class ObserverSetterDelegate<T, V>(
    private var default: V? = null,
    private val notifyRightNow: Boolean = false,
    inline var observer: Func2<T, V>? = null,
) : ReadWriteProperty<T, IObserverSetter<V>> {

    private var observerSetter: IObserverSetter<V>? = null

    override fun getValue(thisRef: T, property: KProperty<*>): IObserverSetter<V> {
        if (observerSetter == null) {
            observerSetter = ObserverSetter(default) {
                observer?.invoke(thisRef, it)
            }
        }
        return observerSetter!!
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: IObserverSetter<V>) {
        if (observer != null) {
            value.observer { observer }
        }
        if (notifyRightNow) {
            //现在的内部实现是livdata 故不用手动通知更新notifyRightNow = false
            observer?.invoke(thisRef, value.get())
        }
    }
}