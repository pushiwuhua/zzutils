package com.pushiwuhua.zzutilslib

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 蓝牙快速工具
 * wzz created at 2019/8/6 15:06
 */
class KBTUtilQk {
    private lateinit var bluetoothManager: BluetoothManager
    private lateinit var bluetoothAdapter: BluetoothAdapter
    private lateinit var appContext: Context

    companion object {
        @Volatile
        private var defaultInstance: KBTUtilQk? = null

        /**
         * 获得管理器
         */
        fun get(context: Context): KBTUtilQk {
            return defaultInstance ?: synchronized(this) {
                defaultInstance ?: KBTUtilQk().also {
                    defaultInstance = it
                    it.init(context)
                }
            }
        }
    }

    private fun init(context: Context) {
        appContext = context.applicationContext
        bluetoothManager =
            appContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
    }

    /**
     * 蓝牙是否打开
     *
     * @return
     */
    fun isBlueToothEnable(): Boolean {
        return bluetoothAdapter.isEnabled
    }

    private val mScanCallback = object : ScanCallback() {
        var method: ((BluetoothDevice) -> Unit)? = null
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            method?.invoke(result.device)
        }
    }

    /**
     * 搜索蓝牙设备
     * @param methodDiscoverDevice 搜索到设备的回调方法
     */
    suspend fun searchBLEDevices(methodDiscoverDevice: (BluetoothDevice) -> Unit) {
        withContext(Dispatchers.IO) {
            mScanCallback.method = methodDiscoverDevice
            bluetoothAdapter.bluetoothLeScanner?.startScan(mScanCallback)
        }
    }

    /**
     * 终止搜索
     */
    fun stopSearch() {
        bluetoothAdapter.bluetoothLeScanner?.stopScan(mScanCallback)
    }
}