package com.basic.ui.utils

import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.view.View
import androidx.annotation.ColorInt

/**
 * @author Peter Liu
 * @since 2022/3/30 21:23
 *
 */
fun SpannableStringBuilder.setClickSpan(
    onclick: View.OnClickListener,
    @ColorInt color: Int,
    start: Int = 0,
    end: Int = this.length,
    showUnderline: Boolean = false
): SpannableStringBuilder {
    return this.apply {
        setSpan(newClickSpan(onclick, color,showUnderline), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

fun SpannableStringBuilder.appendClickSpan(
    str: String,
    onclick: View.OnClickListener,
    @ColorInt color: Int,
    showUnderline: Boolean = false
): SpannableStringBuilder {
    return this.apply {
        append(str, newClickSpan(onclick, color,showUnderline), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}

fun newClickSpan(
    onclick: View.OnClickListener,
    @ColorInt color: Int,
    showUnderline: Boolean = false
): ClickableSpan {
    return object : ClickableSpan() {
        override fun onClick(widget: View) {
            onclick.onClick(widget)
        }

        override fun updateDrawState(ds: TextPaint) {
            ds.color = color
            ds.isUnderlineText = showUnderline
        }
    }
}