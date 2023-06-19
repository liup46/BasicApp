package com.basic.ui.observer

import android.content.Context
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.Objects
import kotlin.properties.Delegates
import kotlin.properties.ObservableProperty

/**
 * @author Peter Liu
 * @since 2023/6/18 00:36
 *
 */
class Te {

    var name: String? by lazy {  }
}


annotation class Observable

class View {
    val date = Te()


    fun render(context: Context, data: Te){
        val textView = TextView(context)
        val map = hashMapOf<String,String>(
            "s" to "fasd"
        )

        textView.text = date.name
        textView.visibility

        Object.
    }


    fun setState(data: Te){
        render(data = data)
    }
}

class ObserverData<T> : MutableLiveData<T>() {
    val observerFields = hashMapOf<String, MutableLiveData<*>>()

    private var isFirst = false;


    override fun setValue(value: T) {
        super.setValue(value)
        updateProperty(value)
    }


    private fun updateProperty(data: T) {
        if (data == null) {
            return
        }
        val fileds = data!!::class.java.declaredFields
        for (field in fileds) {
            if (field.isAnnotationPresent(Observable::class.java)) {
                val name = field.name
                val value = field.get(data)
                var liveData = observerFields[field.name]
                if (liveData == null) {
                    liveData = MutableLiveData<Any?>()
                    observerFields[name] = liveData
                }
                if (field.get(data) != liveData.value) {
                    liveData.value = value
                }
            }
        }
    }

    private fun <T> observable(t:T){


    }



}

