package com.basic.ui

import android.content.Context
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.basic.net.ApiResponse
import com.basic.net.HttpService
import com.basic.ui.vproperty.*

/**
 * @author Peter Liu
 * @since 2023/7/7 23:42
 *
 */
class TestUiModeActivity : BaseActivity() {
    val state by liveState("123")
    val stateVisable by liveState<Boolean>()

    val testState1 by liveState("1")
    val testState2 by liveState<String>()

    var textView1: TextView? = null
    var textView2: TextView? = null

    fun test1() {
        textView1?.vText = testState1
        textView2?.vText = testState2

        TextView(this).vVisible = stateVisable //动态绑定属性
        TextView(this).vVisible(false) //直接设置
        TextView(this).vVisible.set(false) //通过set方法设置
        TextView(this).vVisible(null) //不推荐，不能保证null值是有效的可接收值

        //定义ui更新作为变量 ,该更新不接受数据参数
        val updateText = ui {
            TextView(this).vText.set("2")
            TextView(this).vText("1")
        }

        updateText() //执行ui更新

        //定义并执行一次ui更新
        ui<String> {
            TextView(this@TestUiModeActivity).vText.set("2")
            TextView(this@TestUiModeActivity).vText("1")
            Button(this@TestUiModeActivity).setOnClickListener {
                updateText()
            }
        }("123")

        //定义ui更新并跟livata绑定
        ui<String> {
            TextView(this@TestUiModeActivity).vText.set("2")
            TextView(this@TestUiModeActivity).vText("1")
        }.bind(state)

        //after http request end or click event , then set state value
        fun action() {
            testState1.set("2sss")
            testState2.set("2")
        }

    }

    fun test2(context: Context) {
        View(context).vVisible(false)
        View(context).vPadding(Edge(4.px))
        View(context).vVisible.set(false)
    }

    fun testReqeuest() {
        request<String>("123") {
            HttpService.post(path = "sdfa/1213")
        }.onResult {

        }

        request<String>("345") {
            HttpService.post(path = "sdfa/1213")
        }.onData {

        }

        request<String>("123") {
            HttpService.post(path = "sdfa/1213")
        }.onData(ui<String> {

        })

        ui<String> {

        }.onData("1231") {
            HttpService.post(path = "sdfa/1213")
        }

        ui<ApiResponse<String>> {

        }.onResult("1231") {
            HttpService.post(path = "sdfa/1213")
        }
    }
}