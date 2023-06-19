package com.basic.ui.list

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.MainThread
import androidx.recyclerview.widget.*

/**
 * @author Peter Liu
 * @since 2020-02-23 02:02
 *
 */

fun createView(parent: ViewGroup, layoutId: Int): View {
    return LayoutInflater.from(parent.context).inflate(layoutId, parent, false)
}

/**
 * BaseRecycleAdapter 只负责处理数据
 * 主要有 setData， addData， removeData， clear
 */
abstract class RecycleAdapter<DATA, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {
    protected var dataList: MutableList<DATA>? = null

    /**
     * 返回真实数据
     */
    open fun getData(): List<DATA>? {
        return dataList
    }

    /**
     *  返回真实数据列表的大小，不包含header,footer
     */
    fun getDataSize(): Int {
        return getData()?.size ?: 0
    }

    override fun getItemCount(): Int {
        return dataList?.size ?: 0
    }

    /**
     * 获取列表中的item
     */
    open fun getItem(position: Int): DATA? {
        if (dataList == null || position < 0 || position >= dataList!!.size) return null
        return dataList?.get(position)
    }

    /**
     * 设置数据
     *
     * @param forceNotifyAll 强制刷新整个列表，默认false
     */
    @MainThread
    @JvmOverloads
    open fun setData(list: List<DATA>?, forceNotifyAll: Boolean = false) {
        setData(list, forceNotifyAll, true)
    }

    protected fun setData(list: List<DATA>?, forceNotifyAll: Boolean = false, forceClone: Boolean= false) {
        if (list.isNullOrEmpty()) {
            return
        }
        if (dataList.isNullOrEmpty() || forceNotifyAll) {
            //浅复制
            dataList = if (!forceClone && list is MutableList) list else list.toMutableList()
            notifyDataSetChanged()
        } else {
            diffUpdate(list, forceClone)
        }
    }

    protected fun diffUpdate(list: List<DATA>, forceClone:Boolean = true) {
        val oldData = dataList
        val result = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return areItemsTheSame(oldData!![oldItemPosition], list[newItemPosition])
            }

            override fun getOldListSize(): Int {
                return oldData?.size ?: 0
            }

