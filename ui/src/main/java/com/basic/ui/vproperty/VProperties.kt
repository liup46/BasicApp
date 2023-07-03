package com.basic.ui.vproperty

import android.content.Context
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
 * 注意：尽量不要使用v开头的所有属性的get方法，建议直接设置view的android 属性，而不是通过'v'属性去set。故'v'属性的get方法直接返回null.
 *
 * Note: You'd better not call get method on all extend property start with 'v'
 *
 *  Properties: Width, Height, Text, TextColor, Background, Foreground, Padding, Margin,
 *
 * @author Peter Liu
 * @since 2023/6/27 01:36
 *
 */

var View.vVisible by ObserverSetterPropertyDelegate<View, Boolean> {
    if (it == null) {
        return@ObserverSetterPropertyDelegate
    }
    this?.setVisible(it)
}

var View.vBackground by ObserverSetterPropertyDelegate<View, Drawable> {
    this?.background = it
}

var View.vPadding by ObserverSetterPropertyDelegate<View, Edge> {
    if (it == null) {
        return@ObserverSetterPropertyDelegate
    }
    this?.setPadding(
        it.left ?: this.paddingLeft,
        it.top ?: this.paddingTop,
        it.right ?: this.paddingRight,
        it.bottom ?: this.paddingBottom
    )
}

var View.vMargin by ObserverSetterPropertyDelegate<View, Edge> {
    if (this == null || it == null) {
        return@ObserverSetterPropertyDelegate
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

var TextView.vText by ObserverSetterPropertyDelegate<TextView, String> {
    this?.text = it
}


/**
 * setTextColor(@ColorInt color: Int)
 */
var TextView.vTextColor by ObserverSetterPropertyDelegate<TextView, Int> {
    if (it == null) {
        return@ObserverSetterPropertyDelegate
    }
    this?.setTextColor(it)
}

fun test(context: Context) {
    View(context).vVisible(false)
    View(context).vVisible.set(false)
}
