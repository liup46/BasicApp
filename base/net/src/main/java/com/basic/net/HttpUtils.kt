package com.basic.net

import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.nio.charset.Charset


/**
 * @author Peter Liu
 * @since 2023/3/20 23:08
 *
 */
object HttpUtils {
    val CHARSET_UTF8 = Charset.forName("UTF-8")

    fun getRequestStr(request: Request): String? {
        var requestStr: String? = null
        val requestBody = request.body
        if (requestBody != null) {
            val buffer = Buffer()
            try {
                requestBody.writeTo(buffer)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            requestStr = buffer.clone().readString(CHARSET_UTF8)
        }
        return requestStr
    }

    fun getResponseStr(response: Response): String? {
        var responseStr: String? = null
        val responseBody = response.body
        if (responseBody != null) {
            val source = responseBody.source()
            try {
                // Buffer the entire body.
                source.request(Long.MAX_VALUE)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            val buffer: Buffer = source.buffer()
            responseStr = buffer.clone().readString(CHARSET_UTF8)
        }
        return responseStr
    }
}



