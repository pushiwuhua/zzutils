@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.pushiwuhua.zzutilslib

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.text.TextUtils
import android.util.Base64
import androidx.exifinterface.media.ExifInterface
import java.io.*
import java.util.*
import kotlin.math.max
import kotlin.math.min

/**
 * 图片工具类
 */
object KPicUtils {
    /**
     * 给定图片维持宽高比缩放后，截取正中间的正方形部分
     *
     * @param bitmap     原图
     * @param edgeLength 希望得到的正方形部分的边长
     * @return 缩放截取正中部分后的位图。
     */
    fun centerSquareScaleBitmap(bitmap: Bitmap?, edgeLength: Int): Bitmap? {
        if (null == bitmap || edgeLength <= 0) {
            return null
        }
        var result = bitmap
        val widthOrg = bitmap.width
        val heightOrg = bitmap.height
        if (widthOrg > edgeLength && heightOrg > edgeLength) {
            //压缩到一个最小长度是edgeLength的bitmap
            val longerEdge =
                edgeLength * max(widthOrg, heightOrg) / min(
                    widthOrg,
                    heightOrg
                )
            val scaledWidth = if (widthOrg > heightOrg) longerEdge else edgeLength
            val scaledHeight = if (widthOrg > heightOrg) edgeLength else longerEdge
            val scaledBitmap: Bitmap
            scaledBitmap = Bitmap.createScaledBitmap(bitmap, scaledWidth, scaledHeight, true)

            //从图中截取正中间的正方形部分。
            val xTopLeft = (scaledWidth - edgeLength) / 2
            val yTopLeft = (scaledHeight - edgeLength) / 2
            result = Bitmap.createBitmap(scaledBitmap, xTopLeft, yTopLeft, edgeLength, edgeLength)
            scaledBitmap.recycle()
        }
        return result
    }

