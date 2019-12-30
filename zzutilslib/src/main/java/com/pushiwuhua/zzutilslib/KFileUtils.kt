@file:Suppress("MemberVisibilityCanBePrivate", "unused")

package com.pushiwuhua.zzutilslib

import android.graphics.Bitmap
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.PrintWriter

/**
 * 文件工具类, Kotlin特性
 * wzz created at 2019/10/30 15:19
 */
object KFileUtils {

    /**
     * Checks if is sd card available.检查SD卡是否可用
     */
    val isSdCardAvailable: Boolean
        get() = Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED

    /**
     * Gets the SD root file.获取SD卡根目录
     */
    val sdRootFile: File?
        get() = if (isSdCardAvailable) {
            Environment.getExternalStorageDirectory()
        } else {
            null
        }

    /**
     * 读取txt文件的内容
     *
     * @param filePath 想要读取的文件对象
     * @return 返回文件内容
     */
    fun txt2String(filePath: String): String {
        return try {
            File(filePath).readText()
        } catch (e: Exception) {
            ""
        }
    }


    /**
     * 写入TXT文件
     */
    fun writeTxtFile(content: String, filePath: String): Boolean {
        return try {
            PrintWriter(filePath).append(content).close()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 判断文件是否存在
     */
    fun isFileExist(fileDirectory: String, fileName: String): File? {
        val file = File("$fileDirectory/$fileName")
        try {
            if (!file.exists()) {
                return null
            }
        } catch (e: Exception) {
            return null
        }

        return file
    }

    /**
     * 删除文件
     */
    fun deleteFile(filePath: String) {
        try {
            // 找到文件所在的路径并删除该文件
            val file = File(filePath)
            file.delete()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /*
     * 获取不带扩展名的文件名
     * */
    fun getFileNameNoEx(filename: String): String {
        return File(filename).nameWithoutExtension
    }

    /**
     * 保存图片
     */
    fun saveBitmap(file: File, bitmap: Bitmap): Boolean {
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                out?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return false
    }

    /**
     * 复制文件
     */
    fun copyFile(oldPath: String, newPath: String): Boolean {
        return try {
            File(oldPath).copyTo(File(newPath))
            true
        } catch (e: Exception) {
            false
        }
    }
}
