package com.basic.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleOwner
import com.basic.ui.view.StateView
import com.basic.ui.view.StatusBarHelper
import com.basic.ui.view.Toolbar
import com.basic.ui.vproperty.vText
import com.basic.ui.vproperty.vVisible

/**
 * @author Peter Liu
 * @since 2020/5/22 2:35 PM
 *
 */

abstract class BaseActivity : AppCompatActivity(), ViewContainer, LifecycleInit {
    override val layoutId: Int = -1
    override var toolbar: Toolbar? = null
    override var stateView: StateView? = null

    override val defaultViewModel: BaseViewModel by lazy { getViewModel(this) }

    override fun getRootView(): View? {
        return this.window.decorView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bundle = intent.extras
        initWindow()
        initRootContent(layoutInflater, null)?.let {
            setContentView(it)
        }
        bundle?.let {
            initExtra(it)
        }
        initFragment(savedInstanceState)
        if (needToolbar() && toolbar != null) {
            initToolbar(toolbar!!)
        }
        initView()
        initData()

//         if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
//             requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
//         }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent?.extras?.let {
            initExtra(it)
        }
    }

    protected open fun initWindow() {
        if (isFullScreen()) {
            StatusBarHelper.translucent(this)
        }
        lightStatusBar(true)
    }

    /**
     * 初始化fragment, 注意：如果activity中有fragment变量，要在savedInstanceState != null时给fragment变量赋值
     */
    protected open fun initFragment(savedInstanceState: Bundle?) {}

    protected open fun isFullScreen(): Boolean {
        return true
    }

    override fun needToolbar(): Boolean {
        return true
    }

    override fun needStateView(): Boolean {
        return true
    }

    protected fun lightStatusBar(lightMode: Boolean) {
        if (lightMode) {
            StatusBarHelper.setStatusBarLightMode(this)
        } else {
            StatusBarHelper.setStatusBarDarkMode(this)
        }
    }

    override fun onBackPressed() {
        //修复IllegalStateException: Can not perform this action after onSaveInstanceState
        if (supportFragmentManager.isStateSaved) {
            finishAfterTransition()
        } else {
            super.onBackPressed()
        }
    }

    override fun getLifecycleOwner(): LifecycleOwner {
        return this
    }

}

class TestActivity : BaseActivity() {
    val state by liveState("123")
    val stateVisable by liveState<Boolean>()

    fun test() {
        TextView(this).vVisible = stateVisable //动态绑定属性
        TextView(this).vVisible(false) //设置
        TextView(this).vVisible.set(false) //通过set方法设置
        TextView(this).vVisible(null) //不推荐，不能保证null值是有效的可接收值

        //定义ui更新作为变量
        val updateText = ui {
            TextView(this).vText.set("2")
            TextView(this).vText("1")
        }

        updateText() //执行ui更新

        //定义并执行一次ui更新
        ui<String> {
            TextView(this@TestActivity).vText.set("2")
            TextView(this@TestActivity).vText("1")
            Button(this@TestActivity).setOnClickListener {
                updateText()
            }
        }("123")

        //定义ui更新并跟livata绑定
        ui<String> {
            TextView(this@TestActivity).vText.set("2")
            TextView(this@TestActivity).vText("1")
        }.bind(state)

    }


}