    /**
     * 保存数据到文件
     *
     *
     * wzz created at 2016/5/11 15:23
     */
    fun saveByteArrToFile(data: ByteArray?, path: String) {
        if (data == null) {
            return
        }
        try {
            val file = File(path)
            if (!file.exists()) {
                file.parentFile?.mkdirs()
                file.createNewFile()
            }

            //建立输出字节流
            val fos = FileOutputStream(file)
            fos.write(data)
            fos.close() //关闭输出流
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 存储图片到路径
     *
     *
     * wzz created at 2016/5/11 16:21
     */
    fun saveBitmap(bm: Bitmap?, savePath: String) {
        if (TextUtils.isEmpty(savePath) || bm == null) {
            return
        }
        try {
            val file = File(savePath)
            if (!file.exists()) {
                file.parentFile?.mkdirs()
                file.createNewFile()
            }
            val out = FileOutputStream(savePath)
            bm.compress(Bitmap.CompressFormat.PNG, 100, out)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun saveBitmapQuality(bm: Bitmap?, quality: Int, savePath: String) {
        if (TextUtils.isEmpty(savePath) || bm == null) {
            return
        }
        try {
            val file = File(savePath)
            if (!file.exists()) {
                file.parentFile?.mkdirs()
                file.createNewFile()
            }
            val out = FileOutputStream(savePath)
            bm.compress(Bitmap.CompressFormat.JPEG, quality, out)
            println("file " + savePath + "output done.")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 对Bitmap进行缩放
     *
     *
     * wzz created at 2016/5/28 14:50
     */
    fun getBitmapZoom(bm: Bitmap, newWidth: Int, newHeight: Int): Bitmap {
        val width = bm.width
        val height = bm.height
        val scaleWidth = newWidth.toFloat() / width
        val scaleHeight = newHeight.toFloat() / height
        val matrix = Matrix()
        matrix.postScale(scaleWidth, scaleHeight)
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true)
    }

    /**
     * 将base64编码格式的图片存储
     * wzz created at 2018/6/4 15:14
     */
    fun saveBase64ToFile(
        base64Str: String?,
        saveDir: String,
        saveName: String,
        imageType: String
    ) {
        if (TextUtils.isEmpty(saveDir) || TextUtils.isEmpty(saveName) || TextUtils.isEmpty(base64Str) || TextUtils.isEmpty(
                imageType
            )
        ) {
            return
        }
        try {
            val bitmapArray: ByteArray = Base64.decode(base64Str, Base64.DEFAULT)
            saveByteArrToFile(
                bitmapArray,
                "$saveDir/$saveName.$imageType"
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 对Bitmap进行存储
     *
     *
     * wzz created at 2016/5/28 14:53
     */
    fun saveBitmapToFile(
        bm: Bitmap,
        saveDir: String,
        saveName: String,
        imageType: String
    ) {
        try {
            val fname = "$saveDir/$saveName.$imageType"
            val file = File(fname)
            if (!file.exists()) {
                file.parentFile?.mkdirs()
                file.createNewFile()
            }
            val baos = ByteArrayOutputStream()
            // 把压缩后的数据存放到baos中
            if (imageType.toLowerCase(Locale.ROOT) == "png") {
                bm.compress(Bitmap.CompressFormat.PNG, 100, baos)
            } else if (imageType.toLowerCase(Locale.ROOT) == "jpeg" || imageType.toLowerCase(Locale.ROOT) == "jpg") {
                bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            }
            val fos = FileOutputStream(file)
            fos.write(baos.toByteArray())
            fos.flush()
            fos.close()
            println("file " + fname + "output done.")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getStringFromAssets(c: Context, fileName: String): String {
        var result = ""
        try {
            val inputReader = InputStreamReader(c.resources.assets.open(fileName))
            result = inputReader.readText()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return result
    }

    /**
     * 将图片的exif信息从A拷贝写入到B
     *
     * @param fromPath
     * @param toPath
     */
    fun copyExifInfo(fromPath: String, toPath: String) {
        //写入照片信息到文件
        val exifFrom: ExifInterface? // 获取图片Exif
        val exifTo: ExifInterface? // 获取图片Exif
        try {
            exifFrom = ExifInterface(fromPath)
            exifTo = ExifInterface(toPath)
            val tTag = exifFrom.getAttribute(ExifInterface.TAG_DATETIME)
            val aTag = exifFrom.getAttribute(ExifInterface.TAG_GPS_ALTITUDE)
            val latTag = exifFrom.getAttribute(ExifInterface.TAG_GPS_LATITUDE)
            val lngTag = exifFrom.getAttribute(ExifInterface.TAG_GPS_LONGITUDE)
            //            AiLog.i(AiLog.TAG_WZZ, "Utils copyExifInfo:" + tTag + "@" + aTag + "@" + latTag + "@" + lngTag);
            if (!TextUtils.isEmpty(tTag)) {
                exifTo.setAttribute(ExifInterface.TAG_DATETIME, tTag)
            }
            if (!TextUtils.isEmpty(aTag)) {
                exifTo.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, aTag)
            }
            if (!TextUtils.isEmpty(latTag)) {
                exifTo.setAttribute(ExifInterface.TAG_GPS_LATITUDE, latTag)
            }
            if (!TextUtils.isEmpty(lngTag)) {
                exifTo.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, lngTag)
            }
            exifTo.saveAttributes()
            //            AiLog.i(AiLog.TAG_WZZ, "Utils copyExifInfo 写入exif信息成功:" + exifFrom.getAttribute(ExifInterface.TAG_DATETIME));
        } catch (e: Exception) {
            e.printStackTrace()
            //            AiLog.i(AiLog.TAG_WZZ, "Utils copyExifInfo 写入exif信息失败:" + e.getMessage());
        }
    }

    fun cleanDirectory(directory: File?) {
        if (directory == null || !directory.exists()) {
            return
        }
        if (!directory.isDirectory) {
            return
        }
        val files = directory.listFiles() ?: return
        for (i in files.indices) {
            val file = files[i]
            forceDelete(file)
        }
    }

    fun forceDelete(file: File?) {
        if (file == null) {
            return
        }
        if (file.isDirectory) {
            deleteDirectory(file)
        } else {
            file.delete()
        }
    }

    fun deleteDirectory(directory: File) {
        if (!directory.exists()) {
            return
        }
        cleanDirectory(directory)
        directory.delete()
    }

    /**
     * 获取图片旋转角度
     */
    fun getImageRotate(imagePath: String): Int {
        val exif: ExifInterface?
        return try {
            exif = ExifInterface(imagePath)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0)) {
                ExifInterface.ORIENTATION_NORMAL -> {
                    0
                }
                ExifInterface.ORIENTATION_ROTATE_90 -> {
                    90
                }
                ExifInterface.ORIENTATION_ROTATE_180 -> {
                    180
                }
                ExifInterface.ORIENTATION_ROTATE_270 -> {
                    270
                }
                else -> {
                    0
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            0
        }
    }
}