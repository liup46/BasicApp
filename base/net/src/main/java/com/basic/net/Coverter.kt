package com.basic.net

import com.google.gson.Gson
import com.google.gson.JsonIOException
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import okio.Buffer
import java.io.File
import java.io.OutputStreamWriter

/**
 * @author Peter Liu
 * @since 2023/3/22 22:17
 *
 */
object Coverter {
    private val MEDIA_TYPE_JSON = "application/json; charset=UTF-8".toMediaType()
    private val MEDIA_TYPE_STEAM = "application/octet-stream".toMediaType()
    private val gson = Gson()
    private val responseClass = ApiResponse::class.java

    fun <T> covertResponseBody(data: ResponseBody): ApiResponse<T>? {
        val adapter: TypeAdapter<*> = gson.getAdapter(responseClass)
        return data.use {
            val jsonReader = gson.newJsonReader(data.charStream())
            val result = adapter.read(jsonReader) as? ApiResponse<T>
            if (jsonReader.peek() != JsonToken.END_DOCUMENT) {
                throw JsonIOException("JSON document was not fully consumed.")
            }
            result
        }
    }

    fun convertRequestBody(data: Any?): RequestBody {
        if (data == null) {
            return "".toRequestBody(MEDIA_TYPE_JSON)
        }
        val adapter = gson.getAdapter(data::class.java) as TypeAdapter<Any>
        val buffer = Buffer()
        val writer = OutputStreamWriter(buffer.outputStream(), Charsets.UTF_8)
        val jsonWriter: JsonWriter = gson.newJsonWriter(writer)
        adapter.write(jsonWriter, data)
        jsonWriter.close()
        return buffer.readByteString().toRequestBody(MEDIA_TYPE_JSON)
    }

    fun convertRequestBody(fileMaps: Map<String, File>): RequestBody {
        val multiBody = MultipartBody.Builder()
        for ((name, file) in fileMaps) {
            val requestBody = file.asRequestBody(MEDIA_TYPE_STEAM)
            val part = MultipartBody.Part.createFormData(name, file.name, requestBody)
            multiBody.addPart(part)
        }
        return multiBody.build()
    }


}