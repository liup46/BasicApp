package com.basic.net.interceptor

import android.util.Log
import com.basic.env.App
import com.basic.log.Logger
import com.basic.net.HttpException
import com.basic.net.HttpUtils
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject


/**
 * @author Peter Liu
 * @since 2023/3/20 22:54
 *
 */
class LogInterceptor : Interceptor {
    companion object {
        const val TAG = "http"

        internal fun loggerError(request: Request, e: Exception) {
            val urlStr = request.url.toString()
            val requestStr = HttpUtils.getRequestStr(request)
            val builder = "url : " + urlStr +
                    ",header : " +
                    request.headers.toString() +
                    ",requestBody : " +
                    requestStr +
                    ",response error: " +
                    e.message
            Logger.e(TAG, builder, e, type = Logger.TYPE_HTTP)
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val starTime = System.currentTimeMillis()
        val request: Request = chain.request()
        val requestBuilder = request.newBuilder()
        val response: Response?
        try {
            response = chain.proceed(requestBuilder.build())
            val endTime = System.currentTimeMillis()
            if (response.networkResponse != null) {
                logger(request, response, starTime, endTime)
                printLog(request, response, starTime, endTime)
            }
        } catch (e: Exception) {
            val apiException = HttpException.catchException(e)
            loggerError(request, apiException)
            throw apiException
        }
        return response
    }

    private fun logger(request: Request, response: Response, starTime: Long, endTime: Long) {
        try {
            val jsonObject = JSONObject()
            jsonObject.put("url", request.url.toString())
            jsonObject.put("header", request.headers.toString())
            jsonObject.put("tid", request.header(HeadInterceptor.TRACE_ID))
            jsonObject.put("requestBody", HttpUtils.getRequestStr(request))
            jsonObject.put("responseCode", response.code)
            jsonObject.put("response", HttpUtils.getResponseStr(response))
            jsonObject.put("starTime", starTime)
            jsonObject.put("endTime", endTime)
            jsonObject.put("costTime", endTime - starTime)
            Logger.i(TAG, jsonObject.toString(), type = Logger.TYPE_HTTP)
        } catch (e: Exception) {
            loggerError(request, e)
        }
    }

    private fun printLog(
        request: Request,
        response: Response,
        starTime: Long,
        endTime: Long
    ) {
        if (App.isDev()) {
            val urlStr = request.url.toString()
            val requestStr = HttpUtils.getRequestStr(request)
            val responseStr = HttpUtils.getResponseStr(response)
            val builder = """
            url: $urlStr
            header: ${request.headers}
            requestBody: $requestStr
            responseCode: ${response.code}
            response: $responseStr
            "starTime:" $starTime
            "cost:" ${endTime - starTime}
            """.trimIndent()
            Log.d(TAG, builder)
        }

    }
}