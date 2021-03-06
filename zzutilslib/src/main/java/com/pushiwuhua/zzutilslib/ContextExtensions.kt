@file:Suppress("unused")

package com.pushiwuhua.zzutilslib

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityManager.RunningAppProcessInfo
import android.bluetooth.BluetoothAdapter
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.nfc.NfcAdapter
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.PermissionChecker
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.pushiwuhua.zzutilslib.KPicUtils.getBitmapZoom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.io.File
import java.io.InputStreamReader
import java.net.URL
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * 封装组件级框架的扩展方法
 * wzz created at 2019/7/19 9:19
 */

/**
 * 捕获抛出的各种异常
 * 用途: 用户多次点击跳转的控件
 */
fun NavController.safeNavigate(@IdRes resId: Int) {
    try {
        navigate(resId)
    } catch (e: Exception) {
        Log.e("zz", " safeNavigate $e")
    }
}

/**
 * toast显示,可在线程内调用
 */
fun Context.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    takeIf { this is AppCompatActivity }?.let {
        (this as AppCompatActivity).toast(
            message,
            duration
        )
    }
    takeIf { this is Fragment }?.let { toast(message, duration) }
}

/**
 * toast显示,可在线程内调用
 */
fun AppCompatActivity.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    lifecycleScope.launch {
        Toast.makeText(this@toast, message, duration).show()
    }
}

/**
 * toast显示,可在线程内调用
 */
fun Fragment.toast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    lifecycleScope.launch {
        Toast.makeText(requireContext(), message, duration).show()
    }
}

/**
 * 是否支持悬浮窗
 */
fun Context.isEnableOverlay(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(applicationContext)
    } else {
        true
    }
}

/**
 * 是否打开GPS
 */
fun Context.isEnableGPSProvider(): Boolean {
    return (getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(
        LocationManager.GPS_PROVIDER
    )
}

/**
 * 是否支持蓝牙
 */
fun Context.isEnableBlueTooth(): Boolean {
    return KBTUtilQk.get(this).isBlueToothEnable()
}

/**
 * 是否存在NFC模块
 */
fun Context.isAvilableNFC(): Boolean {
    return packageManager.hasSystemFeature(PackageManager.FEATURE_NFC)
}

/**
 * 是否启用NFC模块
 */
fun Context.isEnableNFC(): Boolean {
    NfcAdapter.getDefaultAdapter(this)?.let {
        return it.isEnabled
    }
    return false
}

/**
 * 判断是否在系统忽略电量优化的白名单内
 */
fun Context.isIgnoringBatteryOptimizations(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        kotlin.runCatching {
            val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
            pm.isIgnoringBatteryOptimizations(packageName)
        }.isSuccess
    } else {
        true
    }
}

/**
 * 跳转悬浮窗的系统设置界面
 */
fun Activity.gotoSysSettingsOverlay() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val permissionIntent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivityForResult(permissionIntent, Permission_REQ_CODE_OVERLAY)
    }
}

/**
 * 跳转蓝牙的系统设置界面
 */
fun Activity.gotoSysSettingsBlueTooth() {
    startActivityForResult(
        Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE),
        Permission_REQ_CODE_BLUETOOTH
    )
}

/**
 * 跳转NFC的系统设置界面
 */
fun Activity.gotoSysSettingsNFC() {
    startActivityForResult(Intent(Settings.ACTION_NFC_SETTINGS), Permission_REQ_CODE_NFC)
}

/**
 * 跳转GPS的系统设置界面
 */
fun Activity.gotoSysSettingsGPS() {
    startActivityForResult(
        Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
        Permission_REQ_CODE_GPS
    )
}

/**
 * 跳转省电白名单的系统设置界面
 */
@SuppressLint("BatteryLife")
@RequiresApi(Build.VERSION_CODES.M)
fun Activity.gotoSysSettingsIgnoreBatteryOptimizations() {
    startActivityForResult(Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
        data = Uri.parse("package:$packageName")
    }, Permission_REQ_IGNORE_BATTERY_IGNORE)
}

/**
 * 空白处点击自动隐藏输入框
 * @param view 空白处承载的view
 */
fun Context.autoHideInputWhenBlackSpaceClick(view: View) {
    view.setOnClickListener {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
            view.windowToken,
            0
        )
    }
}

/**
 * 自定义dialog
 * @param layoutId 视图ID
 */
fun Context.showDialog(layoutId: Int, block: (ViewDataBinding, AlertDialog) -> Unit) {
    val builder = AlertDialog.Builder(this)
    val bind: ViewDataBinding =
        DataBindingUtil.inflate(LayoutInflater.from(this), layoutId, null, false)
    builder.setView(bind.root)
    builder.create().apply {
        show()
        block.invoke(bind, this)
    }
}

/**
 * 在获得权限的情况下,调用此函数可获取到位置信息
 */
