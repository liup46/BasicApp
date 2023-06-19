package com.basic.util

import android.annotation.TargetApi
import android.app.AppOpsManager
import android.content.Context
import android.content.res.Configuration
import android.os.Binder
import android.os.Build
import android.os.Environment
import androidx.annotation.Nullable
import java.io.File
import java.io.FileInputStream
import java.lang.reflect.Method
import java.util.*
import java.util.regex.Pattern

/**
 * @author: Peter Liu
 * @date: 2022/11/9
 *
 */
object BrandUtils {
    private const val TAG = "DeviceHelper"
    private const val KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name"
    private const val KEY_FLYME_VERSION_NAME = "ro.build.display.id"
    private const val FLYME = "flyme"
    private const val ZTEC2016 = "zte c2016"

    /**
     * 判断是否为 ZUK Z1 和 ZTK C2016。
     * 两台设备的系统虽然为 android 6.0，但不支持状态栏icon颜色改变，因此经常需要对它们进行额外判断。
     */
    val isZUKZ1 =  Build.MODEL != null && Build.MODEL.toLowerCase().contains("zuk z1")

    private const val ESSENTIAL = "essential"
    private val MEIZUBOARD = arrayOf("m9", "M9", "mx", "MX")
    private var sMiuiVersionName: String? = null
    private var sFlymeVersionName: String? = null
    private var sIsTabletChecked = false
    private var sIsTabletValue = false
    private val BRAND = Build.BRAND.toLowerCase()

    init {
        val properties = Properties()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            // android 8.0，读取 /system/uild.prop 会报 permission denied
            FileInputStream(File(Environment.getRootDirectory(), "build.prop")).use {
                properties.load(it)
            }
        }
        try {
            val clzSystemProperties = Class.forName("android.os.SystemProperties")
            val getMethod: Method = clzSystemProperties.getDeclaredMethod(
                "get",
                String::class.java
            )
            // miui
            sMiuiVersionName = getLowerCaseName(
                properties,
                getMethod,
                KEY_MIUI_VERSION_NAME
            )
            //flyme
            sFlymeVersionName = getLowerCaseName(
                properties,
                getMethod,
                KEY_FLYME_VERSION_NAME
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun _isTablet(context: Context): Boolean {
        return context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK >=
            Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    /**
     * 判断是否为平板设备
     */
    fun isTablet(context: Context): Boolean {
        if (sIsTabletChecked) {
            return sIsTabletValue
        }
        sIsTabletValue = _isTablet(context)
        sIsTabletChecked = true
        return sIsTabletValue
    }

    /**
     * 判断是否是flyme系统
     */
    val isFlyme: Boolean
        get() = !sFlymeVersionName.isNullOrBlank() && sFlymeVersionName!!.contains(FLYME)

    /**
     * 判断是否是MIUI系统
     */
    val isMIUI: Boolean = !sMiuiVersionName.isNullOrBlank()
    val isMIUIV5: Boolean
        get() = "v5" == sMiuiVersionName
    val isMIUIV6: Boolean
        get() = "v6" == sMiuiVersionName
    val isMIUIV7: Boolean
        get() = "v7" == sMiuiVersionName
    val isMIUIV8: Boolean
        get() = "v8" == sMiuiVersionName
    val isMIUIV9: Boolean
        get() = "v9" == sMiuiVersionName
    val isFlymeLowerThan8: Boolean
        get() {
            var isLower = false
            if (sFlymeVersionName != null && sFlymeVersionName != "") {
                val pattern = Pattern.compile("(\\d+\\.){2}\\d")
                val matcher = pattern.matcher(sFlymeVersionName)
                if (matcher.find()) {
                    val versionString = matcher.group()
                    if (versionString != null && versionString != "") {
                        val version = versionString.split("\\.").toTypedArray()
                        if (version.size >= 1) {
                            if (version[0].toInt() < 8) {
                                isLower = true
                            }
                        }
                    }
                }
            }
            return isMeizu && isLower
        }

    //查不到默认高于5.2.4
    val isFlymeVersionHigher5_2_4: Boolean
        get() {
            //查不到默认高于5.2.4
            var isHigher = true
            try {
                if (sFlymeVersionName != null && sFlymeVersionName != "") {
                    val pattern = Pattern.compile("(\\d+\\.){2}\\d")
                    val matcher = pattern.matcher(sFlymeVersionName)
                    if (matcher.find()) {
                        val versionString = matcher.group()
                        if (versionString != null && versionString != "") {
                            val version = versionString.split("\\.").toTypedArray()
                            if (version.size == 3) {
                                if (version[0].toInt() < 5) {
                                    isHigher = false
                                } else if (version[0].toInt() > 5) {
                                    isHigher = true
                                } else {
                                    if (version[1].toInt() < 2) {
                                        isHigher = false
                                    } else if (version[1].toInt() > 2) {
                                        isHigher = true
                                    } else {
                                        if (version[2].toInt() < 4) {
                                            isHigher = false
                                        } else if (version[2].toInt() >= 5) {
                                            isHigher = true
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return isMeizu && isHigher
        }
    val isMeizu: Boolean
        get() = isPhone(MEIZUBOARD) || isFlyme

    /**
     * 判断是否为小米
     * https://dev.mi.com/doc/?p=254
     */
    val isXiaomi: Boolean
        get() = Build.MANUFACTURER.toLowerCase() == "xiaomi"
    val isVivo: Boolean
        get() = BRAND.contains("vivo") || BRAND.contains("bbk")
    val isOppo: Boolean
        get() = BRAND.contains("oppo")
    val isHuawei: Boolean
        get() = BRAND.contains("huawei") || BRAND.contains("honor")
    val isEssentialPhone: Boolean
        get() = BRAND.contains("essential")
    val isZTKC2016: Boolean
        get() {
            val board = Build.MODEL
            return board != null && board.toLowerCase().contains(ZTEC2016)
        }

    private fun isPhone(boards: Array<String>): Boolean {
        val board = Build.BOARD ?: return false
        for (board1 in boards) {
            if (board == board1) {
                return true
            }
        }
        return false
    }

    /**
     * 判断悬浮窗权限（目前主要用户魅族与小米的检测）。
     */
    fun isFloatWindowOpAllowed(context: Context): Boolean {
        val version = Build.VERSION.SDK_INT
        return if (version >= 19) {
            checkOp(context, 24) // 24 是AppOpsManager.OP_SYSTEM_ALERT_WINDOW 的值，该值无法直接访问
        } else {
            try {
                context.applicationInfo.flags and 1 shl 27 == 1 shl 27
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }
    }

    @TargetApi(19)
    private fun checkOp(context: Context, op: Int): Boolean {
        val version = Build.VERSION.SDK_INT
        if (version >= Build.VERSION_CODES.KITKAT) {
            val manager = context!!.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            try {
                val method = manager.javaClass.getDeclaredMethod(
                    "checkOp",
                    Int::class.javaPrimitiveType,
                    Int::class.javaPrimitiveType,
                    String::class.java
                )
                val property = method.invoke(
                    manager, op,
                    Binder.getCallingUid(), context.packageName
                ) as Int
                return AppOpsManager.MODE_ALLOWED == property
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return false
    }

    @Nullable
    private fun getLowerCaseName(p: Properties, get: Method, key: String): String? {
        var name = p.getProperty(key)
        if (name == null) {
            try {
                name = get.invoke(null, key) as String
            } catch (ignored: Exception) {
            }
        }
        if (name != null) name = name.toLowerCase()
        return name
    }
}