            override fun getNewListSize(): Int {
                return list.size
            }

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                return areContentsTheSame(oldData!![oldItemPosition], list[newItemPosition])
            }

            override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Any? {
                return getChangePayload(oldData!![oldItemPosition], list[newItemPosition])
            }
        })
        dataList = if (!forceClone && list is MutableList) list else list.toMutableList()
        result.dispatchUpdatesTo(this)
    }

    /**
     * 默认情况下检查old.equals(new)
     */
    open fun areItemsTheSame(old: DATA, new: DATA): Boolean {
        return old == new
    }

    /**
     * call after areItemsTheSame return true,
     * 默认情况下是严格模式，只有引用相等才相等。
     */
    open fun areContentsTheSame(old: DATA, new: DATA): Boolean {
        return old === new
    }

    /**
     * A payload object that represents the change between the two items.
     * see [DiffUtil.Callback.getChangePayload]
     */
    open fun getChangePayload(old: DATA, new: DATA): Any? {
        return null
    }


    @MainThread
    fun addAll(list: Collection<DATA>?) {
        if (list.isNullOrEmpty()) {
            return
        }
        if (dataList.isNullOrEmpty()) {
            //浅复制
            dataList = list.toMutableList()
            notifyDataSetChanged()
        } else {
            val oldSize = getListSize()
            dataList?.addAll(list)
            notifyItemRangeInserted(oldSize, list.size)
        }
    }

    @MainThread
    fun addAll(position: Int, list: Collection<DATA>?) {
        if (list.isNullOrEmpty()) {
            return
        }
        if (dataList.isNullOrEmpty()) {
            //浅复制
            dataList = list.toMutableList()
            notifyDataSetChanged()
        } else {
            dataList?.addAll(position, list)
            notifyItemRangeInserted(position, list.size)
        }
    }

    @MainThread
    fun add(item: DATA?) {
        if (item == null) {
            return
        }
        if (dataList.isNullOrEmpty()) {
            //浅复制
            dataList = mutableListOf(item)
            notifyItemChanged(0)
        } else {
            val oldSize = getListSize()
            dataList?.add(item)
            notifyItemInserted(oldSize)
        }
    }

    @MainThread
    fun add(position: Int, item: DATA?) {
        if (item == null) {
            return
        }
        if (dataList.isNullOrEmpty()) {
            //浅复制
            dataList = mutableListOf(item)
            notifyItemChanged(0)
        } else {
            dataList?.add(position, item)
            notifyItemInserted(position)
        }
    }

    @MainThread
    open fun removeAll(list: Collection<DATA>?) {
        if (list.isNullOrEmpty() || dataList.isNullOrEmpty()) {
            return
        }
        val temp = dataList!!.toMutableList()
        temp.removeAll(list)
        diffUpdate(temp)
    }

    @MainThread
    open fun remove(item: DATA?) {
        if (item == null || dataList.isNullOrEmpty()) {
            return
        }
        val index: Int = getItemIndex(item)
        if (index >= 0) {
            dataList?.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    @MainThread
    open fun remove(pos: Int) {
        if (pos >= 0 && pos < getListSize()) {
            dataList?.removeAt(pos)
            notifyItemRemoved(pos)
        }
    }

    @MainThread
    fun move(from: Int, to: Int) {
        val dataItem = getItem(from)
        if (dataItem != null && to >= 0 && to < getListSize()) {
            dataList?.remove(dataItem)
            dataList?.add(to, dataItem)
            notifyItemMoved(from, to)
        }
    }

    fun replace(pos: Int, item: DATA?) {
        if (item != null && dataList != null && pos >= 0 && pos < getListSize()) {
            dataList?.set(pos, item)
            notifyItemChanged(pos)
        }
    }

    fun replace(old: DATA?, item: DATA?) {
        if (item != null && dataList != null && old != null) {
            val index: Int = getItemIndex(old)
            if (index >= 0) {
                dataList?.set(index, item)
                notifyItemChanged(index)
            }
        }
    }

    @MainThread
    fun clear() {
        if (dataList.isNullOrEmpty()) {
            return
        }
        dataList?.clear()
        notifyDataSetChanged()
    }

    protected fun getListSize(): Int {
        return dataList?.size ?: 0
    }

    protected fun getItemIndex(item: DATA?): Int {
        return dataList?.indexOf(item) ?: -1
    }
}


/**
 * 在异步线程的使用DiffUtil更新数据的adapter
 */
abstract class AsyncDifferRecycleAdapter<DATA, VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH> {
    private val mHelper: AsyncListDiffer<DATA>

    private var mDiffCallback: DiffUtil.ItemCallback<DATA> = object : DiffUtil.ItemCallback<DATA>() {
        override fun areItemsTheSame(oldItem: DATA, newItem: DATA): Boolean {
            return this@AsyncDifferRecycleAdapter.areItemsTheSame(oldItem, newItem)
        }

        override fun areContentsTheSame(oldItem: DATA, newItem: DATA): Boolean {
            return this@AsyncDifferRecycleAdapter.areContentsTheSame(oldItem, newItem)
        }
    }

    constructor(diffCallback: DiffUtil.ItemCallback<DATA>) {
        mDiffCallback = diffCallback
        this.mHelper = AsyncListDiffer(AdapterListUpdateCallback(this), AsyncDifferConfig.Builder(diffCallback).build())

    }

    constructor() {
        this.mHelper = AsyncListDiffer(AdapterListUpdateCallback(this), AsyncDifferConfig.Builder(mDiffCallback).build())
    }

    /**
     * 设置更新数据
     */
    open fun setList(list: List<DATA>?) {
        this.mHelper.submitList(list)
    }

    fun getItem(position: Int): DATA {
        return this.mHelper.currentList[position]
    }

    override fun getItemCount(): Int {
        return this.mHelper.currentList.size
    }

    open fun areItemsTheSame(oldItem: DATA, newItem: DATA): Boolean {
        return oldItem == newItem
    }

    open fun areContentsTheSame(oldItem: DATA, newItem: DATA): Boolean {
        return true
    }
}





