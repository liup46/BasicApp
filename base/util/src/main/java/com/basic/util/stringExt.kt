package com.basic.util

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

