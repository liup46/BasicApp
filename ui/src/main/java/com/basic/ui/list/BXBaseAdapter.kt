package com.basic.ui.list

import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.widget.Space
import androidx.annotation.IdRes
import androidx.recyclerview.widget.RecyclerView
import com.basic.env.BuildConfig

/**
 * @author Peter Liu
 * @since 2020-02-23 02:02
 *
 */

open class BXBaseAdapter() : RecycleAdapter<Any?, BaseHolder>() {
    companion object {
        const val HEAD_TYPE = Int.MAX_VALUE - 1
        const val FOOTER_TYPE = Int.MAX_VALUE

        val HEADER_ITEM = object : IType {
            override fun getType(): Int {
                return HEAD_TYPE
            }
        }
    }

    private val itemTypes by lazy { hashMapOf<Int, ItemTypeDelegate<*, *>>() }
    private val itemClassTypes by lazy { hashMapOf<Class<*>, Int?>() }

    var onItemClickListener: OnItemClickListener? = null
    var onItemLongClickListener: OnItemLongClickListener? = null

    var onItemChildClickListener: OnItemChildClickListener? = null
    var onItemChildLongClickListener: OnItemChildLongClickListener? = null

    init {
        registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()
                hideFooterIfEmpty()
                showHeaderIfNeed()
            }

