package com.basic.ui.list

import android.view.ViewGroup

/**
 * RecycleView 条目数据类型接口
 */
interface IType {
    fun getType(): Int
}

interface ItemTypeDelegate<in T, VH : BaseHolder>: IType {
    fun bindData(holder: VH, data: T, position: Int)
    fun bindPayload(holder: VH, data: T, position: Int, payloads:List<Any>){}
    fun createHolder(parent: ViewGroup): VH
}

open class BaseItemDelegate<T>(val layoutId: Int) : ItemTypeDelegate<T, BaseHolder> {
    private var binder: HolderBinder<T>? = null

    constructor(layoutId: Int, binder: HolderBinder<T>?) : this(layoutId) {
        this.binder = binder
    }

    override fun bindData(holder: BaseHolder, data: T, position: Int) {
        binder?.invoke(holder, data, position)
    }

    override fun createHolder(parent: ViewGroup): BaseHolder {
        return BaseHolder(createView(parent, layoutId))
    }

    override fun getType(): Int {
        return 0
    }

}

typealias HolderBinder<Data> = BaseHolder.(data: Data, pos: Int) -> Unit










