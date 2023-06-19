package com.basic.ui.list

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


/**
 * 支持LinearLayout, GridLayout
 * Vertical 方向支持首行，最后一行是否显示分割
 * Horizontal 只支持条目中间，不支持最左边和最右边
 * Grid 支持首行，最后一行，垂直条目中间，支持是否显示最后一行，空位item是否显示分割
 *
 * @author Peter Liu
 * @since 3/23/21 7:10 PM
 *
 */

class DividerItemDecoration : RecyclerView.ItemDecoration() {
    /**
     * 是否绘制第一行顶部横向分割线
     */
    var drawFirstRowTop = false

    /**
     * 是否绘制最后一行底部横向分割线
     */
    var drawLastRowBottom = false

    /**
     * 是否绘制最后一行空的item的分割线
     * (对于GridLayout)
     */
    var draLastRowEmptyItem = false

    var dividerWith = 0
        set(value) {
            field = value
            horizontalDividerWidth = value
            verticalDividerWidth = value
        }

    var horizontalDividerWidth = 0
    var verticalDividerWidth = 0

    var dividerColor: Int = Color.TRANSPARENT
        set(value) {
            field = value
            mDivider = ColorDrawable(value)
        }

    private var mDivider: Drawable? = ColorDrawable(Color.TRANSPARENT)

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        if ((horizontalDividerWidth == 0 && verticalDividerWidth == 0) || mDivider == null) {
            outRect.set(0, 0, 0, 0)
            return
        }
        if (parent.adapter == null) {
            outRect.set(0, 0, 0, 0)
            return
        }

        val itemCount = parent.adapter!!.itemCount
        if (itemCount <= 0) {
            outRect.set(0, 0, 0, 0)
            return
        }

        val itemPosition = parent.getChildAdapterPosition(view)
        if (itemPosition < 0) {
            outRect.set(0, 0, 0, 0)
            return
        }

        val spanCount = getSpanCount(parent)

        var top = 0
        //画第一行顶部分割线
        if (drawFirstRowTop && itemPosition < spanCount) {
            top = horizontalDividerWidth
        }
        var bottom = horizontalDividerWidth
        //不画最后一行底部分割线
        if (!drawLastRowBottom) {
            val count = itemCount % spanCount
            if (count == 0 && itemPosition >= itemCount - spanCount) {
                bottom = 0
            } else if (count != 0 && itemPosition >= itemCount - count) {
                bottom = 0
            }
        }

        //最后一列
        var right = verticalDividerWidth
        if ((itemPosition + 1) % spanCount == 0) {
            right = 0
        }
        outRect.set(0, top, right, bottom)
    }

    private fun getSpanCount(parent: RecyclerView): Int {
        val layoutManager = parent.layoutManager
        return if (layoutManager is GridLayoutManager) {
            layoutManager.spanCount
        } else if (layoutManager is LinearLayoutManager) {
            if (layoutManager.orientation == LinearLayoutManager.VERTICAL) {
                1
            } else {
                parent.adapter?.itemCount ?: 1
            }
        } else {
            1
        }
    }

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        if (horizontalDividerWidth > 0) {
            if (drawFirstRowTop) {
                drawFirstRowTop(c, parent)
            }
            // 绘制水平方向
            drawHorizontal(c, parent)
        }

        if (verticalDividerWidth > 0) {
            // 绘制垂直方向
            drawVertical(c, parent)
        }

        if (draLastRowEmptyItem) {
            drawLastRowEmptyItem(c, parent)
        }
    }

    //画第一行顶部分割线
    private fun drawFirstRowTop(c: Canvas, parent: RecyclerView) {
        val spanCount = getSpanCount(parent)
        if (draLastRowEmptyItem || spanCount == 1) { // 直接水平画直线
            val left = parent.left + parent.paddingLeft
            val top = parent.paddingTop
            val right = parent.right - parent.paddingRight
            val bottom: Int = top + horizontalDividerWidth
            mDivider?.setBounds(left, top, right, bottom)
            mDivider?.draw(c)
        } else { //画每个item 顶部 的分割线
            for (i in 0 until spanCount) {
                val child = parent.getChildAt(i)
                if (child == null) return
                val params = child.layoutParams as RecyclerView.LayoutParams
                val left = child.left - params.leftMargin
                val top = child.top - params.topMargin - horizontalDividerWidth
                val right = child.right + params.rightMargin + verticalDividerWidth
                val bottom: Int = top + horizontalDividerWidth
                mDivider?.setBounds(left, top, right, bottom)
                mDivider?.draw(c)
            }
        }
    }

    //画右边竖直分割线
    private fun drawVertical(c: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val left = child.right + params.rightMargin
            val top = child.top - params.topMargin
            val right: Int = left + verticalDividerWidth
            val bottom = child.bottom + params.bottomMargin
            mDivider?.setBounds(left, top, right, bottom)
            mDivider?.draw(c)
        }
    }

    //画水平底部分割线
    private fun drawHorizontal(c: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            val params = child.layoutParams as RecyclerView.LayoutParams
            val left = child.left - params.leftMargin
            val top = child.bottom + params.bottomMargin
            val right = child.right + params.rightMargin + verticalDividerWidth //加个分割线的宽度
            val bottom: Int = top + horizontalDividerWidth
            mDivider?.setBounds(left, top, right, bottom)
            mDivider?.draw(c)
        }
    }

    //画最后一行空item的右边分割线
    private fun drawLastRowEmptyItem(c: Canvas, parent: RecyclerView) {
        val childCount = parent.childCount
        val spanCount = getSpanCount(parent)
        if (childCount % spanCount != 0) {
            for (j in 1 until (spanCount - childCount % spanCount)) {
                val child = parent.getChildAt(childCount - 1)
                val params = child.layoutParams as RecyclerView.LayoutParams
                val left = child.right + params.rightMargin + child.width * j + dividerWith * j
                val top = child.top - params.topMargin
                val right: Int = left + dividerWith
                val bottom = child.bottom + params.bottomMargin
                mDivider?.setBounds(left, top, right, bottom)
                mDivider?.draw(c)
            }
        }
    }

}