package com.basic.net.interceptor

import android.os.Build
import android.util.Base64
import com.basic.env.App
import com.basic.net.HttpUtils
import com.basic.net.NetworkUtils
import com.basic.util.AppUtils
import com.basic.util.DeviceUtils
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okio.Buffer
import java.util.*
import kotlin.collections.HashMap


/**
 * @author Peter Liu
 * @since 2023/3/24 00:13
 *
 */
class HeadInterceptor : Interceptor {
    companion object{
        const val TRACE_ID = "Trace_Id"
        internal val UA  by lazy(LazyThreadSafetyMode.NONE) { getUserAgent() }

        /**
         * mapi/1.0 (Android 7.1; com.basic.app 1.0.1; xiaomi; appstore)
         */
        private fun getUserAgent(): String {
            val sb = StringBuffer()
            sb.append("mapi/1.0 (Android ")
                .append(Build.VERSION.SDK_INT)
                .append(";")
                .append(App.getContext().packageName)
                .append(" ")
                .append(AppUtils.getVersionName(App.getContext()))
                .append(";")
                .append(DeviceUtils.getPhoneBrand())
                .append(";")
                .append(App.getChannelId())
                .append(";")
                .append(AppUtils.getVersionCode(App.getContext()))
                .append(")")
            return sb.toString()
        }
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val body = request.body
        var bodyJson = ""
        if (body != null) {
            val buffer = Buffer()
            body.writeTo(buffer)
            bodyJson = buffer.clone().readString(HttpUtils.CHARSET_UTF8)
        }
        val baseBody = Base64.encodeToString(bodyJson.toByteArray(), Base64.NO_WRAP)
        val signBody: String = App.getUserConfig().sign(App.getContext(),baseBody)
        val headerMap = generateHeaderMap(signBody)
        val builder: Request.Builder = request.newBuilder()
        for ((key, value) in headerMap) {
            try {
                builder.addHeader(key, value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return chain.proceed(builder.build())
    }

    private fun generateHeaderMap(bodySign: String): HashMap<String, String> {
        val headerMap: HashMap<String, String> = HashMap()
        //1 设备ID
        headerMap["X-Udid"] = App.getDeviceConfig().deviceId
        //2 客户端时间戳(UTC:毫秒)
        headerMap["X-Client-Time"] = System.currentTimeMillis().toString()
        //3 Body内容
        headerMap["X-Sign"] = bodySign
        //4 登陆态
        val accessToken: String = App.getUserConfig().accessToken
        headerMap["X-AccessToken"] = accessToken
        //6 网络制式 wifi,2G,3G,4G,5G
        headerMap["X-NetWork"] = NetworkUtils.getNetTypeDes(App.getContext())
        //7 用户
        headerMap["X-User-Agent"] = UA
        //8 以上字段的MD5值，用户验证Header内容
        headerMap["X-Authentication"] = App.getUserConfig().sign(App.getContext(),headerMap.toString())
        //9 trace id
        headerMap[TRACE_ID] = UUID.randomUUID().toString().replace("-", "")
        return headerMap
    }

}