@Suppress("RemoveExplicitTypeArguments")
suspend fun Context.getGpsInfo(overtime: Long): Location? {
    return withTimeoutOrNull(overtime) {
        val coroutineScope = this
        suspendCoroutine<Location?> { continuation ->
            val manager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    manager.removeUpdates(this)
                    continuation.resume(location)
                }

                override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {

                }

                override fun onProviderEnabled(s: String) {
                }

                override fun onProviderDisabled(s: String) {
                    manager.removeUpdates(this)
                    continuation.resume(null)
                }
            }
            coroutineScope.launch {
                withContext(Dispatchers.Main) {
                    if (PermissionChecker.checkSelfPermission(
                            this@getGpsInfo,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PermissionChecker.PERMISSION_GRANTED
                    ) {
                        manager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            0L,
                            0f,
                            listener
                        )
                    }
                }
            }
        }
    }
}

/**
 * 在获得权限的情况下,调用此函数可获取到GPS的时间信息
 * @param overtime 超时时间
 */
@Suppress("RemoveExplicitTypeArguments")
suspend fun Context.getGpsTime(overtime: Long): Long? {
    val loc = getGpsInfo(overtime)
    loc?.let {
        return it.time
    }
    return null
}

/**
 * 获取互联网时间
 */
suspend fun getNetTime(overTime: Int): Long? {
    val webUrl = "https://www.baidu.com"//
//    val webUrl = "http://www.ntsc.ac.cn"//中国科学院国家授时中心
//    val webUrl = "http://bjtime.cn"//中国科学院国家授时中心
    return try {
        withContext(Dispatchers.IO) {
            val url = URL(webUrl)
            val uc = url.openConnection()
            uc.readTimeout = overTime
            uc.connectTimeout = overTime
            uc.connect()
            uc.date
        }
    } catch (e: Exception) {
        null
    }
}

/**
 * 获取是否可以正常连接互联网
 */
suspend fun netIsNormal(overTime: Int): Boolean {
    val webUrl = "https://www.baidu.com"//
    return try {
        withContext(Dispatchers.IO) {
            val url = URL(webUrl)
            val uc = url.openConnection()
            uc.readTimeout = overTime
            uc.connectTimeout = overTime
            uc.connect()
            true
        }
    } catch (e: Exception) {
        false
    }
}

/**
 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
 * @param dpValue 要转换的dp值
 */
fun Context.dip2px(dpValue: Float): Int {
    val scale = resources.displayMetrics.density
    return (dpValue * scale + 0.5f).toInt()
}

/**
 * 将 sp 转换为 px， 保证尺寸大小不变
 * @param pxValue
 * @return
 */
fun Context.sp2px(pxValue: Float): Int {
    val fontScale = resources.displayMetrics.scaledDensity
    return (pxValue * fontScale + 0.5f).toInt()
}

/**
 * 获取版本号
 */
@Suppress("DEPRECATION")
fun Context.getVerCode(): Int {
    var verCode = -1
    try {
        verCode = packageManager.getPackageInfo(packageName, 0).versionCode
    } catch (localNameNotFoundException: PackageManager.NameNotFoundException) {
    }
    return verCode
}

/**
 * 获取版本名
 */
fun Context.getVerName(): String? {
    var verName: String? = ""
    try {
        verName = packageManager.getPackageInfo(packageName, 0).versionName
    } catch (localNameNotFoundException: PackageManager.NameNotFoundException) {
    }
    return verName
}

/**
 * 获取App名称
 */
fun Context.getAppName(): String? {
    return resources.getText(
        resources.getIdentifier(
            "app_name",
            "string",
            packageName
        )
    ).toString()
}

/**
 * deviceID的组成为：渠道标志+识别符来源标志+hash后的终端识别符
 *
 *
 * 渠道标志为：
 * 1，andriod（a）
 *
 *
 * 识别符来源标志：
 * 1， wifi mac地址（wifi）；
 * 2， IMEI（imei）；
 * 3， 序列号（sn）；
 * 4， id：随机码。若前面的都取不到时，则随机生成一个随机码，需要缓存。
 * @return
 */
