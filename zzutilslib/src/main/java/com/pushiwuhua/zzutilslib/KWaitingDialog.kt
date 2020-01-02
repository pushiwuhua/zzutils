@file:Suppress("unused")

package com.pushiwuhua.zzutilslib

import android.animation.ObjectAnimator
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleCoroutineScope
import com.pushiwuhua.zzutilslib.databinding.LayoutDialogWaitBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * KWaitingDialog 封装等待
 * wzz created at 2019/7/22 9:55
 */
object KWaitingDialog : DialogFragment() {
    private var showText: String? = null //显示的文本
    private var animator: ObjectAnimator? = null //动画
    private var job: Job? = null //延迟显示的工作序列
    private lateinit var bind: LayoutDialogWaitBinding

    override fun onDestroyView() {
        super.onDestroyView()
        animator?.cancel()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)//无标题栏
        bind = DataBindingUtil.inflate(inflater, R.layout.layout_dialog_wait, container, false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(0))//wzz 核心代码:去除背景边框

        if (!TextUtils.isEmpty(showText)) {
            bind.tv.text = showText
        }

        animator =
            ObjectAnimator.ofFloat(bind.img, "rotation", 0f, 360f).apply {
                repeatCount = 20000
                duration = 400
            }
        animator?.start()
        return bind.root
    }

    /**
     * 打开等待框
     *
     * @param manager
     */
    fun open(
        scope: LifecycleCoroutineScope,
        manager: FragmentManager?,
        showText: String = "",
        delayMSecs: Long = 200
    ) {
        KWaitingDialog.showText = showText
        manager?.let {
            job = scope.launch {
                delay(delayMSecs)
                show(manager, "KWaitingDialog")
            }
        }
    }

    /**
     * 关闭对话框
     */
    fun close() {
        kotlin.runCatching {
            job?.cancel()
            dismissAllowingStateLoss()
        }
    }
}