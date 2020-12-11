@file:Suppress("unused")

package com.pushiwuhua.zzutilslib.recyclerview

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil

/**
 * 运用kotlin写的用于Paging架构的适配器
 * wzz created at 2019/7/8 14:14
 */
class KRVPagingBindingAdapter<T>(diffCallback: DiffUtil.ItemCallback<T>) :
    PagedListAdapter<T, KRVBaseHolder<T>>(diffCallback) {
    private var layoutIdNormal = -1//普通的列表布局ID
    private lateinit var methedNormal: ((ViewDataBinding, Int, T?) -> Unit)

    enum class EnumTypeItemView {
        NORMAL {
            override fun value(): Int {
                return 0
            }
        };

        abstract fun value(): Int
    }

    override fun getItemViewType(position: Int): Int {
        return EnumTypeItemView.NORMAL.value()//正常
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KRVBaseHolder<T> {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(parent.context),
            layoutIdNormal,
            parent,
            false
        )

        return KRVBaseHolder(binding, methedNormal)
    }

    override fun onBindViewHolder(holder: KRVBaseHolder<T>, pos: Int) {
        val data = getItem(pos)
        holder.method.invoke(holder.binding, pos, data)
    }

    fun withLayout(
        layoutId: Int,
        method: (ViewDataBinding, Int, T?) -> Unit
    ): KRVPagingBindingAdapter<T> {
        layoutIdNormal = layoutId
        methedNormal = method
        return this
    }
}
