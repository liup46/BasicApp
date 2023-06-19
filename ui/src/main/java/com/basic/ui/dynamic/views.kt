package com.basic.ui.dynamic

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatTextView
import com.basic.ui.dynamic.Layout.Wrap

/**
 * @author: Peter Liu
 * @date: 2022/12/8 18:46
 * Desc:
 *
 */
open class View {
    var id: String = this.hashCode().toString()
    var width: Int? = Wrap
    var height: Int? = Wrap
    var margin: Margin? = null

    var bgColor: Int? = Color.TRANSPARENT
    var bgImage: Drawable ? = null
    var bgImageUrl: String? = null

    var radius: Radius? = null
    var border: Border? = null
    var navPath: String? = null //https://xxx; action://yyy; page://zzz; dialog://tttt

    /*
     * this property will override navPath
     */
    var onClick: ((View) -> Unit)? = null
    var onLongPress: ((View) -> Unit)? = null

    open protected fun createView(context: Context): android.view.View {
        return android.view.View(context)
    }

    open protected fun applyProperty(view: android.view.View) {
        if(bgImage!= null){
            view.background = bgImage
        }
        if(radius!= null || border!= null){
        }

        if (!navPath.isNullOrBlank()) {
            view.setOnClickListener {
                navPath
            }
        }
        if (onClick != null) {
            view.setOnClickListener {
                onClick?.invoke(this)
            }
        }
        if (onLongPress != null) {
            view.setOnClickListener {
                onLongPress?.invoke(this)
            }
        }
    }

    fun build(context: Context): android.view.View {
        val view = createView(context)
        applyProperty(view)
        return view
    }
}

open class Text : View() {
    var text: String? = null
    var textSize: Float = 14f
    var textColor: Int = Color.BLACK
    var textAlign = TextAlign.GRAVITY
    var lineNumber: Int = Int.MAX_VALUE
    var ellipsize = TruncateAt.END

    override fun createView(context: Context): AppCompatTextView {
        return AppCompatTextView(context)
    }

    override fun applyProperty(view: android.view.View) {
        super.applyProperty(view)
        (view as AppCompatTextView).let {
            it.setText(text)
            it.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize)
            it.setTextColor(textColor)
            it.maxLines = lineNumber
            it.ellipsize = TruncateAt.map(ellipsize)
            it.textAlignment = textAlign
        }
    }
}

inline fun Text(crossinline initFun: Text.() -> Unit): Text {
    return Text().apply {
        this.initFun()
    }
}

open class Image : View() {
    var url: String? = null
    var icon: String? = null
    var drawable: Int? = 0
}

inline fun Image(crossinline initFun: Image.() -> Unit): Image {
    return Image().apply {
        this.initFun()
    }
}

open class Box : View() {
    private val children: MutableList<View> = mutableListOf()
    var align = Align.Stack
    open var orientation: Int = Orientation.Vertical

    fun addView(view: View) {
        children.add(view)
    }

    fun removeView(view: View) {
        children.remove(view)
    }

    fun Image(initFun: Image.() -> Unit) {
        addView(Image().apply { initFun() })
    }

    fun Text(initFun: Text.() -> Unit) {
        addView(Text().apply { initFun() })
    }

    override fun createView(context: Context): LinearLayout {
        return null;
    }
}

inline fun Box(crossinline initFun: Box.() -> Unit): Box {
    return Box().apply {
        this.initFun()
    }
}

open class Scroller : Box() {
    var onFling: ((View, offx: Int, offy: Int) -> Unit)? = null

}

