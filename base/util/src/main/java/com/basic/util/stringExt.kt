package com.basic.util

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