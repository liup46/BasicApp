package com.basic.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import com.basic.net.ApiResponse
import com.basic.net.RequestCall
import com.basic.ui.view.StateView
import com.basic.ui.view.Toolbar
import com.basic.ui.view.setVisible
import com.basic.ui.vproperty.Func0
import com.basic.ui.vproperty.Func1
import com.basic.ui.vproperty.Func2
import com.basic.ui.vproperty.IObserverSetter

/**
 * @author: Peter Liu
 * @date: 2022/11/9
 *
 */
interface LifecycleInit : ViewContainer {
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

     fun <T> Func1<ApiResponse<T>>.onResult(tag: String, requestCall: RequestCall<T>) {
        request(tag, requestCall).onResult {
            this.invoke(it)
        }
    }

    fun <T> Func1<T>.onData(tag: String, requestCall: RequestCall<T>) {
        request(tag, requestCall).onData {
            this.invoke(it)
        }
    }

    fun <T> MutableLiveData<T>.onResult(ui: Func1<T>) {
        if (this.hasObservers()) {
            return
        }
        this.observe(getLifecycleOwner()) {
            ui.invoke(it)
        }
    }

    fun <T> MutableLiveData<ApiResponse<T>>.onData(ui: Func1<T>) {
        if (this.hasObservers()) {
            return
        }
        this.observe(getLifecycleOwner()) {
            if (it.isSuccess()) {
                ui.invoke(it.result)
            }
        }
    }
}

inline fun <T> LifecycleInit.request(
    requestTag: String,
    crossinline requestCall: RequestCall<T>
): MutableLiveData<ApiResponse<T>> {
    return getViewModel().request(requestTag, requestCall)
}

interface ViewContainer {
    fun getRootView(): View?
}

/**** 参数柯里化 *****/
inline fun <T> ViewContainer.ui(crossinline fuc: Func2<View, T>): Func1<T> {
    return {
        fuc(getRootView(), it)
    }
}

inline fun ViewContainer.ui(crossinline fuc: Func1<View>): Func0 {
    return {
        fuc(getRootView())
    }
}

inline fun <T> Func1<T>.bind(observerSetter: IObserverSetter<T>) {
    observerSetter.observer(this)
}

