package com.basic.net

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.junit.Test
import java.util.*

/**
 * @author Peter Liu
 * @since 2023/3/21 22:13
 *
 */
class TestNet {


    @Test
    fun findTheStartPosition(){

        println( findTheStartPosition(intArrayOf(50,50,2,3),2));

    }


    fun findTheStartPosition(scores: IntArray, K: Int): Int {
        // write code here
        val temArray = Arrays.copyOfRange(scores, 0, scores.size)
        val mid = temArray[temArray.size / 2]
        println("mid"+mid)
        var delta = Int.MAX_VALUE
        var index = 0
        for (i in 0..scores.size - K) {
            var n = scores[i]
            for (j in i + 1..i + K - 1) {
                n -= scores[j]
            }
            val temp = Math.abs(mid - n)
            println("temp"+temp)
            if (temp <= delta) {
                delta = temp
                index = i
            }
        }
        return index
    }



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