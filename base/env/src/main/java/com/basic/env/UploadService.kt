package com.basic.env

import java.io.File

/**
 * @author Peter Liu
 * @since 2023/3/31 00:28
 *
 */
interface UploadService {

    fun uploadCrash(file:File, onResult:(Boolean,String)->Unit)


    fun uploadLogger(file: File, onResult: (Boolean, String) -> Unit)
}