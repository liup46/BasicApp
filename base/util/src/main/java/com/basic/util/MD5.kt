package com.basic.util

import java.io.Closeable
import java.io.File
import java.io.IOException
import java.io.RandomAccessFile
import java.math.BigInteger
import java.security.MessageDigest

/**
 * @author Peter Liu
 * @since 2023/5/6 19:57
 *
 */
object Md5 {
    /**
     * 获取 文件的md5
     * 计算方式性能优化
     * 参考：https://juejin.im/post/583e2172128fe1006bf66bc8#heading-9
     */
    fun md5File(file: File?): String {
        val messageDigest: MessageDigest
        var randomAccessFile: RandomAccessFile? = null
        return try {
            messageDigest = MessageDigest.getInstance("MD5")
            randomAccessFile = RandomAccessFile(file, "r")
            val bytes = ByteArray(2 * 1024 * 1024)
            var len = 0
            while (randomAccessFile.read(bytes).also { len = it } != -1) {
                messageDigest.update(bytes, 0, len)
            }
            val bigInt = BigInteger(1, messageDigest.digest())
            val md5: StringBuilder = StringBuilder(bigInt.toString(16))
            while (md5.length < 32) {
                md5.insert(0, "0")
            }
            md5.toString()
        } catch (e: Exception) {
            throw RuntimeException(e)
        } finally {
            closeQuietly(randomAccessFile)
        }
    }

    fun closeQuietly(closeable: Closeable?) {
        try {
            if (closeable != null) {
                closeable.close()
            }
        } catch (ioe: IOException) {
            // ignore
        }
    }
}