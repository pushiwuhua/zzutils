@file:Suppress("unused")

package com.pushiwuhua.zzutilslib

import android.util.Log
import okhttp3.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.URL


/**
 * 封装OkHttp的下载文件类
 * wzz created at 2020/1/3 14:16
 */
object KDownQk {
    private val okClient: OkHttpClient by lazy {
        OkHttpClient()
    }

    /**
     * 下载状态
     */
    interface Status {
        fun err(err: Throwable)
        fun finish()
        fun start()
        fun downing(percents: Float)
    }

    /**
     * 下载文件
     */
    fun downFile(url: URL, file: File, downStatus: Status): Call {
        val request: Request = Request.Builder().url(url).build()
        return okClient.newCall(request).also {
            it.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    downStatus.err(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    Log.i("wzz", "KDownQk onResponse 线程 ${Thread.currentThread()}")
                    writeFile(response, file, downStatus)
                }
            })
        }
    }

    /**
     * 写入文件
     */
    private fun writeFile(response: Response, outFile: File, downStatus: Status) {
//        Log.i("wzz", "KDownQk writeFile $outFile")
        val ins = response.body?.byteStream()
        val fos = FileOutputStream(outFile)
        kotlin.runCatching {
            val bytes = ByteArray(1024)
            var len = 0
            //获取下载的文件的大小
            val fileSize = response.body!!.contentLength()
            downStatus.start()
//            Log.i("wzz", "KDownQk writeFile 文件总大小 $fileSize")
            var sum: Long = 0
            var porSize: Int
            while (ins?.read(bytes).also { it?.let { len = it } } != -1) {
//                Log.i("wzz", "KDownQk writeFile 每一次读写 $len")
                fos.write(bytes, 0, len)
                sum += len.toLong()
                porSize = (sum * 1.0f / fileSize * 100).toInt()
//                Log.i("wzz", "KDownQk writeFile 百分比 $fileSize $sum $porSize")
                downStatus.downing(porSize.toFloat())
            }
        }.onFailure {
//            Log.i("wzz", "KDownQk writeFile 失败 $it")
            ins?.close()
            fos.close()
            downStatus.err(it)
        }
            .onSuccess {
//                Log.i("wzz", "KDownQk writeFile 成功")
//                Log.i("wzz", "KDownQk writeFile 线程 ${Thread.currentThread()}")
                ins?.close()
                fos.flush()
                fos.close()
                downStatus.finish()
            }
    }

}