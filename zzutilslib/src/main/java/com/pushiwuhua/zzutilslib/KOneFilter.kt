@file:Suppress("unused")

package com.pushiwuhua.zzutilslib

import android.util.ArrayMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 过滤器, 最初设计过滤重复数值
 * wzz created at 2019/7/24 16:22
 * wzz updated at 2019/10/11 11:34
 */
class KOneFilter<T> {
    private val mapValue: ArrayMap<T, Job?> = ArrayMap()

    /**
     * 过滤输出,重复值空滤，返回空
     * @param value 过滤值
     */
    fun out(value: T): T? {
        if (!mapValue.containsKey(value)) {
            mapValue[value] = null
            return value
        }
        return null
    }

    /**
     * 过滤输出， 过滤时间内返回空，超时时间后进入下一周期
     * @param value 过滤值
     * @param overTimeMSecs 超时时间,过滤的有效限制时间
     */
    fun out(scope: CoroutineScope, value: T, overTimeMSecs: Long): T? {
        val getNewJob: (T) -> Job = {
            scope.launch {
                delay(overTimeMSecs)
//                Log.v("wzz", "KOneFilter out 输出删除:$it")
                mapValue.remove(it)
            }
        }

        return if (!mapValue.containsKey(value)) {
            mapValue[value] = getNewJob(value)
            value
        } else {
            null
        }
    }

    /**
     * 过滤输出, 在超时的时间范围内输出最后一次的值
     * @param value 过滤值
     * @param overTimeMSecs 超时时间,过滤的有效限制时间
     * @block 输出返回值的触发方法
     */
    fun delayOut(scope: CoroutineScope, value: T, overTimeMSecs: Long, block: (T) -> Unit) {
        val getNewJob: (T) -> Job = {
            scope.launch {
                delay(overTimeMSecs)
//                Log.i("wzz", "KOneFilter delayOut 输出删除:$it")
                block.invoke(it)
                mapValue.remove(it)
            }
        }

        if (!mapValue.containsKey(value)) {
            mapValue[value] = getNewJob(value)
        }
    }
}