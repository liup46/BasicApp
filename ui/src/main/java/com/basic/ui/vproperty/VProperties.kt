package com.basic.ui.vproperty

import android.graphics.drawable.Drawable
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import com.basic.ui.view.setVisible


/**
 *
 *
 * 可动态绑定的属性。具体用法参考[com.basic.ui.TestUiModeActivity]
 *
 *  Properties: Width, Height, Text, TextColor, Background, Padding, Margin,
 *
 *
 * @author Peter Liu
 * @since 2023/6/27 01:36
 *
 */
var View.vVisible by ObserverSetterDelegate<View, Boolean> {
    if (it == null) {
        return@ObserverSetterDelegate
    }
    this?.setVisible(it)
}

var View.vBackground by ObserverSetterDelegate<View, Drawable> {
    this?.background = it
}

var View.vPadding by ObserverSetterDelegate<View, Edge> {
    if (it == null) {
        return@ObserverSetterDelegate
    }
    this?.setPadding(
        it.left ?: this.paddingLeft,
        it.top ?: this.paddingTop,
        it.right ?: this.paddingRight,
        it.bottom ?: this.paddingBottom
    )
}

var View.vMargin by ObserverSetterDelegate<View, Edge> {
    if (this == null || it == null) {
        return@ObserverSetterDelegate
    }
    val param = this.layoutParams
    if (param is ViewGroup.MarginLayoutParams) {
        param.setMargins(
            it.left ?: this.marginLeft,
            it.top ?: this.marginTop,
            it.right ?: this.marginRight,
            it.bottom ?: this.marginBottom
        )
        this.layoutParams = param
//      this.requestLayout()
    }
}

var TextView.vText by ObserverSetterDelegate<TextView, String> {
    this?.text = it
}

/**
 * setTextColor(@ColorInt color: Int)
 */
var TextView.vTextColor by ObserverSetterDelegate<TextView, Int> {
    if (it == null) {
        return@ObserverSetterDelegate
    }
    this?.setTextColor(it)
}
