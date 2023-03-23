package com.basic.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.Signature
import android.os.Build
import java.net.URLEncoder
import java.security.MessageDigest
import java.util.*


/**
 * @author Peter Liu
 * @since 2023/3/19 11:43
 *
 */

fun Context.isDebug(): Boolean {
    return AppUtils.isDebug(context = this)
}

object AppUtils {
    private var appVersionName: String? = null
    private var appVersionCode: Int? = null
    private var appSign: String? = null

    fun isDebug(context: Context):Boolean{
        return context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE !== 0
    }

    fun getVersionName(context: Context): String? {
        if (appVersionName == null) {
            appVersionName = safeCall("") {
                val application = context.applicationContext
                val packageManager: PackageManager = application.packageManager
                val packInfo: PackageInfo =
                    packageManager.getPackageInfo(application.packageName, 0)
                packInfo.versionName
            }
        }
        return appVersionName
    }

    fun getVersionCode(context: Context): Int {
        if (appVersionCode == null) {
            appVersionCode = safeCall(0) {
                val application = context.applicationContext
                val packageManager = application.packageManager
                val packInfo: PackageInfo = packageManager.getPackageInfo(application.packageName, 0)
                packInfo.versionCode
            }
        }
        return appVersionCode!!
    }


    /**
     * 获取App签名
     */
    fun getAppSignature(application: Context): String? {
        if(appSign== null){
            appSign = safeCall {
                var packageInfo: PackageInfo? = null
                var signatures: Array<Signature>? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    packageInfo = application.packageManager.getPackageInfo(
                        application.packageName,
                        PackageManager.GET_SIGNING_CERTIFICATES
                    )
                    if (packageInfo != null) {
                        val signingInfo = packageInfo.signingInfo
                        if (signingInfo != null) {
                            signatures = if (signingInfo.hasMultipleSigners()) {
                                //多个签名时，这里逻辑需要调整
                                signingInfo.apkContentsSigners
                            } else {
                                signingInfo.signingCertificateHistory
                            }
                        }
                    }
                } else {
                    packageInfo = application.packageManager.getPackageInfo(
                        application.packageName,
                        PackageManager.GET_SIGNATURES
                    )
                    if (packageInfo != null) {
                        signatures = packageInfo.signatures
                    }
                }
                if (signatures == null || signatures.isEmpty()) {
                    null
                }else{
                    val md5StrBuff = StringBuilder()
                    val messageDigest = MessageDigest.getInstance("MD5")
                    messageDigest.reset()
                    messageDigest.update(signatures[0].toByteArray())
                    val byteArray = messageDigest.digest()
                    for (i in byteArray.indices) {
                        if (Integer.toHexString(0xFF and byteArray[i].toInt()).length == 1) {
                            md5StrBuff.append("0")
                                .append(Integer.toHexString(0xFF and byteArray[i].toInt()))
                        } else {
                            md5StrBuff.append(Integer.toHexString(0xFF and byteArray[i].toInt()))
                        }
                    }
                    md5StrBuff.toString().toLowerCase(Locale.CHINA)
                }
            }
        }
        return appSign
    }

}



