package com.basic.net

import com.basic.env.App
import com.basic.net.interceptor.HeadInterceptor
import com.basic.net.interceptor.LogInterceptor
import kotlinx.coroutines.*
import okhttp3.*
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * @author Peter Liu
 * @since 2023/3/20 17:13
 *
 */
object HttpService {

    private const val TIME_OUT = 15000L
    private const val CACHE_SIZE = 20 * 1024 * 1024L //20M

    private var client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
        .readTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
        .writeTimeout(TIME_OUT, TimeUnit.MILLISECONDS)
        .cache(Cache(App.getContext().cacheDir, CACHE_SIZE))
        .addInterceptor(HeadInterceptor())
        .addInterceptor(LogInterceptor())
        .build()

    private fun newRequestBuilder(
        host: String,
        path: String,
        query: Map<String?, Any?>? = null
    ): Request.Builder {
        return Request.Builder().url(getFullUrl(host, path, query))
    }

    private suspend fun <T> executeCall(request: Request): ApiResponse<T>? {
        return suspendCancellableCoroutine { continuation ->
            val call = client.newCall(request)
            continuation.invokeOnCancellation {
                call.cancel()
            }
            try {
                val response = call.execute()
                val result = response.parseBody<T>()
                continuation.resume(result)
            } catch (e: Exception) {
                if (e !is HttpException) {
                    val httpException = HttpException.catchException(e)
                    LogInterceptor.loggerError(request, httpException)
                    continuation.resumeWithException(httpException)
                } else {
                    continuation.resumeWithException(e)
                }
            }
        }
    }

    private fun <T> Response.parseBody(): ApiResponse<T>? {
        val rawBody = this.body
        val code: Int = this.code
        if (code in 200..299) {
            return if (code == 204 || code == 205) {
                rawBody?.close()
                ApiResponse(null, this.code.toString(), this.message)
            } else {
                if (rawBody == null) {
                    ApiResponse(null, this.code.toString(), this.message)
                } else {
                    Coverter.covertResponseBody(rawBody)
                }
            }
        } else {
            return ApiResponse(null, this.code.toString(), this.message)
            //Do not throw any more, just return ApiResponse
//            throw HttpException(code, this.message)
        }
    }

    suspend fun <T> get(
        host: String = App.getUrlConfig().HttpHost,
        path: String,
        query: Map<String?, Any?>? = null,
    ): ApiResponse<T>? {
        return withContext(Dispatchers.IO) {
            val request = newRequestBuilder(host, path, query).get().build()
            executeCall(request)
        }
    }

    suspend fun <T> post(
        host: String = App.getUrlConfig().HttpHost,
        path: String,
        body: Map<String, Any?>,
        query: Map<String?, Any?>? = null
    ): ApiResponse<T>? {
        return withContext(Dispatchers.IO) {
            val requestBody = Coverter.convertRequestBody(body)
            val request = newRequestBuilder(host, path, query).post(requestBody).build()
            executeCall(request)
        }
    }

    suspend fun <T> post(
        host: String = App.getUrlConfig().HttpHost,
        path: String,
        body: Any?,
        query: Map<String?, Any?>? = null
    ): ApiResponse<T>? {
        return withContext(Dispatchers.IO) {
            val requestBody = Coverter.convertRequestBody(body)
            val request = newRequestBuilder(host, path, query).post(requestBody).build()
            executeCall(request)
        }
    }

    suspend fun <T> uploadFile(
        host: String = App.getUrlConfig().HttpHost,
        path: String,
        fileMaps: Map<String, File>
    ): ApiResponse<T>? {
        return withContext(Dispatchers.IO) {
            val multipartBody = Coverter.convertRequestBody(fileMaps)
            val request = newRequestBuilder(host, path).post(multipartBody).build()
            executeCall(request)
        }
    }

    private fun getFullUrl(
        host: String,
        path: String,
        query: Map<String?, Any?>? = null
    ): String {
        val url = StringBuilder(host)
        if (host.endsWith("/")) {
            if (path.startsWith("/")) {
                url.append(path.substring(1))
            } else {
                url.append(path)
            }
        } else {
            if (path.startsWith("/")) {
                url.append(path)
            } else {
                url.append("/$path")
            }
        }
        if (query != null && query.isNotEmpty()) {
            url.append("?")
            for ((key, value) in query) {
                if (value != null && !key.isNullOrBlank()) {
                    url.append("$key=$value&")
                }
            }
            url.dropLast(1)
        }
        return url.toString()
    }
}

