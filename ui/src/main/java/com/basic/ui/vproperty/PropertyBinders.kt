package com.basic.ui.vproperty

import android.graphics.drawable.Drawable
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import com.basic.ui.BaseViewModel
import com.basic.ui.ViewContainer
import com.basic.ui.view.setVisible
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


/**
 * 暂时保留这部分代码，后续大概率会删除，优先使用[VProperties]中的api。
 *
 * @author Peter Liu
 * @since 2022/11/3 00:52
 *
 * Binder Api: Width, Height, Text, TextColor, Background, Foreground, Padding, Margin,
 */

typealias ViewUpdater<V, T> = V.(t: T?) -> Unit

fun <V : View, T> V.bind(
    liveData: LiveData<T>,
    lifecycleOwner: LifecycleOwner? = null,
    updater: ViewUpdater<V, T?>? = null
): V {
    if (lifecycleOwner != null) {
        liveData.observe(lifecycleOwner) {
            updater?.invoke(this, it)
        }
    } else {
        updater?.invoke(this, liveData.value)
    }
    return this
}

fun <T> ViewContainer.bind(
    data: LiveData<T>,
    lifecycleOwner: LifecycleOwner?,
    updater: ViewUpdater<View, T>? = null
) {
    getRootView()?.bind(data, lifecycleOwner, updater)
}

/***********************Customize binders***********************/

fun <V : View> V.bindVisible(
    liveData: LiveData<Boolean>,
    lifecycleOwner: LifecycleOwner?,
    action: (V.() -> Unit)? = null
): V {
    return this.bind(liveData, lifecycleOwner) {
        this.setVisible(it == true, action)
    }
}


fun <V : TextView> V.bindText(liveData: LiveData<String>, lifecycleOwner: LifecycleOwner?): V {
    return this.bind(liveData, lifecycleOwner) { text = it }
}

fun <V : TextView> V.bindTextColor(liveData: LiveData<Int>, lifecycleOwner: LifecycleOwner?): V {
    return this.bind(liveData, lifecycleOwner) { if (it != null) setTextColor(it) }
}

fun <V : View> V.bindBackground(liveData: LiveData<Drawable>, lifecycleOwner: LifecycleOwner?): V {
    return this.bind(liveData, lifecycleOwner) { background = it }
}

data class Edge(
    val left: Int? = null,
    val top: Int? = null,
    val right: Int? = null,
    val bottom: Int? = null
) {
    constructor(leftRight: Int? = null, topBottom: Int? = null) :this(leftRight, topBottom, leftRight, topBottom)
    constructor(all:Int):this(all, all,all,all)

}


fun <V : View> V.bindPadding(liveData: LiveData<Edge>, lifecycleOwner: LifecycleOwner?): V {
    return this.bind(liveData, lifecycleOwner) {
        if (it != null) {
            setPadding(
                it.left ?: this.paddingLeft,
                it.top ?: this.paddingTop,
                it.right ?: this.paddingRight,
                it.bottom ?: this.paddingBottom
            )
        }
    }
}

fun <V : View> V.bindMargin(liveData: LiveData<Edge>, lifecycleOwner: LifecycleOwner?): V {
    return this.bind(liveData, lifecycleOwner) {
        val param = this.layoutParams
        if (it != null && param is ViewGroup.MarginLayoutParams) {
            param.setMargins(
                it.left ?: this.marginLeft,
                it.top ?: this.marginTop,
                it.right ?: this.marginRight,
                it.bottom ?: this.marginBottom
            )
            this.requestLayout()
        }
    }
}

class ObserverProperty<T, V>(var getter: T.() -> V, var observer: T.(V?) -> Unit) :
    ReadWriteProperty<T, V?> {
    var name: String? = null
    private var isFirst: Boolean = true

    override fun getValue(thisRef: T, property: KProperty<*>): V? {
        setName(property.name)
        return getter(thisRef)
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V?) {
        setName(property.name)
        if (isFirst && value != null) {
            this.isFirst = false
        }
        if (!isFirst) {
            observer(thisRef, value)
        }
    }

    private fun setName(name: String) {
        if (this.name == null) {
            this.name = name
        }
    }
}


open class LiveDataProperty<T, V>(var viewModel: BaseViewModel, var default: V? = null) :
    ReadWriteProperty<T, V?> {
    override fun getValue(thisRef: T, property: KProperty<*>): V? {
        return viewModel.getLiveData(property.name, default)!!.value
    }

    override fun setValue(thisRef: T, property: KProperty<*>, value: V?) {
        val livedata = viewModel.getLiveData(property.name, default)!!
        if (Looper.getMainLooper().isCurrentThread) {
            livedata.value = value
        } else {
            livedata.postValue(value)
        }
    }
}
