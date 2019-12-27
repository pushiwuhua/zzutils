@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package com.pushiwuhua.zzutilslib.recyclerview

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.*
import android.widget.FrameLayout
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.util.*
import kotlin.math.max

/**
 * KRecyclerView 自定义列表控件
 * wzz created at 2019/7/19 11:27
 */
class KRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : RecyclerView(context, attrs, defStyle) {

    private var mScrollListeners: MutableList<OnUTScrollListener> = ArrayList()
    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null//设置关联的刷新控件

    private var mEventDownY = 0.0
    private var mEventUpY = 0.0
    private var mIsDownFresh = false//是否是下拉刷新的动作
    private var mIsChildViewTouchFilter = false//是否过滤子控件的触摸事件
    private var mIsFatherViewTouchIgnoreFilter = false//是否忽略对父控件的触摸事件反馈过滤

    private var mIsLimitMaxHeight: Boolean = false
    private var mMaxHeightLimit = 0
    private var mScroolDy: Int = 0//滑动时的dy值

    private var emptyView: View? = null

    private val dataObserver = object : RecyclerView.AdapterDataObserver() {
        override fun onChanged() {//设置空view原理都一样，没有数据显示空view，有数据隐藏空view
            if (emptyView != null) {
                val isEmptyData = 0 == adapter!!.itemCount
                if (isEmptyData) {
                    if (mSwipeRefreshLayout != null && mSwipeRefreshLayout!!.isRefreshing) {
                        emptyView?.visibility = View.GONE
                    } else {
                        emptyView?.visibility = View.VISIBLE
                    }
                } else {
                    emptyView?.visibility = View.GONE
                }
            }
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int) {
            onChanged()
        }

        override fun onItemRangeChanged(positionStart: Int, itemCount: Int, payload: Any?) {
            onChanged()
        }

        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            onChanged()
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            onChanged()
        }

