package com.basic.crash

import android.content.Context
import com.basic.env.App
import com.basic.log.Logger
import com.basic.util.getVersionName
import xcrash.ICrashCallback
import xcrash.TombstoneManager
import xcrash.XCrash
import java.io.File


/**
 * @author Peter Liu
 * @since 2023/3/28 17:21
 *
 */
object CrashInit {

    const val TAG = "Crash"

    private val JAVA_THREAD_WHITE_LIST = arrayOf("^main$", "^Binder:.*", ".*Finalizer.*")

    private var NATIVE_THREAD_WHITE_LIST = arrayOf(
        "^xcrash\\.sample$",
        "^Signal Catcher$",
        "^Jit thread pool$",
        ".*(R|r)ender.*",
        ".*Chrome.*"
    )

    fun init(context: Context) {
        JavaUncaughtExceptionHandler.initialize()
        val callback = ICrashCallback { logPath, emergency ->
            if (emergency != null) {
                Logger.e(TAG, emergency)
                Logger.f()
            }
            CrashInfoAppender.appendExtraInfo(logPath, emergency)
            CrashSender.sendCrash(File(logPath))
        }
        XCrash.init(
            context, XCrash.InitParameters()
                .setAppVersion(getAppVersion())
                .setLogger(CrashLogger())
                .setJavaRethrow(true)
                .setJavaDumpFds(false)
                .setJavaDumpNetworkInfo(false)
                .setJavaDumpAllThreads(true)
                .setJavaDumpAllThreadsCountMax(10)
                .setJavaDumpAllThreadsWhiteList(JAVA_THREAD_WHITE_LIST)
                .setJavaCallback(callback)
                .setNativeRethrow(true)
                .setNativeDumpAllThreadsWhiteList(NATIVE_THREAD_WHITE_LIST)
                .setNativeDumpAllThreadsCountMax(10)
                .setNativeDumpFds(false)
                .setNativeCallback(callback)
                .setAnrRethrow(true)
                .setAnrCheckProcessState(true)
                .setAnrDumpFds(true)
                .setAnrDumpNetwork(false)
                .setAnrCallback(callback)
                .setLogFileMaintainDelayMs(1000)
        )

        // 初始化之后上传并删除本地剩余的日志
        for (file in TombstoneManager.getAllTombstones()) {
            CrashSender.sendCrash(file)
        }
        Logger.i(TAG, "init end")
    }

    private fun getAppVersion(): String {
        return App.getContext().getVersionName() + "_" +
                if (App.isDev()) "debug" else if (App.isBeta()) "beta" else "rel"
    }

}