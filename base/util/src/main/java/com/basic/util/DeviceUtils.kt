package com.basic.util

import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.io.File
import java.net.URLEncoder


/**
 * Extentions for Phone Deivces
 *
 * @author Peter Liu
 * @since 2023/3/24 00:38
 *
 */
object DeviceUtils {
    const val GB = 0x40000000L

    fun getPhoneBrand(): String {
        return getEncoder(Build.BRAND) + " " + getEncoder(Build.MODEL.replace(Build.BRAND, ""))
    }

    private fun getEncoder(str: String?): String {
        try {
            if (!str.isNullOrBlank()) {
                return URLEncoder.encode(str.trim { it <= ' ' }, "utf-8")
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return "unknown"
    }

    /**
     * 获取手机ROM使用情况
     *
     * @return 3.56GB，已用：56.03GB
     */
    fun getRomSizeInfo(): String {
        val total: Float = getTotalRomSize() * 1.0f / GB
        val available: Float = getAvailableRomSize() * 1.0f / GB
        return "可用:" + available + "GB,已用:" + (total - available) + "GB"
    }

    fun getTotalRomSize(): Long {
        val path: File = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize: Long = stat.blockSizeLong
        val totalBlocks: Long = stat.blockCountLong
        return totalBlocks * blockSize
    }

    fun getAvailableRomSize(): Long {
        val path: File = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize: Long = stat.blockSizeLong
        val availableBlocks: Long = stat.availableBlocksLong
        return availableBlocks * blockSize
    }
}