        override fun onItemRangeMoved(fromPosition: Int, toPosition: Int, itemCount: Int) {
            onChanged()
        }
    }

    /**
     * 列表布局类型
     * Horizontal 水平
     * Vertical 垂直的
     * Grid 方格型
     */
    enum class RecyclerType {
        Horizontal,
        Vertical,
        Grid
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            mEventUpY = event.y.toDouble()
            mIsDownFresh = mEventUpY - mEventDownY >= 0

        }
        if (event.action == MotionEvent.ACTION_DOWN) {
            mEventDownY = event.y.toDouble()
        }
        return if (mIsFatherViewTouchIgnoreFilter) {
            false
        } else {
            super.onTouchEvent(event)
        }
    }

    /**
     * 是否限制最大高度
     *
     * @param isLimit
     * @param px
     */
    fun setMaxHeightLimit(isLimit: Boolean, px: Int) {
        mIsLimitMaxHeight = isLimit
        mMaxHeightLimit = px
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        var heightMSpec = heightMeasureSpec
        try {
            if (mIsLimitMaxHeight) {
                heightMSpec =
                    MeasureSpec.makeMeasureSpec(mMaxHeightLimit, MeasureSpec.AT_MOST)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        //重新计算控件高、宽
        super.onMeasure(widthMeasureSpec, heightMSpec)
    }

    /**
     * 设置子控件是否接受触摸事件
     *
     * @param isFilter
     */
    fun setChildViewTouchFilter(isFilter: Boolean) {
        mIsChildViewTouchFilter = isFilter
    }

    /**
     * 设置是否对父控件的触摸事件反馈过滤进行忽略
     *
     * @param isIgnoreFilter true:放行 false:拦截,默认
     */
    fun setFatherViewTouchIgnoreFilter(isIgnoreFilter: Boolean) {
        mIsFatherViewTouchIgnoreFilter = isIgnoreFilter
    }


    /**
     * wzz 特别注意onInterceptTouchEvent 和 onTouchEvent 要同时实现, 避免列表内的子控件拦截处理
     *
     * @param event
     * @return
     */
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_UP) {
            mEventUpY = event.y.toDouble()
            mIsDownFresh = mEventUpY - mEventDownY >= 0
        }
        if (event.action == MotionEvent.ACTION_DOWN) {
            mEventDownY = event.y.toDouble()
        }
        val `is` = super.onInterceptTouchEvent(event)
        return if (mIsChildViewTouchFilter) {
            true//拦截触摸事件
        } else {
            `is`
        }
    }

    @JvmOverloads
    fun setUTLayout(type: RecyclerType, value: Int = 0) {
        val layoutManager: LayoutManager = if (type == RecyclerType.Grid) {
            if (value == 0) {
                StaggeredGridLayoutManager(4, StaggeredGridLayoutManager.VERTICAL)
            } else {
                StaggeredGridLayoutManager(value, StaggeredGridLayoutManager.VERTICAL)
            }
        } else if (type == RecyclerType.Horizontal) {
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        } else {
            LinearLayoutManager(context, VERTICAL, false)
        }

        setLayoutManager(layoutManager)
    }

    /**
     * 设置关联的刷新控件, 当正在刷新时,禁止滑动列表, 此处有recyclerview的bug
     * 此bug已在优徒验证, 更多信息见:http://www.cnblogs.com/krislight1105/p/5272740.html
     *
     *
     * wzz created at 2016/6/15 18:02
     */
    fun setSwipeRefreshLayout(layout: SwipeRefreshLayout) {
        mSwipeRefreshLayout = layout
        setOnTouchListener { _, _ -> mSwipeRefreshLayout != null && mSwipeRefreshLayout!!.isRefreshing }
    }

    fun addOnUTScrollListener(listener: OnUTScrollListener) {
        mScrollListeners.add(listener)
        addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                //                AiLog.i(AiLog.TAG_WZZ, "UTRecyclerView onScrollStateChanged:" + newState);
                //                AiLog.i(AiLog.TAG_WZZ, "UTRecyclerView onScrollStateChanged:" + recyclerView.getLayoutManager().getClass());
                if (recyclerView.layoutManager!!.javaClass == LinearLayoutManager::class.java) {
                    // 当不滚动时
                    if (newState == SCROLL_STATE_IDLE) {
                        //不能向上滚动滚动时,到了最后
                        if (!canScrollVertically(1)) {
                            for (i in mScrollListeners.indices.reversed()) {
                                if (!mIsDownFresh)
                                    mScrollListeners[i].onScrollEnd()
                            }
                        } else if (!canScrollVertically(-1)) {
                            for (i in mScrollListeners.indices.reversed()) {
                                mScrollListeners[i].onScrollTop()
                            }
                        }
                    }
                } else if (recyclerView.layoutManager!!.javaClass == StaggeredGridLayoutManager::class.java) {
                    val manager = recyclerView.layoutManager as StaggeredGridLayoutManager?
                    // 当不滚动时
                    if (newState == SCROLL_STATE_IDLE) {
                        //获取最后一个完全显示的ItemPosition
                        val pos = IntArray(manager!!.spanCount)
                        manager.findLastCompletelyVisibleItemPositions(pos)
                        val totalItemCount = manager.itemCount
                        var lastIndex = -1
                        if (pos.isNotEmpty()) {
                            lastIndex = pos[0]
                            for (po in pos) {
                                lastIndex = max(po, lastIndex)
                            }
                        }
                        // 判断是否滚动到底部
                        if (lastIndex > -1 && lastIndex == totalItemCount - 1) {
                            //加载更多功能的代码
                            for (i in mScrollListeners.indices.reversed()) {
                                if (!mIsDownFresh)
                                    mScrollListeners[i].onScrollEnd()
                            }
                        }
                    }
                }

            }
        })
    }

    fun removeOnUTScrollListener(listener: OnUTScrollListener) {
        mScrollListeners.remove(listener)
    }

    fun clearOnUTScrollListeners() {
        mScrollListeners.clear()
    }

    /**
     * 自定义简易监听列表滑动到底部或顶部
     */
    abstract class OnUTScrollListener {
        fun onScrollEnd() {}

        fun onScrollTop() {}
    }

    /**
     * 设置空视图
     */
    fun setEmptyView(view: View, gravityValue: Int) {
        this.emptyView = view
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        params.gravity = gravityValue
        (this.parent as ViewGroup).addView(view, params)
        change()
    }

    /**
     * 设置空视图
     */
    fun withEmptyView(
        layoutEmptyId: Int,
        gravityValue: Int,
        method: ((ViewDataBinding) -> Unit)? = null
    ): KRecyclerView {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(context),
            layoutEmptyId,
            this,
            false
        )
        method?.invoke(binding)
        setEmptyView(binding.root, gravityValue)
        return this
    }


    /**
     * 设置头部控件
     */
    fun withHeadView(layoutHeadId: Int, method: ((ViewDataBinding) -> Unit)?): KRecyclerView {
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(context),
            layoutHeadId,
            this,
            false
        )
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        (this.parent as ViewGroup).addView(binding.root, params)
        method?.invoke(binding)
        //留头部空间
        binding.root.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                this@KRecyclerView.setPadding(
                    paddingLeft,
                    paddingTop + binding.root.measuredHeight,
                    paddingRight,
                    paddingBottom
                )
            }
        })
        return this
    }


    /**
     * 刷新, 外界通过此接口强制触发列表检查状态变更
     */
    fun change() {
        dataObserver.onChanged()
    }


    override fun setAdapter(adapter: Adapter<*>?) {
        super.setAdapter(adapter)
        adapter!!.registerAdapterDataObserver(dataObserver)
        change()
    }
}
