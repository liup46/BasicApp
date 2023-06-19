package com.peter.vunit.views

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import androidx.fragment.app.Fragment

/**
 * @author: Peter Liu
 * @date: 2022/11/9
 *
 */
class Toolbar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : android.widget.Toolbar(context, attrs) {
}


fun Toolbar.enableLeftBack(activity: Activity) {
    // { activity.onBackPressed() }

}

fun Toolbar.enableLeftBack(fragment: Fragment) {
    // fragment.onBackPressed()

}