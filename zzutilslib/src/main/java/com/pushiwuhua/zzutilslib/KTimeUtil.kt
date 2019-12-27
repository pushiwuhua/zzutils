@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.pushiwuhua.zzutilslib

import android.annotation.SuppressLint
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * KTimeUtil 时间转换函数
 * wzz created at 2019/7/25 16:36
 */
object KTimeUtil {
    const val YMD_HMS = "yyyy-MM-dd HH:mm:ss"
    const val UT_BD_DATE = "yyyyMMdd"//北斗传输自定义协议
    const val UT_BD_TIME = "HHmmss"//北斗传输自定义协议
    const val HMS = "HH:mm:ss"
    const val AHMS = "a h:mm:ss" //上午 8：00：00

    /**
     * @param time
     * @return
     */
    fun getTime(time: Long): String {
        val sdf = SimpleDateFormat(YMD_HMS, Locale.getDefault())
        val date = Date(time)
        return sdf.format(date)
    }

    /**
     * @param time
     * @return
     */
    fun getTime(time: Long, format: String): String {
        val sdf = SimpleDateFormat(format, Locale.getDefault())
        val date = Date(time)
        return sdf.format(date)
    }

    /**
     * 获取打卡时间
     *
     * @param time
     * @return
     */
    fun getCheckTime(time: Long): String {
        val sdf = SimpleDateFormat(AHMS, Locale.getDefault())
        val date = Date(time)
        return sdf.format(date)
    }

    /**
     * 获得2000-01-01 08：00：00格式日期的long类型毫秒数
     */
    fun getLongTime1(cc_time: String): Long {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()) // yyyy-MM-dd
        return try {
            val curDate = formatter.parse(cc_time)
            curDate.time
        } catch (e: ParseException) {
            e.printStackTrace()
            0
        }
    }

    /**
     * 将北斗协议里的时间格式转为标准时间格式
     *
     * @param cc_time
     * @return
     */
    fun getLongTime2(cc_time: String): Long {
        val formatter = SimpleDateFormat("ddMMyyHHmmss", Locale.getDefault()) // yyyy-MM-dd
        formatter.timeZone = TimeZone.getTimeZone("GMT+0")//零时区时间
        return try {
            val curDate = formatter.parse(cc_time)
            curDate.time
        } catch (e: ParseException) {
            e.printStackTrace()
            0
        }
    }

    /**
     * 将1970年的毫秒数转为格式 2016:03:08 11:41:41
     *
     * @author wzz
     */
    fun getUTMSecsStr(timeMSecs: Long): String {
        val formatter = SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(timeMSecs))
    }

    /**
     * 方便的时间标签函数
     * wzz
     *
     * @return 时间格式 2016:03:08 11:41:41
     */
    fun timeTag(): String {
        return getUTMSecsStr(System.currentTimeMillis())
    }

    /**
     * 电子表字体的时间格式
     *
     * @param time 标准1970时间
     * @return
     */
    fun timeStr(time: Long?): String {
        val date = Date(time!!)
        val formatter1 = SimpleDateFormat("HH :mm :ss", Locale.getDefault())
        return formatter1.format(date)
    }


    /**
     * 获取2018年8月3日\n星期五 的字符串格式
     *
     * @param time
     * @return
     */
    @SuppressLint("SimpleDateFormat")
    fun dateStr(time: Long?): String {
        val date = Date(time!!)
        val formatter1 = SimpleDateFormat("yyyy年MM月dd日") // yyyy-MM-dd
        val builder = StringBuilder()
        builder.append(formatter1.format(date) + "\n")
        builder.append(getWeekDay(time))
        return builder.toString()
    }

    /**
     * 根据时间获取中文星期几
     *
     * @param time
     * @return
     */
    fun getWeekDay(time: Long?): String {
        val cal = Calendar.getInstance()
        cal.time = Date(time!!)
        when (cal.get(Calendar.DAY_OF_WEEK)) {
            1 -> return "星期日"
            2 -> return "星期一"
            3 -> return "星期二"
            4 -> return "星期三"
            5 -> return "星期四"
            6 -> return "星期五"
            7 -> return "星期六"
        }
        return ""
    }
}
