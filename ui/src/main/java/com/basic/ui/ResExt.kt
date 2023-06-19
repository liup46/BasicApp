package com.basic.ui

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.TypedValue
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.basic.env.App
import kotlin.math.roundToInt


fun <T : Any> T.getString(@StringRes resId: Int): String = resId.res2String()

/**
 * You can invoke the method use type 'Int' anywhere in kotlin,
 * and use 'LuxResourcesKt' in Java.
 */
fun Int.res2Color(): Int = ContextCompat.getColor(App.getContext(), this)

fun Int.res2String(): String = App.getContext().getString(this)

fun Int.res2String(vararg args: Any): String = App.getContext().getString(this, *args)

fun Int.res2Drawable(): Drawable? = ContextCompat.getDrawable(App.getContext(), this)

fun Int.res2StringArray(): Array<String>? = App.getContext().resources?.getStringArray(this)

fun Int.res2IntArray(): IntArray? = App.getContext().resources?.getIntArray(this)

fun Int.res2Dp(): Int = App.getContext().resources?.getDimensionPixelSize(this) ?: 0


@JvmOverloads
fun parseColor(str: String?, default: Int? = null): Int? {
    if (str.isNullOrBlank()) {
        return default
    }
    try {
        return if (str[0] == '#')
            Color.parseColor(str)
        else
            Color.parseColor("#$str")
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return default
}

@JvmOverloads
fun String?.toColor(@ColorInt defaultColor: Int? = null): Int? {
    return parseColor(this, defaultColor)
}

fun Float.px(): Int {
    val context = App.getContext()
    return TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        this,
        context.resources.displayMetrics
    ).roundToInt()
}

fun Float.dp(): Float {
    val density = App.getContext().resources.displayMetrics.density
    return this / density
}

val Number.px: Int
    get() = this.toFloat().px()

val Number.dp: Float
    get() {
        return this.toFloat().dp()
    }



