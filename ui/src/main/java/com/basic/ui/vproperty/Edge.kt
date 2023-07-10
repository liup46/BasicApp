package com.basic.ui.vproperty

/**
 * @author Peter Liu
 * @since 2023/7/7 23:28
 *
 */

data class Edge(
    val left: Int? = null,
    val top: Int? = null,
    val right: Int? = null,
    val bottom: Int? = null
) {
    constructor(leftRight: Int? = null, topBottom: Int? = null) :this(leftRight, topBottom, leftRight, topBottom)
    constructor(all:Int):this(all, all,all,all)
}