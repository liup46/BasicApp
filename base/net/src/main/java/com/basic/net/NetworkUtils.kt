package com.basic.net

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.telephony.TelephonyManager


/**
 * @author Peter Liu
 * @since 2023/3/23 17:08
 *
 */
object NetworkUtils {

    const val NETWORK_TYPE_UNKNOWN = 0
    const val NETWORK_TYPE_WIFI = 1
    const val NETWORK_TYPE_2G = 2
    const val NETWORK_TYPE_3G = 3
    const val NETWORK_TYPE_4G = 4
    private var connectivityManager: ConnectivityManager? = null
    private var teleManager: TelephonyManager? = null

    /**
     * 判断是否有网络连接
     * 1，显示连接已保存，但标题栏没有，即没有实质连接上   not connect， available
     * 2，显示连接已保存，标题栏也有已连接上的图标，      connect， available
     * 3，选择不保存后                                not connect， available
     * 4，选择连接，在正在获取IP地址时                  not connect， not available
     *
     * @param context
     * @return
     */
    @SuppressLint("MissingPermission")
    fun isNetworkConnected(context: Context?): Boolean {
        try {
            if (context != null) {
                val mConnectivityManager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val mNetworkInfo = mConnectivityManager.activeNetworkInfo
                if (mNetworkInfo != null) {
                    return mNetworkInfo.isAvailable && mNetworkInfo.isConnected
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 网络是否可连接状态
     */
    @SuppressLint("MissingPermission")
    fun netIsConnected(context: Context?): Boolean {
        try {
            if (context != null) {
                val mConnectivityManager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val mNetworkInfo = mConnectivityManager.activeNetworkInfo
                if (mNetworkInfo != null) {
                    return mNetworkInfo.isConnected
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    /**
     * 网络是否可用状态
     */
    @SuppressLint("MissingPermission")
    fun netIsAvailable(context: Context?): Boolean {
        try {
            if (context != null) {
                val mConnectivityManager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val mNetworkInfo = mConnectivityManager.activeNetworkInfo
                if (mNetworkInfo != null) {
                    return mNetworkInfo.isAvailable
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun connectivityManager(context: Context): ConnectivityManager? {
        if (connectivityManager == null) {
            try {
                connectivityManager = context
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
            } catch (ignore: Exception) {
            }
        }
        return connectivityManager
    }

    private fun telephonyManager(context: Context): TelephonyManager? {
        if (teleManager == null) {
            try {
                teleManager =
                    context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager?
            } catch (ignore: Exception) {
            }
        }
        return teleManager
    }

    /**
     * 返回当前网络类型，NETWORK_TYPE_XXX
     */
    @SuppressLint("MissingPermission")
    fun getNetworkType(context: Context): Int {
        val connectivityManager = connectivityManager(context) ?: return NETWORK_TYPE_UNKNOWN
        try {
            val activeNetInfo = connectivityManager.activeNetworkInfo ?: return NETWORK_TYPE_UNKNOWN
            when (activeNetInfo.type) {
                ConnectivityManager.TYPE_WIFI -> return NETWORK_TYPE_WIFI
                ConnectivityManager.TYPE_MOBILE -> {
                    val tm = telephonyManager(context)
                    val t = tm!!.networkType
                    when (t) {
                        TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE, TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT, TelephonyManager.NETWORK_TYPE_IDEN -> return NETWORK_TYPE_2G
                        TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0, TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA, TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA, TelephonyManager.NETWORK_TYPE_HSPAP, TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD -> return NETWORK_TYPE_3G
                        TelephonyManager.NETWORK_TYPE_LTE -> return NETWORK_TYPE_4G
                        else -> {}
                    }
                }
                else -> {}
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return NETWORK_TYPE_UNKNOWN
    }

    fun getNetTypeDes(context: Context): String {
        return when (getNetworkType(context)) {
            NETWORK_TYPE_WIFI -> "wifi"
            NETWORK_TYPE_2G -> "2G"
            NETWORK_TYPE_3G -> "3G"
            NETWORK_TYPE_4G -> "4G"
            else -> "unknown"
        }
    }

    /**
     * 判断外网是否可用（常用于wifi已经连接但是无法访问外网的情况），耗时操作，不应该放在主线程
     * @param second 超时时间设置
     * @return 0：网络正常连接  1：需要网页认证的wifi 2：网络不可用的状态
     */
    fun isNetworkOnlineByPing(second: Int): Int {
        try {
            val ip = "www.baidu.com" // 除非服务器挂了，否则用这个应该没问题~
            //ping -c 3 -w 2  中  ，-c 是指ping的次数 3是指ping 3次 ，-w 2  以秒为单位指定超时间隔，是指超时时间为2秒
            val ipProcess = Runtime.getRuntime().exec("ping -c 3 -W $second $ip") //ping3次

            // 网络连接正常 ping 也是通的
            return ipProcess.waitFor()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return -1
    }

}