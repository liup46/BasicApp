package com.peter.vunit.views

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.ViewPager

/**
 * @author Peter Liu
 * @since 2022/11/11 01:48
 *
 */
class TabLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ViewGroup(context, attrs, defStyleAttr) {
    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
    }

    fun attachPager(pager: ViewPager) {
        //todo need imp
    }

    fun notifyDataSetChanged() {

        //todo need imp

    }
}