            override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
                super.onItemRangeRemoved(positionStart, itemCount)
                hideFooterIfEmpty()
                showHeaderIfNeed()
            }

            private fun showHeaderIfNeed() {
                if (showHeader && itemCount == 0) {
                    add(0, HEADER_ITEM)
                }
            }

            private fun hideFooterIfEmpty() {
                //如果没有item，不显示footer
                if (getDataSize() == 0) {
                    showFooter = false
                }
            }
        })
    }

    @JvmOverloads
    constructor(layoutId: Int, binder: HolderBinder<*>? = null) : this() {
        val itemType = BaseItemDelegate(layoutId, binder)
        itemTypes[itemType.getType()] = itemType
    }

    override fun setData(list: List<Any?>?, forceNotifyAll: Boolean) {
        if (list.isNullOrEmpty()) return
        if (showHeader) {
            val newList = ArrayList<Any?>(list.size + 1)
            newList.add(HEADER_ITEM)
            newList.addAll(list)
            setData(newList, forceNotifyAll, false)
        } else {
            super.setData(list, forceNotifyAll)
        }
    }

    /**
     * 返回实际数据
     */
    override fun getData(): List<Any?>? {
        val dataList = super.getData()
        return if (showHeader && !dataList.isNullOrEmpty())
            dataList.subList(1, dataList.size)
        else dataList
    }

    open fun <VH : BaseHolder, DATA> addItemType(typeItemType: ItemTypeDelegate<DATA, VH>): BXBaseAdapter {
        itemTypes[typeItemType.getType()] = typeItemType
        return this
    }

    open fun <VH : BaseHolder, DATA> addItemType(type: Int, typeItemType: ItemTypeDelegate<DATA, VH>): BXBaseAdapter {
        itemTypes[type] = typeItemType
        return this
    }

    open fun <VH : BaseHolder, DATA> addItemType(clazz: Class<DATA>, typeItemType: ItemTypeDelegate<DATA, VH>): BXBaseAdapter {
        val type = clazz.hashCode()
        itemClassTypes[clazz] = type
        itemTypes[type] = typeItemType
        return this
    }

    override fun getItemCount(): Int {
        val dataCount = super.getItemCount()
        return dataCount + if (showFooter) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        if (position >= super.getListSize()) return FOOTER_TYPE
        return when (val data = getItem(position)) {
            null -> {
                super.getItemViewType(position)
            }
            is IType -> {
                data.getType()
            }
            else -> {
                val type = itemClassTypes[data::class.java]
                type ?: super.getItemViewType(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseHolder {
        val itemType = itemTypes[viewType]
        if (itemType == null) {
            val e = "TYPE_NOT_FOUND, type= $viewType, dataList: ${getData()}"
            if (BuildConfig.DEBUG) {
                throw NullPointerException(e)
            } else {
                return BaseHolder(Space(parent.context))
            }
        }
        return itemType.createHolder(parent).apply { initView() }
    }

    protected fun getItemDelegate(type: Int): ItemTypeDelegate<*, *>? {
        return itemTypes[type]
    }

    /**
     * 这个方法不会调用了 see [onBindViewHolder(holder, position, payloads)]
     */
    override fun onBindViewHolder(holder: BaseHolder, position: Int) {
        realBindViewHolder(holder, position, null)
    }

    protected open fun realBindViewHolder(holder: BaseHolder, position: Int, payloads: MutableList<Any>?) {
        if (holder.itemViewType == HEAD_TYPE) return
        val data = getItem(position) ?: return
        val typeItem = getItemDelegate(holder.itemViewType) ?: return
        this.bindHolder(typeItem, holder, data, position, payloads)
        bindItemClick(holder, data)
        bindChildCliCk(holder, data)
    }

    override fun onBindViewHolder(holder: BaseHolder, position: Int, payloads: MutableList<Any>) {
        realBindViewHolder(holder, position, payloads)
    }

    //该方法用来去除泛型问题
    @Suppress("UNCHECKED_CAST")
    protected fun <VH : BaseHolder, DATA> bindHolder(typeItem: ItemTypeDelegate<DATA, VH>, holder: BaseHolder, data: Any, position: Int, payloads: MutableList<Any>? = null) {
        if (payloads.isNullOrEmpty()) {
            typeItem.bindData(holder as VH, data as DATA, position)
        } else {
            typeItem.bindPayload(holder as VH, data as DATA, position, payloads)
        }
    }

    //<editor-fold desc="click 事件">
    protected open fun bindItemClick(holder: BaseHolder, data: Any) {
        if (onItemClickListener != null) {
            holder.itemView.setOnClickListener { onItemClickListener?.onItemClick(holder, data) }
        }
        if (onItemLongClickListener != null) {
            holder.itemView.setOnLongClickListener {
                return@setOnLongClickListener onItemLongClickListener?.onItemLongClick(holder, data)
                        ?: false
            }
        }
    }

    protected open fun bindChildCliCk(holder: BaseHolder, data: Any) {
        if (onItemChildClickListener != null && !holder.childClickViews.isNullOrEmpty()) {
            val onclickListener: View.OnClickListener = View.OnClickListener { onItemChildClickListener?.onItemChildClick(it, holder, data) }
            holder.childClickViews?.forEach { it.setOnClickListener(onclickListener) }
//            holder.childClickViews?.clear()
        }

        if (onItemChildLongClickListener != null && !holder.childLongClickViews.isNullOrEmpty()) {
            val onLongClickListener: View.OnLongClickListener = View.OnLongClickListener {
                return@OnLongClickListener onItemChildLongClickListener?.onItemChildLongClick(it, holder, data)
                        ?: false
            }
            holder.childLongClickViews?.forEach { it.setOnLongClickListener(onLongClickListener) }
//            holder.childLongClickViews?.clear()
        }
    }

    //</editor-fold>

    //<editor-fold desc="header">
    fun setHeader(layoutId: Int, binder: (BaseHolder.() -> Unit)? = null): BXBaseAdapter {
        val headerType = object : BaseItemDelegate<Any>(layoutId) {
            override fun createHolder(parent: ViewGroup): BaseHolder {
                return super.createHolder(parent).apply { binder?.invoke(this) }
            }
        }
        itemTypes[HEAD_TYPE] = headerType
        showHeader = true
        return this
    }

    fun setHeader(view: View): BXBaseAdapter {
        val headerType = object : BaseItemDelegate<Any>(0) {
            override fun createHolder(parent: ViewGroup): BaseHolder {
                if (view.parent != null) {
                    (view.parent as? ViewGroup)?.removeView(view)
                }
                return BaseHolder(view)
            }
        }
        itemTypes[HEAD_TYPE] = headerType
        showHeader = true
        return this
    }

    val hasHeader: Boolean
        get() {
            return itemTypes[HEAD_TYPE] != null
        }

    var showHeader: Boolean = false
        set(value) {
            if (field == value || !hasHeader) return
            field = value //先设置值后notify
            if (value) {
                add(0, HEADER_ITEM)
            } else {
                remove(0)
            }
        }

    //</editor-fold>

    //<editor-fold desc="footer">
    fun setFooter(layoutId: Int, binder: (BaseHolder.() -> Unit)? = null): BXBaseAdapter {
        val footerType = object : BaseItemDelegate<Any>(layoutId) {
            override fun createHolder(parent: ViewGroup): BaseHolder {
                return super.createHolder(parent).apply { binder?.invoke(this) }
            }
        }
        itemTypes[FOOTER_TYPE] = footerType
        if (getDataSize() > 0) { //有数据时默认设置显示footer，没有数据时不设置，避免notify
            showFooter = true
        }
        return this
    }

    fun setFooter(view: View): BXBaseAdapter {
        val footerType = object : BaseItemDelegate<Any>(0) {
            override fun createHolder(parent: ViewGroup): BaseHolder {
                if (view.parent != null) {
                    (view.parent as? ViewGroup)?.removeView(view)
                }
                return BaseHolder(view)
            }
        }
        itemTypes[FOOTER_TYPE] = footerType
        if (getDataSize() > 0) {//有数据时默认设置显示footer，没有数据时不设置，避免notify
            showFooter = true
        }
        return this
    }

    val hasFooter: Boolean
        get() {
            return itemTypes[FOOTER_TYPE] != null
        }

    var showFooter = false
        set(value) {
            if (field == value || !hasFooter) return
            if (value) {
                val dataSize = getListSize()
                field = value
                if (dataSize > 0) { //实际数据大于0才显示footer
                    notifyItemInserted(dataSize)
                }
            } else {
                val count = itemCount
                field = value //先设置值后notify
                if (count > 0) {
                    notifyItemRemoved(count - 1)
                }
            }
        }

    //</editor-fold>

}

interface OnItemClickListener {
    fun onItemClick(holder: BaseHolder, data: Any)
}

interface OnItemLongClickListener {
    fun onItemLongClick(holder: BaseHolder, data: Any): Boolean
}

interface OnItemChildClickListener {
    fun onItemChildClick(view: View?, holder: BaseHolder, data: Any)
}

interface OnItemChildLongClickListener {
    fun onItemChildLongClick(view: View?, holder: BaseHolder, data: Any): Boolean
}

open class BaseHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val views: SparseArray<View> by lazy { SparseArray<View>() }

    internal var childClickViews: HashSet<View>? = null
    internal var childLongClickViews: HashSet<View>? = null

    open fun initView() {
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : View?> getView(@IdRes id: Int): T? {
        if (id == -1) return null
        var view = views[id]
        if (view == null) {
            view = itemView.findViewById(id)
            if (view == null) return null
            views.put(id, view)
        }
        return view as T
    }

    open fun addChildClick(view: View?) {
        view?.let {
            if (childClickViews == null) {
                childClickViews = hashSetOf()
            }
            childClickViews!!.add(it)
        }
    }

    fun addChildClick(@IdRes viewId: Int) {
        addChildClick(getView<View>(viewId))
    }

    fun addChildLongClick(@IdRes viewId: Int) {
        addChildLongClick(getView<View>(viewId))
    }

    open fun addChildLongClick(view: View?) {
        view?.let {
            if (childLongClickViews == null) {
                childLongClickViews = hashSetOf()
            }
            childLongClickViews!!.add(view)
        }
    }

}


