package com.basic.env

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import java.lang.ref.WeakReference
import java.util.*
import kotlin.collections.ArrayList


/**
 * @author Peter Liu
 * @since 2023/3/30 23:36
 *
 */
object AppLifecycleManager {
    private const val TAG = "AppLifecycleManager"
    private var mActivityRef: WeakReference<Activity>? = null
    private val mActivityStack: Stack<Activity> = Stack()
    private var rencentActivitiesNames = LimitSizeLinkList<String>()
    private var mForegroundActivityCount = 0

    private val mListeners: MutableSet<AppStatusListener> = HashSet()
    private val mUIHandler: Handler = Handler(Looper.getMainLooper())

    fun init(application: Application) {
        application.registerActivityLifecycleCallbacks(object :
            Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                mActivityStack.push(activity)
                rencentActivitiesNames.add(activity::class.java.name)
            }

            override fun onActivityStarted(activity: Activity) {
                if (mForegroundActivityCount == 0) {
                    dispatchOnForeground()
                }
                mForegroundActivityCount++
            }

            override fun onActivityResumed(activity: Activity) {
                mActivityRef = WeakReference(activity)
                if (mActivityRef!!.get() !== mActivityStack.peek()) {
                    Log.e(TAG, "Should Not like this!!Pls Check this situation!!")
                }
            }

            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {
                mForegroundActivityCount--
                if (mForegroundActivityCount < 0) {
                    mForegroundActivityCount = 0
                    Log.e(TAG, "Should Not like this!!Pls Check this situation!!")
                }
                if (mForegroundActivityCount == 0) {
                    dispatchOnBackground()
                }
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                mActivityStack.remove(activity)
            }
        })
    }

    /**
     * 获取当前在显示的Activity实例
     *
     * @return 当前显示在前台的Activity
     */
    fun getPresentActivity(): Activity? {
        return mActivityRef?.get()
    }

    /**
     * @return 将当前所有的Activity作为list输出返回
     */
    fun getAllActivities(): List<Activity?> {
        return LinkedList(mActivityStack)
    }

    fun getRecentActivities():List<String>{
        return ArrayList(rencentActivitiesNames)
    }

    /**
     * finish 栈中的除了参数之外的所有Activity
     *
     * @param clazz 需要保留的Activity
     */
    fun clearActivities(clazz: Class<*>?) {
        if (clazz == null) {
            dispatchOnExit()
        }
        synchronized(this) {
            for (activity in mActivityStack) {
                if (activity == null) {
                    continue
                }
                if (clazz != null && TextUtils.equals(
                        clazz.simpleName, activity.javaClass.simpleName
                    )
                ) {
                    continue
                }
                if (!activity.isFinishing) {
                    activity.finish()
                }
            }
        }
    }


    /**
     * 注册APP状态监听器
     *
     * @param listener APP状态监听
     */
    fun registerAppStatusListener(listener: AppStatusListener) {
        mUIHandler.post(Runnable { mListeners.add(listener) })
    }

    /**
     * 取消注册APP状态监听器
     *
     * @param listener APP状态监听
     */
    fun unregisterAppStatusListener(listener: AppStatusListener) {
        mUIHandler.post { mListeners.remove(listener) }
    }

    internal fun dispatchOnForeground() {
        mUIHandler.post {
            for (listener in mListeners) {
                listener.onForeground()
            }
        }
    }

    internal fun dispatchOnBackground() {
        mUIHandler.post {
            for (listener in mListeners) {
                listener.onBackground()
            }
        }
    }

    private fun dispatchOnExit() {
        mUIHandler.post {
            for (listener in mListeners) {
                listener.onExit()
            }
        }
    }

    fun isInForeground(): Boolean {
        return mForegroundActivityCount > 0
    }

    /**
     * APP状态监听接口
     */
    interface AppStatusListener {
        /**
         * App切换到前台
         */
        fun onForeground()

        /**
         * App切换到后台
         */
        fun onBackground()

        /**
         * 清栈
         */
        fun onExit()
    }
}

class LimitSizeLinkList<T>(var max: Int = 10) : LinkedList<T>() {

    override fun add(element: T): Boolean {
        if (size + 1 > max) {
            super.removeFirst()
        }
        return super.add(element)
    }
}