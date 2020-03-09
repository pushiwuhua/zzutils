@file:Suppress("unused")

package com.pushiwuhua.zzutilslib


import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Checkable
import android.widget.EditText
import kotlin.math.abs

/**
 * 此文件封装一些常用的控件的扩展函数
 * wzz created at 2019/7/4 14:37
 */

/**
 * 绑定一个带清除按钮的控件
 */
fun EditText.bindClearView(clearView: View) {
    clearView.visibility = View.GONE
    textChanged {
        if (it.isEmpty()) {
            clearView.visibility = View.GONE
        } else {
            clearView.visibility = View.VISIBLE
        }
    }

    clearView.setOnClickListener {
        setText("")
    }
}

/**
 * 监听文本框内容变化
 */
fun EditText.textChanged(method: (String) -> Unit) {
    var oldText = String()
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            if (oldText != s.toString()) {
                oldText = s.toString()
                method.invoke(s.toString())
            }
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

    })
}

/**
 * 弹出输入法框
 */
fun EditText.showInput() {
    requestFocus()
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).showSoftInput(
        this,
        InputMethodManager.SHOW_IMPLICIT
    )
}

/**
 * 关闭输入法框
 */
fun EditText.closeInput() {
    (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).hideSoftInputFromWindow(
        windowToken,
        0
    )
}

/**
 * wzz自定义
 * 规定时间内多次点击
 */
inline fun <T : View> T.multipleClick(num: Int, time: Long = 800, crossinline block: (T) -> Unit) {
    var clickedNum = 0
    setOnClickListener {
        val currentTimeMillis = System.currentTimeMillis()
        if (abs(currentTimeMillis - lastClickTime) > time) {
            lastClickTime = currentTimeMillis
            clickedNum = 1
        } else {
            clickedNum++
            if (clickedNum == num) {
                clickedNum = 0
                lastClickTime = System.currentTimeMillis()
                block(this)
            }
        }
    }
}

/**
 * 过滤重复的点击事件
 */
inline fun <T : View> T.singleClick(time: Long = 800, crossinline block: (T) -> Unit) {
    setOnClickListener {
        val currentTimeMillis = System.currentTimeMillis()
        if (abs(currentTimeMillis - lastClickTime) > time) {
            lastClickTime = currentTimeMillis
            block(this)
        }
    }
}

//兼容点击事件设置为this的情况
fun <T : View> T.singleClick(onClickListener: View.OnClickListener, time: Long = 800) {
    setOnClickListener {
        val currentTimeMillis = System.currentTimeMillis()
        if (abs(currentTimeMillis - lastClickTime) > time || this is Checkable) {
            lastClickTime = currentTimeMillis
            onClickListener.onClick(this)
        }
    }
}

var <T : View> T.lastClickTime: Long
    set(value) = setTag(R.string.view_single_click, value)
    get() = getTag(R.string.view_single_click) as? Long ?: 0