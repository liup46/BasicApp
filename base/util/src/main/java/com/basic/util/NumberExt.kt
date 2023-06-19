package com.basic.util

import java.text.DecimalFormat

/**
 * @author Peter Liu
 * @since 2023/6/8 15:23
 *
 */
@JvmOverloads
fun Int?.nullOr(dft: Int = 0) = this ?: dft

@JvmOverloads
fun Long?.nullOr(dft: Long = 0) = this ?: dft

@JvmOverloads
fun Float?.nullOr(dft: Float = 0F) = this ?: dft

@JvmOverloads
fun Double?.nullOr(dft: Double = 0.toDouble()) = this ?: dft

@JvmOverloads
fun Short?.nullOr(dft: Short = 0) = this ?: dft

@JvmOverloads
fun Byte?.nullOr(dft: Byte = 0) = this ?: dft

@JvmOverloads
fun Boolean?.nullOr(dft: Boolean = false) = this ?: dft
