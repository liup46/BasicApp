package com.basic.util

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.reflect.Type


/**
 * @author Peter Liu
 * @since 2023/3/21 00:17
 *
 */
object Json {

    private var gson: Gson

    init {
        val gsonBuilder = GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss")
        gsonBuilder.disableHtmlEscaping() //禁止将部分特殊字符转义为unicode编码
        gson = gsonBuilder.create()
    }

    fun <T> toJson(t: T?): String? {
        if (t == null) {
            return null
        }
        return gson.toJson(t)
    }

    /**
     * 将对象转为jsonObject
     */
    fun toJsonObject(o: Any?): JSONObject {
        var jsonObject = JSONObject()
        if (o != null) {
            try {
                val str = gson.toJson(o)
                jsonObject = JSONObject(str)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        return jsonObject
    }

    /**
     * JSON反序列化
     */
    fun <V> from(inputStream: InputStream?, type: Class<V>?): V? {
        if (inputStream == null) {
            return null
        }
        return safeCall {
            val reader = JsonReader(InputStreamReader(inputStream))
            gson.fromJson(reader, type)
        }
    }

    /**
     * JSON反序列化（List）
     */
    fun <V> fromList(json: String?, type: Class<V>?): List<V>? {
        if(json.isNullOrBlank()){
            return null
        }
        return safeCall {
            val typeToken = TypeToken.getParameterized(
                ArrayList::class.java, type
            ) as TypeToken<List<V>>
            gson.fromJson(json, typeToken.type)
        }
    }

    fun <T> fromJson(json: String?, type: Type?): T? {
        return if (json.isNullOrBlank()) {
            null
        } else {
            safeCall {
                gson.fromJson(json, type)
            }
        }
    }

    fun <T> fromJson(json: String?, typeToken: TypeToken<T>): T? {
        return fromJson(json, typeToken.type)
    }

    fun <T> getValue(json: String?, key: String?, defaultValue: T? = null): T? {
        return try {
            val jsonObject = JSONObject(json)
            if (!jsonObject.has(key)) {
                defaultValue
            } else jsonObject[key] as? T
        } catch (e: JSONException) {
            defaultValue
        }
    }

}

