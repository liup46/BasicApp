package com.basic.ui

import android.app.Activity
import android.content.Intent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner

/**
 *
 * @author : thr
 * @date: 2023/6/19
 */
fun Fragment.startActivityForResultOK(intent: Intent, onOk: (Intent?) -> Unit) {
    startActivityForResult(intent) {
        if (it.resultCode == Activity.RESULT_OK) {
            onOk.invoke(it.data)
        }
    }
}

fun Fragment.startActivityForResult(intent: Intent, oResult: (ActivityResult) -> Unit) {
    val activity = activity
    if (activity != null && !activity.isDestroyed && !activity.isFinishing) {
        val key = getStartActivityForResultKey(intent)
        if (key.isEmpty()) {
            return
        }
        val launcher = activity.activityResultRegistry.register(
            key,
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            oResult.invoke(result)
        }
        this.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                super.onDestroy(owner)
                launcher.unregister()
                lifecycle.removeObserver(this)
            }
        })
        launcher.launch(intent)
    }
}