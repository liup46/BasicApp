package com.basic.crash

import com.basic.log.Logger
import java.util.concurrent.TimeoutException

/**
 * @author Peter Liu
 * @since 2023/3/28 23:42
 *
 */

object JavaUncaughtExceptionHandler : Thread.UncaughtExceptionHandler {
    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    /**
     * 初始化时机一定要在XCrash之前，目的是使XCrash中getDefaultUncaughtExceptionHandler的是此handler。
     */
    fun initialize() {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        try {
            Thread.setDefaultUncaughtExceptionHandler(this)
        } catch (ignored: Exception) {
        }
    }

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (defaultHandler != null) {
            Thread.setDefaultUncaughtExceptionHandler(defaultHandler)
        }
        if (t.name == "FinalizerWatchdogDaemon" && e is TimeoutException) {
            // ignore finalize time out
            // 详见：https://mp.weixin.qq.com/s/uFcFYO2GtWWiblotem2bGg
            return
        }
        Logger.e(CrashInit.TAG, "", e)
        Logger.f()
        defaultHandler?.uncaughtException(t, e)
    }
}