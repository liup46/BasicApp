package com.basic.env

import android.content.Context

//todo will imp this files

/**
 * @author Peter Liu
 * @since 2023/3/24 01:47
 *
 */
interface HttpConfig {
    var HttpHost: String
    var LogUploadPath: String

}

interface DeviceConfig {
    var deviceId: String
    var defaultChannel: String

}

interface UserConfig {
    var accessToken: String
    var userId:String

    //when debug return faslse
    open fun enableEncryptRequest():Boolean{
        return !App.isDev()
    }

    //验签
    fun sign(context: Context, sign: String): String

    fun decrypt(byteArray: ByteArray): String

    fun encrypt(data: String): ByteArray
}
