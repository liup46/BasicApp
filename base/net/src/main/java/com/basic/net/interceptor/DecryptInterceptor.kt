package com.basic.net.interceptor

import android.app.ActivityManager
import com.basic.env.App
import okhttp3.Interceptor
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.io.RandomAccessFile
import java.nio.ByteOrder

/**
 * @author Peter Liu
 * @since 2023/3/24 02:18
 *
 */
class DecryptInterceptor : Interceptor {
    companion object {
        /**
         * response是否加密
         */
        const val X_ENCRYPT = "X-Encrypt"

    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        val isEncrypt = response.header(X_ENCRYPT)
        var decryptResponse = response
        if (true.toString() == isEncrypt) {
            RandomAccessFile("","").channel
            ByteOrder.LITTLE_ENDIAN
            ActivityManager.ProcessErrorStateInfo.CRASHED
            val responseBody = response.body
            responseBody?.apply {
                val decryptResult = App.getUserConfig().decrypt(bytes())
                val decryptBody = decryptResult.toResponseBody(contentType())
                decryptResponse = response.newBuilder().body(decryptBody).build()
            }
        }
        return decryptResponse
    }

}