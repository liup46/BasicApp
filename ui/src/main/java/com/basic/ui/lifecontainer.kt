package com.basic.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import com.basic.ui.view.StateView
import com.basic.ui.view.Toolbar
import com.basic.ui.view.setVisible
import com.basic.ui.vproperty.*

/**
 * @author: Peter Liu
 * @date: 2022/11/9
 *
 */
internal interface LifecycleInit : ViewContainer {
    val layoutId: Int?
    var toolbar: Toolbar?
    var stateView: StateView?

    val defaultViewModel: BaseViewModel

    fun getViewModel(): BaseViewModel {
        return defaultViewModel
    }

    fun getLifecycleOwner(): LifecycleOwner

    /**
     * 初始化intent Extra或者fragment arguments
     */
    fun initExtra(bundle: Bundle) {}

    /**
     * 初始化ToolBar
     */
    fun initToolbar(toolbar: Toolbar) {}

    /**
     * 初始化view
     */
    fun initView() {}

    /**
     * 初始化ViewModel
     */
    fun initData() {}

    fun needToolbar(): Boolean {
        return false
    }

    fun needStateView(): Boolean {
        return false
    }

    fun initRootContent(inflater: LayoutInflater, parent: ViewGroup?): View? {
        val showToolbar = needToolbar()
        val showState = needStateView()
        if (showToolbar || showState) {
            val rootContent = inflater.inflate(R.layout.base_content, parent, false) as ViewGroup
            if (showToolbar) {
                toolbar = rootContent.findViewById<Toolbar>(R.id.toolbar)?.apply {
                    setVisible(true)
                }
            }
            if (showState) {
                stateView = rootContent.findViewById<StateView>(R.id.stateView)?.apply {
                    setVisible(true)
                }
            }
            return initLayout(inflater, rootContent, true)
        } else {
            return initLayout(inflater, parent)
        }
    }

    fun initLayout(inflater: LayoutInflater, parent: ViewGroup?, attach: Boolean = false): View? {
        if (layoutId != null && layoutId!! > 0) {
            val baseContent = parent?.findViewById<FrameLayout>(R.id.baseContent)
            return inflater.inflate(layoutId!!, baseContent, attach)
        }
        return parent
    }

    fun <V> liveState(default: V? = null): LiveDataSetterProperty<V> {
        return LiveDataSetterProperty(default)
    }

}

interface ViewContainer {
    fun getRootView(): View?
}

/**** 参数柯里化 *****/
inline fun <T> ViewContainer.ui(crossinline fuc:Func2<View,T>):Func1<T>{
    return {
        fuc(getRootView(),it)
    }
}

inline fun ViewContainer.ui(crossinline fuc:Func1<View>):Func0{
    return {
        fuc(getRootView())
    }
}

fun <T> Func1<T>.bind(observerSetter: ObserverSetter<T>){
    observerSetter.observer(this)
}

