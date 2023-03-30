package com.basic.util

import java.io.PrintWriter
import java.io.StringWriter

/**
 * @author Peter Liu
 * @since 2023/3/19 12:49
 *
 */
fun Throwable.traceMessage(): String {
    val stringWriter = StringWriter()
    this.writeTrace(PrintWriter(stringWriter))
    return stringWriter.toString()
}

fun Throwable.writeTrace(printWriter: PrintWriter) {
    if (this.cause != null && this.cause != this) {
        this.cause!!.writeTrace(printWriter)
    }
    this.printStackTrace(printWriter)
}