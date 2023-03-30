package com.basic.crash

import com.basic.env.App
import com.basic.env.AppLifecycleManager
import com.basic.log.Logger
import com.basic.util.DeviceUtils
import com.basic.util.NetworkUtils
import com.basic.util.getVersionCode
import xcrash.TombstoneManager
import java.io.RandomAccessFile
import java.util.*

/**
 * @author Peter Liu
 * @since 2023/3/30 17:31
 *
 */
object CrashInfoAppender {

    fun appendExtraInfo(logPath: String, emergency: String) {
        appendHeader(logPath, getExtraHeader())
        TombstoneManager.appendSection(logPath,"RecentActivities:", AppLifecycleManager.getRecentActivities().joinToString { "\n" })
        TombstoneManager.appendSection(logPath,"Disk Info:", DeviceUtils.getRomSizeInfo())
    }

    private fun getExtraHeader():String{
        return """
            platform: android
            versionCode: ${App.getContext().getVersionCode()}
            userid: ${App.getUserConfig().userId}
            deviceId: ${App.getDeviceConfig().deviceId}
            uuid: ${UUID.randomUUID()}
            channel: ${App.getChannelId()}
            net_type:${NetworkUtils.getNetworkType(App.getContext())}
        """.trimIndent()+"\n"
    }

     fun appendHeader(logPath: String?, text: String) {
        try {
            RandomAccessFile(logPath, "rws").use {
                it.readLine()
                val pos = it.filePointer
                val otherBytes = ByteArray((it.length() - pos).toInt())
                it.read(otherBytes)
                it.seek(pos)
                it.write(text.toByteArray(charset("UTF-8")))
                it.write(otherBytes)
            }
        } catch (e: Exception) {
            Logger.e(CrashInit.TAG, "appendHead failed", e)
        }
    }
}