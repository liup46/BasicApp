package com.basic.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View

/**
 * @author: Peter Liu
 * @date: 2022/11/9
 *
 */
class StateView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr), IStateView {
    override fun showError(code: String?, msg: String?) {
        TODO("Not yet implemented")
    }

    override fun showLoading(show: Boolean) {
        TODO("Not yet implemented")
    }

    override fun showEmpty(msg: String?) {
        TODO("Not yet implemented")
    }
}

