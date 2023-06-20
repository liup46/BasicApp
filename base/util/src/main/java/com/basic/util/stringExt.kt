package com.basic.util

import android.graphics.Color
import android.util.Base64
import androidx.annotation.ColorInt
import java.io.ByteArrayOutputStream
import java.nio.charset.Charset
import java.util.zip.GZIPOutputStream

/**
 * @author Peter Liu
 * @since 2023/3/24 00:57
 *
 */

fun String?.nullEmpty(default:String):String{
    return if(this.isNullOrBlank()){
        default
    }else{
        this
    }
}

fun String?.gzipBytes(charset: Charset = Charsets.UTF_8): ByteArray? {
    if (this.isNullOrBlank()) {
        return ByteArray(0)
    }
    val out = ByteArrayOutputStream()
    var gzip: GZIPOutputStream? = null
    val encodedBuf: ByteArray?
    try {
        gzip = GZIPOutputStream(out)
        gzip.write(this.toByteArray(charset))
        gzip.flush()
        gzip.close()
        out.close()
        encodedBuf = out.toByteArray()
    } catch (e: Throwable) {
        e.printStackTrace()
        return ByteArray(0)
    } finally {
        try {
            out.close()
            gzip?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    return encodedBuf
}

@JvmOverloads
fun <T> T.encodeBase64(flags: Int = Base64.NO_WRAP): String {
    return try {
        Base64.encodeToString(this?.toString().orEmpty().toByteArray(), flags)
    } catch (e: java.lang.Exception) {
        ""
    }
}

@JvmOverloads
fun String?.decodeBase64(flags: Int = Base64.NO_WRAP): String {
    return try {
        String(Base64.decode(this.orEmpty(), flags))
    } catch (e: java.lang.Exception) {
        ""
    }
}

fun String?.toColor(@ColorInt defaultColor:Int?= null):Int?{
    if (this.isNullOrBlank()) {
        return defaultColor
    }
    try {
        return if (this[0] == '#')
            Color.parseColor(this)
        else
            Color.parseColor("#$this")
    } catch (ignore: java.lang.Exception) {
    }
    return defaultColor
}

