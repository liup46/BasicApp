package com.basic.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.basic.ui.dynamic.Box
import com.basic.ui.dynamic.Text
import com.basic.ui.dynamic.View
import com.google.android.material.R

/**
 * @author Peter Liu
 * @since 2023/6/8 15:02
 *
 */
class Test: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(stest().build(this))
    }

    fun stest() : View {
        Box {
            Text {
                textSize = 14.dp
                textColor = R.color.test_color.res2Color()
            }
        }
        Text {
        }
    }


}