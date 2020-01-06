package com.pushiwuhua.zzutils

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.pushiwuhua.zzutilslib.KDownQk
import com.pushiwuhua.zzutilslib.KOneFilter
import com.pushiwuhua.zzutilslib.KWaitingDialog
import com.pushiwuhua.zzutilslib.singleClick
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Call
import java.io.File
import java.net.URL

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        var call: Call? = null
        btnDown.singleClick {
            Log.i("wzz", "MainActivity onCreate 线程${Thread.currentThread()}")
            call = KDownQk.downFile(
                URL("https://ossn.cmfun.cn/group1/M00/00/01/CgsANl4O9n-ANiTdAnTs1bcvWIY653.apk"),
                File(getExternalFilesDir("").absolutePath + "/ttt${System.currentTimeMillis()}.apk"),
                object : KDownQk.Status {
                    override fun err(err: Throwable) {
                        Log.i("wzz", "MainActivity err $err")
                        btnDown.text = "下载"
                    }

                    override fun finish() {
                        Log.i("wzz", "MainActivity finish ")
                        btnDown.text = "下载"
                    }

                    override fun start() {
                        Log.i("wzz", "MainActivity start ${Thread.currentThread()}")
                    }

                    override fun downing(percents: Float) {
                        Log.i("wzz", "MainActivity downing $percents ${Thread.currentThread()}")
                        lifecycleScope.launch {
                            Log.i(
                                "wzz",
                                "MainActivity downing 内部 $percents ${Thread.currentThread()}"
                            )
                            btnDown.text = "下载$percents"
                        }
                    }
                }
            )
        }

        btnCancel.singleClick {
            Log.i("wzz", "MainActivity onCreate 取消下载")
            call?.cancel()
        }

        fab.singleClick {
            Log.i("wzz", "MainActivity onCreate2 ")
            KWaitingDialog.open(lifecycleScope, supportFragmentManager, "haha")
        }
        val filter: KOneFilter<Boolean> = KOneFilter()
        Log.i("wzz", "MainActivity onCreate ${filter.out(true)}")
        Log.i("wzz", "MainActivity onCreate ${filter.out(true)}")
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
