package com.basic.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts

/**
 *
 * @author : thr
 * @date: 2023/6/19
 */


fun ComponentActivity.startActivityForResultOK(intent: Intent, onOk: (Intent?) -> Unit) {
    startActivityForResult(intent) {
        if (it.resultCode == Activity.RESULT_OK) {
            onOk.invoke(it.data)
        }
    }
}

fun ComponentActivity.startActivityForResult(
    intent: Intent,
    oResult: (ActivityResult) -> Unit
) {
    val key = getStartActivityForResultKey(intent)
    if (key.isEmpty()) {
        return
    }
    activityResultRegistry.register(
        key,
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        oResult.invoke(result)
    }.launch(intent)
}

internal fun getStartActivityForResultKey(intent: Intent): String {
    val key = if (!intent.action.isNullOrEmpty()) {
        intent.action
    } else if (intent.component != null) {
        intent.component.toString()
    } else if (intent.data != null) {
        intent.data.toString()
    } else {
        ""
    }
    return "startActivityForResult#$key"
}