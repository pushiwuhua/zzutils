@file:Suppress("unused")

package com.pushiwuhua.zzutilslib

import android.app.Activity
import android.graphics.*
import android.graphics.drawable.Drawable
import android.text.TextUtils
import java.security.MessageDigest

/**
 * md5加密字符串
 * md5使用后转成16进制变成32个字节
 */
fun String.md5(): String {
    val digest = MessageDigest.getInstance("MD5")
    val result = digest.digest(toByteArray())
    //没转16进制之前是16位
    println("result${result.size}")
    //转成16进制后是32字节
    return result.toHex()
}

fun ByteArray.toHex(): String {
    //转成16进制后是32字节
    val sb = StringBuilder()
    this.forEach {
        val hex = it.toInt() and (0xFF)
        val hexStr = Integer.toHexString(hex)
        if (hexStr.length == 1) {
            sb.append("0").append(hexStr)
        } else {
            sb.append(hexStr)
        }
    }
    return sb.toString()
}

fun String.sha1(): String {
    val digest = MessageDigest.getInstance("SHA-1")
    return digest.digest(toByteArray()).toHex()
}

fun String.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(toByteArray()).toHex()
}

/**
 * 获取当前屏幕截图，不包含状态栏
 *
 * @param activity
 * @return
 */
fun Activity.snapShotWithoutStatusBar(activity: Activity): Bitmap? {
    val view = activity.window.decorView
    view.isDrawingCacheEnabled = true
    view.buildDrawingCache()
    val bmp = view.drawingCache
    val frame = Rect()
    activity.window.decorView.getWindowVisibleDisplayFrame(frame)
    val statusBarHeight = frame.top
    var bp: Bitmap? = null
    bp = Bitmap.createBitmap(
        bmp, 0, statusBarHeight, screenWidth(), screenHeight()
                - statusBarHeight
    )
    view.destroyDrawingCache()
    return bp
}

/**
 * 选择变换图片
 * @param rotate 旋转角度，可正可负
 * @return 旋转后的图片
 */
fun Bitmap.rotateBitmap(rotate: Float): Bitmap? {
    if (rotate == 0f) {
        return this
    }
    val matrix = Matrix()
    matrix.setRotate(rotate)
    // 围绕原地进行旋转
    val newBM = Bitmap.createBitmap(this, 0, 0, width, height, matrix, false)
    recycle()
    return newBM
}

/**
 * 验证字符串是否是北斗卡号
 */
fun String.isBeidouNO(): Boolean {
    val telRegex: String = "\\d{1,8}" //数字, 最少匹配1次,最多匹配8次
    return !TextUtils.isEmpty(this) && matches(Regex(telRegex)) && Integer.valueOf(this) <= "FFFFFF".toInt(
        16
    )
}

/**
 * 验证手机格式
 */
fun String.isMobileNO(): Boolean {
    val telRegex = Regex("[1][1-9]\\d{9}") //"[1]"代表第1位为数字1，"[358]"代表第二位可以为1-9中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
    return !TextUtils.isEmpty(this) && matches(telRegex)
}

fun Drawable.drawableToBitmap(): Bitmap? {
    val bitmap = Bitmap.createBitmap(
        intrinsicWidth,
        intrinsicHeight,
        if (opacity != PixelFormat.OPAQUE) Bitmap.Config.ARGB_8888 else Bitmap.Config.RGB_565
    )
    val canvas = Canvas(bitmap)
    setBounds(0, 0, intrinsicWidth, intrinsicHeight)
    draw(canvas)
    return bitmap
}