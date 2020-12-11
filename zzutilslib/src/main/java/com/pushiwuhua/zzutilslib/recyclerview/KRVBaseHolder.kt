package com.pushiwuhua.zzutilslib.recyclerview

import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

/**
 * kotlin封装的列表元素基类
 * wzz created at 2019/7/8 14:25
 */
data class KRVBaseHolder<T> constructor(
    var binding: ViewDataBinding,
    var method: (ViewDataBinding, Int, T?) -> Unit
) : RecyclerView.ViewHolder(binding.root)
