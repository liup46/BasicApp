package com.basic.ui.view

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TextView
import androidx.core.view.ViewCompat
import com.basic.ui.dp
import com.basic.util.nullOr
import kotlin.math.roundToInt

const val KEYBOARD_VISIBLE_THRESHOLD_DP = 100

var View.visible: Boolean
    get() {
        return visibility == View.VISIBLE
    }
    set(value) {
        visibility = if (value) View.VISIBLE else View.GONE
    }

var View.invisible: Boolean
    get() {
        return visibility == View.INVISIBLE
    }
    set(value) {
        visibility = if (value) View.INVISIBLE else View.VISIBLE
    }

var View.gone: Boolean
    get() {
        return visibility == View.GONE
    }
    set(value) {
        visibility = if (value) View.GONE else View.VISIBLE
    }

fun <T : View> T.setVisible(visible: Boolean, visibleAction: (T.() -> Unit)? = null): T {
    if (visible) {
        visibility = View.VISIBLE
        visibleAction?.invoke(this)
    } else {
        visibility = View.GONE
    }
    return this
}

fun <T : View> T.setInVisible(invisible: Boolean, trueAction: (T.() -> Unit)? = null): T {
    if (invisible) {
        visibility = View.INVISIBLE
        trueAction?.invoke(this)
    } else {
        visibility = View.VISIBLE
    }
    return this
}

fun <T : View> T.gone(goneAction: (T.() -> Unit)? = null): T {
    visibility = View.GONE
    goneAction?.invoke(this)
    return this
}

var View.startMargin: Int
    get():Int {
        return (layoutParams as? ViewGroup.MarginLayoutParams)?.marginStart ?: 0
    }
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.marginStart = value
    }

var View.leftMargin: Int
    get():Int {
        return (layoutParams as? ViewGroup.MarginLayoutParams)?.leftMargin ?: 0
    }
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.leftMargin = value
    }

var View.topMargin: Int
    get():Int {
        return (layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin ?: 0
    }
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.topMargin = value
    }

var View.endMargin: Int
    get():Int {
        return (layoutParams as? ViewGroup.MarginLayoutParams)?.marginEnd ?: 0
    }
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.marginEnd = value
    }

var View.rightMargin: Int
    get():Int {
        return (layoutParams as? ViewGroup.MarginLayoutParams)?.rightMargin ?: 0
    }
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.rightMargin = value
    }

var View.bottomMargin: Int
    get():Int {
        return (layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin ?: 0
    }
    set(value) {
        (layoutParams as? ViewGroup.MarginLayoutParams)?.bottomMargin = value
    }

var View.size: Pair<Int, Int>
    get() {
        return (layoutParams?.width ?: 0) to (layoutParams?.height ?: 0)
    }
    set(value) {
        layoutParams?.width = value.first
        layoutParams?.height = value.second
    }

var View.bottomPadding: Int
    get(): Int {
        return paddingBottom
    }
    set(value) {
        if (value != paddingBottom) {
            setPadding(paddingLeft, paddingTop, paddingRight, value)
        }
    }

var View.topPadding: Int
    get(): Int {
        return paddingTop
    }
    set(value) {
        if (value != paddingTop) {
            setPadding(paddingLeft, value, paddingRight, paddingBottom)
        }
    }

var View.rightPadding: Int
    get(): Int {
        return paddingRight
    }
    set(value) {
        if (value != paddingRight) {
            setPadding(paddingLeft, paddingTop, value, paddingBottom)
        }
    }

var View.leftPadding: Int
    get(): Int {
        return paddingLeft
    }
    set(value) {
        if (value != paddingLeft) {
            setPadding(value, paddingTop, paddingRight, paddingBottom)
        }
    }

var View.startPadding: Int
    get(): Int {
        return paddingStart
    }
    set(value) {
        if (value != paddingStart) {
            setPaddingRelative(value, paddingTop, paddingEnd, paddingBottom)
        }
    }

var View.endPadding: Int
    get(): Int {
        return paddingEnd
    }
    set(value) {
        if (value != paddingEnd) {
            setPaddingRelative(paddingStart, paddingTop, value, paddingBottom)
        }
    }


val View?.isFullVisibleToUser
    get(): Boolean {
        return getVisibilityStatus().first
    }

val View?.isPartVisibleToUser
    get(): Boolean {
        return getVisibilityStatus().second
    }

val View?.isInvisibleToUser
    get(): Boolean {
        return getVisibilityStatus().third
    }

/**
 * @return A Triple contains first for [isFullVisibleToUser], second for
 * [isPartVisibleToUser], and third for [isInvisibleToUser].
 */
private fun View?.getVisibilityStatus(): Triple<Boolean, Boolean, Boolean> {
    val visible: Boolean = this != null &&
            ViewCompat.isAttachedToWindow(this) && isShown

    if (!visible) {
        return Triple(false, false, true)
    }

    val visibleArea = Rect()
    val partOrFullVisible = this?.getGlobalVisibleRect(visibleArea).nullOr(false)

    val fullHeightVisible = (visibleArea.bottom - visibleArea.top) >=
            this?.height.nullOr(0)
    val fullWidthVisible = (visibleArea.right - visibleArea.left) >=
            this?.width.nullOr(0)

    val fullViewVisible = partOrFullVisible && fullHeightVisible && fullWidthVisible
    val partViewVisible = partOrFullVisible && (!fullHeightVisible || !fullWidthVisible)
    val viewInvisible = !partOrFullVisible

    return Triple(fullViewVisible, partViewVisible, viewInvisible)
}


/**
 * Determine if keyboard is visible.
 */
fun Activity.isKeyboardVisible(): Boolean {
    val activityRoot = (this.findViewById<View>(
        Window.ID_ANDROID_CONTENT
    ) as? ViewGroup)?.getChildAt(0)

    val visibleThreshold = KEYBOARD_VISIBLE_THRESHOLD_DP.dp.toDouble().roundToInt()
    val r = Rect()

    activityRoot?.getWindowVisibleDisplayFrame(r)
    val heightDiff = activityRoot?.rootView?.height.nullOr(0) - r.height()
    return heightDiff > visibleThreshold
}
