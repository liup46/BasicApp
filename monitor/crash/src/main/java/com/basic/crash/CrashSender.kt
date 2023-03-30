package com.basic.crash

import com.basic.env.App
import com.basic.log.Logger
import xcrash.TombstoneManager
import xcrash.TombstoneParser
import java.io.File

/**
 * @author Peter Liu
 * @since 2023/3/29 00:07
 *
 */
object CrashSender {

    fun sendCrash(logFile:File){
        App.uploadService?.uploadCrash(logFile) { it, msg ->
            if (it) {
                TombstoneManager.deleteTombstone(logFile)
            } else {
                Logger.e(CrashInit.TAG, msg)
            }
        }
    }

}