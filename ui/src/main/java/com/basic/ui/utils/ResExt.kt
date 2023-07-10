package com.basic.ui

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.FontRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import com.basic.env.App
import kotlin.math.roundToInt


/**
 * 资源转换类
 *
 */
fun Int.res2Color(): Int = ContextCompat.getColor(App.getContext(), this)

fun Int.res2String(): String = App.getContext().getString(this)

fun Int.res2String(vararg args: Any): String = App.getContext().getString(this, *args)

fun Int.res2Drawable(): Drawable? = ContextCompat.getDrawable(App.getContext(), this)

fun Int.res2StringArray(): Array<String>? = App.getContext().resources?.getStringArray(this)

fun Int.res2IntArray(): IntArray? = App.getContext().resources?.getIntArray(this)

fun Int.res2Dp(): Int = App.getContext().resources?.getDimensionPixelSize(this) ?: 0
fun Int.res2Font(assetFontFileName: String? = null): Typeface? =
    App.getContext().getFont(this, assetFontFileName)

val Number.px: Int
    get() = this.toFloat().px()

val Number.dp: Float
    get() {
        return this.toFloat().dp()
    }

internal fun Float.px(): Int {
    val context = App.getContext()
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        context.resources.displayMetrics
    ).roundToInt()
}

internal fun Float.dp(): Float {
    val density = App.getContext().resources.displayMetrics.density
    return this / density
}

internal fun Context.getFont(@FontRes fontId: Int, fontFileName: String? = null): Typeface? {
    // 文字默认字体 android roboto medium字体
    return try {
        //在小米/oppo 某些手机crash：Resources$NotFoundException: Font resource ID could not be retrieved
        ResourcesCompat.getFont(this, fontId)
    } catch (e: Exception) {
        if (!fontFileName.isNullOrBlank()) {
            Typeface.createFromAsset(this.assets, fontFileName)
        } else {
            null
        }
    }
}



