package com.basic.util

import android.os.Build
import java.net.URLEncoder

/**
 * @author Peter Liu
 * @since 2023/3/24 00:38
 *
 */
object DeviceUtils {

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
}