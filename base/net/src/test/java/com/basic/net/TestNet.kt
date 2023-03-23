package com.basic.net

import android.util.Range
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.Test

/**
 * @author Peter Liu
 * @since 2023/3/21 22:13
 *
 */
class TestNet {

    @Test
    fun test() {
        GlobalScope.launch {
            safeRequest {
                HttpService.get<String>(path = "/asdb")
                HttpService.get<String>("", "")
                HttpService.post<String>(path = "111", body = "{}")
            }.onData {

            }.onFailed(true)
        }
    }

    @Test
    fun testWithContext(){
        println("000:" + Thread.currentThread().id + " ," + Thread.currentThread())

        runBlocking {
            println("111:" + Thread.currentThread().id + " ," + Thread.currentThread())
            val rel =  withContext(Dispatchers.IO) {
                println("22:" + Thread.currentThread().id + " ," + Thread.currentThread())
                "abc"
            }
            println("333:" + Thread.currentThread().id + " ," + Thread.currentThread())
            println("333:"+rel)
        }
    }

    @Test
    fun testFlow() {
        println("000:" + Thread.currentThread().id + " ," + Thread.currentThread())
        runBlocking {
            flow<String> {
                println("111:" + Thread.currentThread().id + " ," + Thread.currentThread())
                for (i in 1..10) {
                    if (i == 5) {
                        throw java.lang.IllegalArgumentException("error $i")
                    }
                    emit("$i")
                }
            }.flowOn(Dispatchers.IO)
                .onCompletion {
                    println("222:" + Thread.currentThread().id + " ," + Thread.currentThread())
                    println(it)
                }.catch {
                    println("444:" + Thread.currentThread().id + " ," + Thread.currentThread())
                    println(it)
                }.collectLatest {
                    println("333: " + Thread.currentThread().id + " ," + Thread.currentThread())
                    println("333: " + it)
                }
//                .collect {
//                    println("333: " + Thread.currentThread().id + " ," + Thread.currentThread())
//                    println("333: " + it)
//                }
        }
    }


}