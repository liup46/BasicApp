package com.basic.ui.danmu

import android.animation.Animator
import android.animation.Animator.AnimatorListener
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.core.view.children
import com.frogsing.contacts.R
import com.frogsing.libutil.ui.ResUtil
import kotlin.random.Random

/**
 *
 * @author: Peter Liu
 * @date: 2023/6/28
 */
class DanmuGroupView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ViewGroup(context, attrs) {
    companion object {
        const val RTL = 0
        const val LTR = 1
    }

    init {
        setWillNotDraw(false)
    }


    var itemMargin: Int = 60 //Px

    var orientation: Int = LTR

    private var isStarted = false
    private var consumedY = 0

    private val animHandler by lazy { Handler(Looper.getMainLooper()) }

    private val danmuItems by lazy { mutableListOf<DanmuItem>() }

    public fun addDanmu(danmuItem: DanmuItem) {
        danmuItems.add(danmuItem)
        addView(danmuItem)
    }

    public fun addDanmus(danmuItems: List<DanmuItem>) {
        this.danmuItems.addAll(danmuItems)
        danmuItems.forEach {
            addView(it)
        }
    }

    private fun addView(danmuItem: DanmuItem) {
        TextView(this.context).apply {
            text = danmuItem.text
            setPadding(
                danmuItem.danmuItemStyle.paddingLeft,
                danmuItem.danmuItemStyle.paddingTop,
                danmuItem.danmuItemStyle.paddingRight,
                danmuItem.danmuItemStyle.paddingBottom
            )
            setTextSize(
                TypedValue.COMPLEX_UNIT_SP, danmuItem.danmuItemStyle.textSize,
            )
            setTextColor(danmuItem.danmuItemStyle.texColor)
            setBackgroundColor(danmuItem.danmuItemStyle.backgroundColor)
            setTypeface(danmuItem.danmuItemStyle.typeface)
            addView(this)
        }
    }

    public fun start() {
        isStarted = true
        invalidate()
    }

    fun stop() {
        isStarted = false
        children.forEachIndexed { index, view ->
            view.animate().cancel()
            val danmuItem = danmuItems[index]
            danmuItem.isAnimating = false
        }
        animHandler.removeCallbacksAndMessages(null)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        val h = measuredHeight - paddingTop - paddingBottom
        children.forEachIndexed { index, view ->
            val danmuItem = danmuItems[index]
            if (danmuItem.y < 0) {
                var nextY = consumedY + itemMargin
                if (nextY > h) {
                    nextY = itemMargin
                }
                danmuItem.y = nextY
                consumedY = view.measuredHeight + nextY

                val childLeft = if (orientation == LTR) -view.measuredWidth else measuredWidth
                val childRight = if (orientation == LTR) 0 else measuredWidth + view.measuredWidth
                view.layout(childLeft, danmuItem.y, childRight, bottom)
                //todo 计算x避免重叠
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        children.forEachIndexed { index, view ->
            val danmuItem = danmuItems[index]
            if (isStarted && !danmuItem.isAnimating) {
                danmuItem.isAnimating = true
                val animatorListener = object : AnimatorListener {
                    override fun onAnimationStart(animation: Animator?) {
                    }

                    override fun onAnimationEnd(animation: Animator?) {
                        danmuItem.animateEnd = true
                        danmuItem.isAnimating = false
                    }

                    override fun onAnimationCancel(animation: Animator?) {
                    }

                    override fun onAnimationRepeat(animation: Animator?) {
                    }
                }
                animHandler.postDelayed({
                    if (orientation == LTR) {
                        view.animate().x((width - paddingRight).toFloat())
                            .setDuration(danmuItem.duration)
                            .setListener(animatorListener).start()
                    } else {
                        view.animate().x(0f).setDuration(danmuItem.duration)
                            .setListener(animatorListener).start()
                    }
                }, danmuItem.startOffset)
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stop()
    }

    class DanmuItemStyle {
        @ColorInt
        var backgroundColor: Int = ResUtil.getResColor(R.color.bl_black_50)

        @ColorInt
        var texColor = Color.WHITE
        var textSize = ResUtil.getResDimenSp(R.dimen.adjust_sp_12).toFloat() //px float
        var typeface: Typeface? = ResUtil.getFont(R.font.pingfang_sc_medium)

        var paddingTop: Int = 0 //px
        var paddingLeft: Int = 0 //px
        var paddingRight: Int = 0 //px
        var paddingBottom: Int = 0 //px

        fun padding(padding:Int){
            this.paddingLeft =padding
            this.paddingTop =padding
            this.paddingRight =padding
            this.paddingBottom =padding
        }
    }


    class DanmuItem(val text: CharSequence) {
        //在y方向的位置
        internal var y: Int = -1

        //在滚动
        internal var isAnimating = false

        internal var animateEnd = false

        //停止时固定的位置
        var fixX = 0//

        var danmuItemStyle = DanmuItemStyle()
        var duration = 1000L //ms
        var startOffset: Long = Random.nextLong(500) //ms
    }

}

