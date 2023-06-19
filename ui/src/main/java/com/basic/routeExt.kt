package com.peter.vunit.router

import androidx.fragment.app.Fragment

/**
 * @author Peter Liu
 * @since 2022/11/15 00:43
 *
 */

fun String?.nav():Fragment? {
    if (this.isNullOrBlank()) {
        return null
    }
    return Fragment()
}