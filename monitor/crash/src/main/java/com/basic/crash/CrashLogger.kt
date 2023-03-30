package com.basic.crash

import android.util.Log
import com.basic.log.Logger
import xcrash.ILogger

/**
 * @author Peter Liu
 * @since 2023/3/28 17:36
 *
 */
class CrashLogger:ILogger {
    override fun v(tag: String, msg: String) {
        Logger.v(tag, msg)
    }

    override fun v(tag: String, msg: String, tr: Throwable?) {
        Logger.v(tag, msg, tr)
    }

    override fun d(tag: String?, msg: String) {
        Log.d(tag, msg)
    }

    override fun d(tag: String, msg: String, tr: Throwable) {
        Logger.d(tag, msg, tr)
    }

    override fun i(tag: String, msg: String) {
        Logger.i(tag, msg)
    }

    override fun i(tag: String, msg: String, tr: Throwable) {
        Logger.i(tag, msg, tr)
    }

    override fun w(tag: String, msg: String) {
        Logger.i(tag, msg)
    }

    override fun w(tag: String, msg: String, tr: Throwable?) {
        Logger.e(tag, msg, tr)
    }

    override fun e(tag: String, msg: String) {
        Logger.e(tag, msg)
    }

    override fun e(tag: String, msg: String, tr: Throwable?) {
        Logger.e(tag, msg, tr)
    }
}