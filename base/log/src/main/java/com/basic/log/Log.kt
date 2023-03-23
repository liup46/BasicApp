package com.basic.log

import android.util.Log
import com.basic.env.App
import com.basic.util.isDebug
import com.basic.util.traceMessage
import com.dianping.logan.Logan
import com.dianping.logan.LoganConfig
import com.dianping.logan.SendLogRunnable
import java.io.File


/**
 * @author Peter Liu
 * @since 2023/3/19 00:57
 *
 */
object Logger {
    private const val Tag = "Logger"
    private const val TYPE_CODE = 3
    const val TYPE_HTTP =4

    init {
        val context = App.getContext();
        val config = LoganConfig.Builder()
            .setMaxFile(20)
            .setCachePath(context.filesDir.absolutePath)
            .setPath(
                (context.getExternalFilesDir(null)?.absolutePath
                        + File.separator) + "logan_v1"
            )
            .setEncryptKey16("log123450".toByteArray())
            .setEncryptIV16("log123450".toByteArray())
            .build()
        Logan.setDebug(context.isDebug())
        Logan.init(config)
        Logan.setOnLoganProtocolStatus { cmd, code ->
            Log.i(Tag, "Logan protoal status $cmd: $code")
        }
    }

    private var logUploader: LogUploader? = null
    fun setLogUploader(logUploader: LogUploader) {
        this.logUploader = logUploader
    }

    private var sendLogRunnable = object : SendLogRunnable() {
        override fun sendLog(logFile: File?) {
            if (logFile != null && logFile.exists()) {
                logUploader?.invoke(logFile)
            }
            finish()
            if (logFile?.name?.contains(".copy") == true) {
                logFile.delete();
            }
        }
    }

    private fun write(type: Int, tag: String, msg: String) {
        Logan.w("$tag|$msg", type)
    }

    fun e(tag: String, msg: String, e: Throwable? = null, type: Int = TYPE_CODE) {
        write(type, "|e|$tag", msg + e?.traceMessage())
    }

    fun i(tag: String, msg: String, e: Throwable? = null, type: Int = TYPE_CODE) {
        write(type, "|i|$tag", msg + e?.traceMessage())
    }

    fun d(tag: String, msg: String, e: Throwable? = null) {
        Log.d(tag, msg, e)
    }

    fun f() {
        Logan.f()
    }

    fun send(dates: Array<String>) {
        Logan.s(dates, sendLogRunnable)
    }

}

typealias LogUploader = (File) -> Unit