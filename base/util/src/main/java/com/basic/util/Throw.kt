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
    this.printStackTrace(PrintWriter(stringWriter))
    return stringWriter.toString()
}