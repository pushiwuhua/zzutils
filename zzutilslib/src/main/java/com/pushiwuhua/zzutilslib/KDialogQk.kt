@file:Suppress("unused")

package com.pushiwuhua.zzutilslib

import android.content.Context
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AlertDialog
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * KDialogQk 构建Dialog框的快速工具类
 * wzz created at 2019/7/1 11:08
 */
class KDialogQk private constructor(private val context: Context) {
    private var title: String? = null
    private var message: String? = null
    private var buttonTextNegative: String? = null//否定的按钮文本
    private var buttonTextPositive: String? = null//肯定的按钮文本
    private var configView: View? = null//自定义的视图

    companion object {
        /**
         * 创建实例
         */
        fun newDialog(context: Context): KDialogQk {
            return KDialogQk(context)
        }
    }

    /**
     * 设置标题
     *
     * @param title
     * @return
     */
    fun withTitle(title: String): KDialogQk {
        this.title = title
        return this
    }

    /**
     * 设置消息体
     *
     * @param message
     * @return
     */
    fun withMessage(message: String?): KDialogQk {
        this.message = message
        return this
    }

    /**
     * 设置否定按钮的文本
     *
     * @param text
     * @return
     */
    fun withBtnTextNegative(text: String): KDialogQk {
        this.buttonTextNegative = text
        return this
    }

    /**
     * 设置肯定按钮的文本
     *
     * @param text
     * @return
     */
    fun withBtnTextPositive(text: String): KDialogQk {
        this.buttonTextPositive = text
        return this
    }

    /**
     * 设置自定义控件
     *
     * @return
     */
    fun withView(view: View): KDialogQk {
        this.configView = view
        return this
    }

    /**
     * 创建并显示AlerDialog, 利用协程的特性挂起函数
     *
     * @return
     */
    suspend fun build(blockDialogFinish: ((dialog: AlertDialog, continuation: Continuation<Boolean>) -> Unit)? = null): Boolean {
        return suspendCoroutine {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(title)
            builder.setMessage(message)
            if (configView != null) {
                builder.setView(configView)
            }

            if (!TextUtils.isEmpty(buttonTextNegative)) {
                builder.setNegativeButton(buttonTextNegative) { dialog, _ ->
                    dialog.dismiss()
                    it.resumeWith(Result.success(false))
                }
            }
            if (!TextUtils.isEmpty(buttonTextPositive)) {
                builder.setPositiveButton(buttonTextPositive) { dialog, _ ->
                    dialog.dismiss()
                    it.resumeWith(Result.success(true))
//                    it.resume(true)
//                    emitter.onNext(java.lang.Boolean.TRUE)
//                    emitter.onComplete()
                }
            }

            val cancelAble =
                TextUtils.isEmpty(buttonTextPositive) && TextUtils.isEmpty(buttonTextNegative)
            builder.setCancelable(cancelAble)//是否可取消
            val alertDialog = builder.create()
            alertDialog.show()
            blockDialogFinish?.invoke(alertDialog, it)
            alertDialog.setOnDismissListener { _ ->
                if (cancelAble) {
                    it.resume(false)
                }
            }
        }
    }
}
