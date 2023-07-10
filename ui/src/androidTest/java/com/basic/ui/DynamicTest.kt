package com.basic.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.basic.ui.dynamic.Box
import com.basic.ui.dynamic.Text
import com.basic.ui.dynamic.View
import com.google.android.material.R

/**
 * @author Peter Liu
 * @since 2023/7/7 23:48
 *
 */
class DynamicTest: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(stest().build(this))
    }

    fun stest() : View {
        return Box {
            Text {
                textSize = 14.dp
                textColor = R.color.test_color.res2Color()
            }
        }
        Text {
        }
    }


}