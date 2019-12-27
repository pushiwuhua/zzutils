package com.pushiwuhua.zzutils

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.pushiwuhua.zzutilslib.KOneFilter
import com.pushiwuhua.zzutilslib.singleClick
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        fab.singleClick {
            Log.i("wzz", "MainActivity onCreate2 ")
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
