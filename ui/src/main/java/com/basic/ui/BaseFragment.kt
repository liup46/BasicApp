package com.basic.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.basic.ui.view.StateView
import com.peter.vunit.views.Toolbar

/**
 * @author Peter Liu
 * @since 2020/5/22 2:37 PM
 *
 */
abstract class BaseFragment : Fragment(), ViewGetter, LifecycleInit {
    override val layoutId: Int = -1
    override var toolbar: Toolbar? = null
    override var stateView: StateView? = null
    private var isFirstResume = true

    private val viewmodel:BaseViewModel by lazy { getViewModel(this) }

    private var name by observable("st")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { initExtra(it) }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return initRootContent(inflater, container) ?: super.onCreateView(
            inflater,
            container,
            savedInstanceState
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (needToolbar() && toolbar != null) {
            initToolbar(toolbar!!)
        }
        initView()
        initModel()
    }

    override fun getRootView(): View? {
        return view
    }

    override fun onResume() {
        super.onResume()
        if (isFirstResume) {
            isFirstResume = false
            onFirstResume()
        }
    }

    protected open fun onFirstResume() {
    }

    open fun onBackPressed() {
        activity?.onBackPressed()
    }

    fun <T> observable(default:T?= null):LiveDataPropertyProvider<BaseFragment,T>{
        return viewmodel.liveDataProvider(default)
    }
}

