package com.basic.ui.dynamic

import android.graphics.Color
import android.text.TextUtils

/**
 * @author: Peter Liu
 * @date: 2022/12/9 00:03
 * Desc:
 *
 */
object Layout {
    const val Wrap = -1
    const val Match = -2;
}

data class Radius(
    var leftTop: Int = 0,
    val topRight: Int = leftTop,
    var rightBottom: Int = topRight,
    var leftBottom: Int = leftTop
)

data class Border(
    var color: Int = Color.TRANSPARENT,
    var left: Int = 0,
    val top: Int = left,
    var right: Int = left,
    var bottom: Int = top
)

data class Margin(
    var left: Int = 0,
    val top: Int = 0,
    var right: Int = 0,
    var bottom: Int = 0,
) {
    constructor(all: Int) : this(all, all, all, all)
}

object Orientation {
    const val Vertical = 0
    const val Horizontal = 1
}

object Align {
    const val Stack = 0x0000
    const val Left = 0x0001
    const val Right = 0x0010
    const val HCenter = 0x0011
    const val Top = 0x0100
    const val Bottom = 0x1000
    const val VCenter = 0x1100
    const val Center = 0x1111
    const val SpreadInside = 0x11111
    const val Spread = 0x111111
}

object TruncateAt {
    const val END = 0
    const val START = 1
    const val MIDDLE = 2
    const val MARQUEE = 4

    fun map(at: Int): TextUtils.TruncateAt {
        return when (at) {
            START -> TextUtils.TruncateAt.START
            MIDDLE -> TextUtils.TruncateAt.MIDDLE
            MARQUEE -> TextUtils.TruncateAt.MARQUEE
            else -> {
                TextUtils.TruncateAt.END
            }
        }
    }
}

object TextAlign{
    const val INHERIT = android.view.View.TEXT_ALIGNMENT_INHERIT
    const val GRAVITY = android.view.View.TEXT_ALIGNMENT_GRAVITY
    const val CENTER = android.view.View.TEXT_ALIGNMENT_CENTER
    const val START =android.view.View.TEXT_ALIGNMENT_TEXT_START
    const val END = android.view.View.TEXT_ALIGNMENT_TEXT_END
    const val VIEW_START = android.view.View.TEXT_ALIGNMENT_VIEW_START
    const val VIEW_END = android.view.View.TEXT_ALIGNMENT_VIEW_END
}