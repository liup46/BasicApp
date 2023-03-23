package com.basic.net.interceptor

import com.basic.env.App
import com.basic.net.HttpUtils
import okhttp3.Interceptor
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okio.Buffer

/**
 * @author Peter Liu
 * @since 2023/3/24 02:23
 *
 */
class EncryptInterceptor : Interceptor {
    companion object {
        /**
         * post method
         */
        const val HTTP_POST = "POST"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        //只有Host是网关域名或者Post请求走加解密逻辑
        if (request.method != HTTP_POST || !App.getUserConfig().enableEncryptRequest() || request.body == null) {
            return chain.proceed(request)
        }

        val encryptRequest = request.body!!.run {
            val buffer = Buffer()
            writeTo(buffer)
            val requestBodyJson = buffer.clone().readString(HttpUtils.CHARSET_UTF8)
            val encryptByteArray = App.getUserConfig().encrypt(requestBodyJson)
            val encryptBody = encryptByteArray.toRequestBody(contentType())
            request.newBuilder().post(encryptBody).build()
        }
        return chain.proceed(encryptRequest)
    }

}