fun Context.getDeviceId(): String? {
    val deviceId = StringBuilder()
    // 渠道标志
    deviceId.append("ut")
    try {
        val tm = getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val imei = tm.deviceId
        if (!TextUtils.isEmpty(imei)) {
            deviceId.append("imei")
            deviceId.append(imei)
            return deviceId.toString()
        }
        //序列号（sn）
        val sn = tm.simSerialNumber
        if (!TextUtils.isEmpty(sn)) {
            deviceId.append("sn")
            deviceId.append(sn)
            return deviceId.toString()
        }
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return deviceId.toString()
}

/**
 * 检测是否有网络连接，如果无网络连接，弹出“网络未连接提示”
 *
 * @return 是否网络连接
 */
fun Context.isNetworkAvailableWithTip(): Boolean {
    val flag = checkNetworkState()
    if (!flag) {
        toast("网络未连接")
    }
    return flag
}

/**
 * 检测是否有网络连接,并返回结果，无任何界面提示
 * @return
 */
fun Context.isNetworkAvailable(): Boolean {
    return checkNetworkState()
}

/**
 * 判断wifi是否连接
 */
fun Context.isWifiAvailable(): Boolean {
    var isWifiConnected = false
    val manager =
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager //得到网络连接信息
    val networkInfo = manager.activeNetworkInfo
    if (networkInfo != null && networkInfo.type == ConnectivityManager.TYPE_WIFI) {
        isWifiConnected = networkInfo.isConnected
    }
    return isWifiConnected
}

/**
 * 判断网络是否连接
 */
fun Context.checkNetworkState(): Boolean {
    var isConnect = false
    val manager =
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager //得到网络连接信息
    val networkInfo = manager.activeNetworkInfo
    if (networkInfo != null) {
        isConnect = networkInfo.isAvailable //去进行判断网络是否连接
    }
    return isConnect
}

/**
 * 隐藏软键盘
 */
fun Context.hideKeyBoard() {
    val imm =
        applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (imm.isActive) {
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}

/**
 * 隐藏软键盘
 */
fun Context.forceHideKeyBoard(v: View) {
    val imm = applicationContext
        .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (imm.isActive) {
        imm.hideSoftInputFromWindow(v.applicationWindowToken, 0)
    }
}

/**
 * 强制弹出软键盘，传递一个EditText控件进去
 *
 * @param targetView
 */
fun forceShowKeyBoard(targetView: View) {
    val imm =
        targetView.context.applicationContext
            .getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.showSoftInput(targetView, InputMethodManager.SHOW_FORCED)
}

/**
 * 从assets目录中读取文本内容
 * @param fileName 资源文件名
 * @return
 */
fun Context.readContentFromAssets(fileName: String): String {
    val inputReader =
        InputStreamReader(resources.assets.open(fileName))
    return inputReader.readText()
}

/**
 * 获得屏幕高度
 * @return
 */
fun Context.screenWidth(): Int {
    val wm = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val outMetrics = DisplayMetrics()
    wm.defaultDisplay.getMetrics(outMetrics)
    return outMetrics.widthPixels
}

/**
 * 获得屏幕高度
 * @return
 */
fun Context.screenHeight(): Int {
    val wm = applicationContext.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val outMetrics = DisplayMetrics()
    wm.defaultDisplay.getMetrics(outMetrics)
    return outMetrics.heightPixels
}

/**
 * 获取manifest meta值  用处之一: 百度推送获取api key
 * @param metaKey
 * @return wzz
 */
fun Context.metaValue(metaKey: String?): String? {
    var metaData: Bundle?
    var apiKey: String?
    metaKey?.let {
        runCatching {
            val ai = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
            metaData = ai.metaData
            apiKey = metaData?.getString(metaKey)
            return apiKey
        }.onFailure {
            return null
        }
    }
    return null
}

/**
 * 通知系统更新扫描新的图片
 * @param newImagePath 新的图片地址
 */
fun Context.updateGallery(newImagePath: String) {
    val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
    val uri = Uri.fromFile(File(newImagePath))
    intent.data = uri
    sendBroadcast(intent) //这个广播的目的就是更新图库，发了这个广播进入相册就可以找到你保存的图片了！，记得要传你更新的file哦
}

/**
 * 判断app在前台还是后台,wzz封装已测, 此函数速度很快,毫秒数在个位数
 */
fun Context.checkAppBackground(): Boolean {
    val activityManager =
        applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val appProcesses = activityManager.runningAppProcesses
    if (appProcesses != null && appProcesses.size > 0) {
        for (appProcess in appProcesses) {
            if (appProcess.processName == packageName) {
                return appProcess.importance != RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            }
        }
    }
    return false
}

fun Context.getBitmapZoomDp(bm: Bitmap, wdp: Int, hdp: Int): Bitmap? {
    val newWidth: Int = dip2px(wdp.toFloat())
    val newHeight: Int = dip2px(hdp.toFloat())
    return getBitmapZoom(bm, newWidth, newHeight)
}

/**
 * 检查某APK是否存在
 * @param packageName
 * @return
 */
fun Context.checkApkExist(packageName: String): Boolean {
    return if (TextUtils.isEmpty(packageName)) false else try {
        packageManager.getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

/**
 * 判断应用是否已经启动
 * @param packageName 要判断应用的包名
 * @return boolean
 */
fun Context.isAppAlive(packageName: String): Boolean {
    val activityManager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    val processInfos = activityManager.runningAppProcesses
    for (i in processInfos.indices) {
        if (processInfos[i].processName == packageName) {
            return true
        }
    }
    return false
}