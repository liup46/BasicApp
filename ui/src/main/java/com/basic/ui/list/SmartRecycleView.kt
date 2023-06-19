// package com.peter.vunit.list
//
// import android.content.Context
// import android.text.TextUtils
// import android.util.AttributeSet
// import android.view.View
// import android.view.ViewGroup
// import androidx.activity.ComponentActivity
// import androidx.fragment.app.Fragment
// import androidx.lifecycle.Lifecycle
// import androidx.lifecycle.LifecycleOwner
// import androidx.recyclerview.widget.RecyclerView
// import com.bx.bxui.widget.BxNoMoreDataView
// import com.bx.core.R
// import com.bx.core.base.list.adapter.*
// import com.bx.core.base.list.sticky.CustomerViewHeaderCallback
// import com.bx.core.base.list.sticky.StickyScrollListener
// import com.bx.core.base.viewmodel.BXBaseViewModel
// import com.bx.core.base.viewmodel.observeResult
// import com.bx.core.base.viewmodel.subscribeResult
// import com.bx.core.net.ApiResponse
// import com.bx.repository.model.base.PageModel
// import com.ypp.net.bean.ResponseResult
// import com.yupaopao.android.statemanager.StateLayout
// import com.yupaopao.android.statemanager.state.CoreState
// import com.yupaopao.android.statemanager.state.StateProperty
// import com.yupaopao.lux.base.createViewModel
// import com.yupaopao.lux.base.state.BaseEmptyState
// import com.yupaopao.lux.base.state.BaseLoadingState
// import com.yupaopao.lux.base.state.BaseNetErrorState
// import com.yupaopao.refresh.layout.SmartRefreshLayout
// import com.yupaopao.refresh.layout.listener.OnLoadMoreListener
// import com.yupaopao.refresh.layout.listener.OnRefreshListener
// import io.reactivex.Flowable
//
// /**
//  * @author Peter Liu
//  * @since 2020-01-17 17:41
//  */
// class SmartRecycleView @JvmOverloads constructor(
//         context: Context, attrs: AttributeSet? = null
// ) : SmartRefreshLayout(context, attrs) {
//
//     companion object {
//         private const val SIMPLE_LIST_REQUEST = "simple_list_request"
//     }
//
//     /**
//      *  request 标记，每个view 生成一个唯一的Tag,
//      *  避免一个页面存在多个SmartRecycleView时，liveData复用导致的问题
//      */
//     private val requestTag by lazy {  SIMPLE_LIST_REQUEST+ Integer.toHexString(hashCode())}
//
//     val recyclerView: RecyclerView?
//     val stateView: StateLayout?
//
//     private var isAttachedWindow = false
//
//     /**
//      * 是否正在请求
//      */
//     private var isRequesting = false
//
//     /**
//      * 是否在刷新
//      */
//     private var isRefreshing = false
//
//     /**
//      * 是不是第一页数据
//      */
//     var isFirstPage = true
//         protected set
//
//     /**
//      * 滚动到还剩下多少个条目是开始预加载，默认 为 5 预加载。
//      * 大于1时，且enableLoadMore = true 时启动预加载功能
//      * 例如 返回 5 表示 滚动到还剩下5个条目时开始预加载。
//      * <=1 时关闭预加载
//      */
//     @JvmField
//     var preLoadOffset = 5
//
//     /**
//      * 是否分页结束
//      */
//     var pageEnd = false //true：分页请求没有更多分页了
//         protected set
//     var pageNum: Int = 0 //兼容pageNum的分页请求
//         protected set
//     var anchor: String? = "" //以anchor 作为参数的分页请求
//         protected set
//
//     //是否支持加载更多, 设置为 false 则不管有没有更多都不能加载更多。
//     //与setEnableLoadMore 不同
//     var supportLoadMore = false
//
//     val adapterDataObserver = object : RecyclerView.AdapterDataObserver() {
//         override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
//             super.onItemRangeRemoved(positionStart, itemCount)
//             checkIfShowEmpty()
//         }
//
//         override fun onChanged() {
//             super.onChanged()
//             checkIfShowEmpty()
//         }
//
//         private fun checkIfShowEmpty() {
//             if (adapter.getDataSize() == 0) {
//                 showEmpty.invoke()
//             }
//         }
//     }
//
//     var adapter: BXBaseAdapter = BXBaseAdapter()
//         set(value) {
//             field.unregisterAdapterDataObserver(adapterDataObserver)
//             field = value
//             recyclerView?.adapter = value
//             field.registerAdapterDataObserver(adapterDataObserver)
//         }
//
//     init {
//         View.inflate(context, R.layout.bx_smart_list, this)
//
//         /**
//          * 预加载监听器
//          */
//         val preLoadScrollListener: RecyclerView.OnScrollListener = object : RecyclerView.OnScrollListener() {
//             override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
//                 if (!supportLoadMore || !mEnableLoadMore || pageEnd || recyclerView.adapter == null || !isAttachedWindow || preLoadOffset <= 1) return
//                 val pos = getLastVisibleItemPosition(recyclerView)
//                 if (pos < recyclerView.adapter!!.itemCount - preLoadOffset) return
//                 if (!isRequesting) {
//                     requestData()
//                 }
//             }
//         }
//
//         recyclerView = findViewById<RecyclerView>(R.id.recyclerView)?.apply {
//             addOnScrollListener(preLoadScrollListener)
//             this.adapter = this@SmartRecycleView.adapter
//             this.adapter?.registerAdapterDataObserver(adapterDataObserver)
//         }
//
//         stateView = findViewById<StateLayout>(R.id.listState)?.apply {
//             setStateEventListener { state, _ ->
//                 if (TextUtils.equals(state, BxNetErrorState.EVENT_CLICK)) {
//                     loadData()
//                 }
//             }
//         }
//     }
//
//     protected var onRefreshListener = OnRefreshListener {
//         if (isAttachedWindow) {
//             refreshData()
//         }
//     }
//
//     protected var onLoadMoreListener = OnLoadMoreListener {
//         if (isAttachedWindow) {
//             loadMore()
//         }
//     }
//
//     /**
//      * 请求数据的方法，
//      */
//     @JvmField
//     var request: (() -> Unit)? = null
//     private var lifecycleOwner: LifecycleOwner? = null
//     var baseViewModel: BXBaseViewModel? = null
//         protected set
//
//     /**
//      * 请求成功的回调
//      */
//     @JvmField
//     var onRequestSuccess: OnResultSuccess<Any> = this::setData
//
//     /**
//      * 请求失败的回调
//      */
//     @JvmField
//     var onRequestError: ((apiResponse: ApiResponse<*>) -> Unit)? = {
//         onRequestError(it)
//     }
//
//     /**
//      * 请求成功，但返回模型为空的回调
//      */
//     @JvmField
//     var onRequestEmpty: (() -> Unit)? = {
//         onRequestEmpty()
//     }
//
//     /**
//      * 请求成功，数据准备好了，更新adapter吧
//      *
//      * firstPage 是不是第一个页数据
//      */
//     @JvmField
//     var onListReady: OnListReady<*>? = { list, firstPage ->
//         onListReady(firstPage, list)
//     }
//
//     /**
//      * 请求成功，但列表数据为空时的回调
//      * 默认第一页数据为空显示空态页，非第一页不处理
//      *
//      * firstPage 是不是第一个页数据
//      */
//     @JvmField
//     var onListEmpty: (isFirstPage: Boolean) -> Unit = {
//         onListEmpty(it)
//     }
//
//     @JvmField
//     var onListNoMore: (() -> Unit)? = {
//         showFooter(true)
//     }
//
//     @JvmField
//     var showNormal: (() -> Unit) = { showNormal() }
//
//     @JvmField
//     var showLoading: (() -> Unit) = { showLoading() }
//
//     @JvmField
//     var showEmpty: (() -> Unit) = { showEmpty() }
//
//     @JvmField
//     var showError: ((e: ApiResponse<*>) -> Unit) = { showError() }
//
//     /**
//      * 初始化SmartRefreshLayout 比如： enableRefresh enableLoadMore preLoadOffset,或者其他SmartRefreshLayout属性
//      */
//     fun setupSmartRefresh(initFun: SmartRefreshLayout.() -> Unit): SmartRecycleView {
//         initFun.invoke(this)
//         return this
//     }
//
//     /**
//      * 初始化RecyclerView 比如设置 Adapter, layoutManager or addItemDecoration，给RecyclerView设置背景
//      * by default no itemDecoration and is LinearLayoutManager
//      */
//     fun setupRecycleView(initFun: RecyclerView.() -> Unit): SmartRecycleView {
//         recyclerView?.let {
//             initFun.invoke(it)
//         }
//         return this
//     }
//
//     fun setAdapter(adapter: BXBaseAdapter): SmartRecycleView {
//         this.adapter = adapter
//         return this
//     }
//
//     override fun setOnRefreshListener(listener: OnRefreshListener?): SmartRecycleView {
//         this.onRefreshListener = OnRefreshListener {
//             if (isAttachedWindow) {
//                 listener?.onRefresh(this)
//             }
//         }
//
//         super.setOnRefreshListener(onRefreshListener)
//         return this
//     }
//
//     override fun setOnLoadMoreListener(listener: OnLoadMoreListener?): SmartRecycleView {
//         this.onLoadMoreListener = OnLoadMoreListener {
//             if (isAttachedWindow) {
//                 listener?.onLoadMore(this)
//             }
//         }
//         super.setOnLoadMoreListener(onLoadMoreListener)
//         return this
//     }
//
//     override fun setEnableLoadMore(enabled: Boolean): SmartRecycleView {
//         if (enabled) {
//             supportLoadMore = true
//         }
//         super.setEnableLoadMore(enabled)
//         setEnableAutoLoadMore(enabled)
//         if (enabled) {
//             super.setOnLoadMoreListener(onLoadMoreListener)
//         } else {
//             super.setOnLoadMoreListener(null)
//         }
//         return this
//     }
//
//     override fun setEnableRefresh(enabled: Boolean): SmartRecycleView {
//         super.setEnableRefresh(enabled)
//         setEnableHeaderTranslationContent(enabled)
//         if (enabled) {
//             super.setOnRefreshListener(onRefreshListener)
//         } else {
//             super.setOnRefreshListener(null)
//         }
//         return this
//     }
//
//     fun setPreLoadOffset(offset: Int): SmartRecycleView {
//         preLoadOffset = offset
//         return this
//     }
//
//     override fun onAttachedToWindow() {
//         super.onAttachedToWindow()
//         isAttachedWindow = true
//     }
//
//     override fun onDetachedFromWindow() {
//         super.onDetachedFromWindow()
//         isAttachedWindow = false
//     }
//
//     //auto refresh 支持是否显示loading 态
//     fun autoRefresh(showLoading: Boolean): Boolean {
//         if (showLoading) {
//             this.showLoading.invoke()
//         }
//         return super.autoRefresh()
//     }
//
//     //<editor-fold desc="触发请求方法">
//     /**
//      * 调用 loadData or smartRefresh.autoRefresh() 发起数据请求
//      *
//      * 该方法会显示loading态
//      *
//      */
//     @JvmOverloads
//     open fun loadData(showLoading: Boolean = true) {
//         if (showLoading) {
//             this.showLoading.invoke()
//         }
//         requestData()
//     }
//
//     /**
//      * 发起数据请求，该方法不会显示loading态
//      */
//     open fun requestData() {
//         isRequesting = true
//         request?.invoke()
//     }
//
//     /**
//      * 刷新数据并请求
//      */
//     @JvmOverloads
//     open fun refreshData(showLoading: Boolean = false) {
//         if (isRefreshing) {
//             return
//         }
//         isRefreshing = true
//         clearState()
//         loadData(showLoading)
//     }
//
//     fun clearState() {
//         isFirstPage = true
//         pageNum = 0
//         pageEnd = false
//         anchor = ""
//     }
//
//     /**
//      *  加载更多并请求
//      */
//     open fun loadMore() {
//         if (!pageEnd && !isRequesting) {
//             requestData()
//         }
//     }
//     //</editor-fold>
//
//     /**
//      * 请求数据的方法
//      * 调用此方法必须要在列表数据请求返回时调用[setDataList]去刷新列表数据
//      */
//     fun setRequest(request: () -> Unit): SmartRecycleView {
//         this.request = request
//         return this
//     }
//
//     //<editor-fold desc="默认请求处理方法">
//     /**
//      * 请求数据的方法
//      * 只需设置retrofit api 请求即可自动完成所有处理
//      *
//      * @param lifecycleOwner: ComponentActivity 或者Fragment
//      */
//     fun setRequest(lifecycleOwner: LifecycleOwner, request: () -> Flowable<*>?): SmartRecycleView {
//         this.request = {
//             if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
//                 subscribeRequest(lifecycleOwner, request.invoke() as? Flowable<ResponseResult<Any>>, onRequestSuccess)
//             }
//         }
//         return this
//     }
//
//     fun <T> setRequest(lifecycleOwner: LifecycleOwner, request: () -> Flowable<ResponseResult<T>>?, onData: (t: T) -> Unit = { onRequestSuccess(it as Any) }, showError: ((e: ApiResponse<T>) -> Unit)? = onRequestError, showEmpty: (() -> Unit)? = onRequestEmpty): SmartRecycleView {
//         this.request = {
//             if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
//                 subscribeRequest(lifecycleOwner, request.invoke(), onData, showError, showEmpty)
//             }
//         }
//         return this
//     }
//
//     @JvmOverloads
//     fun <T> subscribeRequest(lifecycleOwner: LifecycleOwner, flowable: Flowable<ResponseResult<T>>?, onData: (t: T) -> Unit, showError: ((e: ApiResponse<T>) -> Unit)? = onRequestError, showEmpty: (() -> Unit)? = onRequestEmpty) {
//         flowable?.doOnError { isRequesting = false; isRefreshing = false }
//                 ?.subscribeResult(getViewModel(lifecycleOwner), requestTag, false)
//                 ?.observeResult(lifecycleOwner, { onData(it) },
//                         { showError?.invoke(it) }, { showEmpty?.invoke() })
//     }
//
//     fun getViewModel(lifecycleOwner: LifecycleOwner): BXBaseViewModel {
//         if (baseViewModel == null) {
//             this.lifecycleOwner = lifecycleOwner
//             if (lifecycleOwner is ComponentActivity) {
//                 baseViewModel = createViewModel(lifecycleOwner)
//             } else if (lifecycleOwner is Fragment) {
//                 baseViewModel = createViewModel(lifecycleOwner)
//             }
//         }
//         return baseViewModel!!
//     }
//
//     /**
//      * 请求成功的回调
//      */
//     fun <T> setOnRequestSuccess(onRequestSuccess: OnResultSuccess<T>): SmartRecycleView {
//         this.onRequestSuccess = onRequestSuccess as OnResultSuccess<Any>
//         return this
//     }
//
//     /**
//      * 请求失败的回调
//      */
//     fun setOnRequestError(onRequestError: (e: ApiResponse<*>) -> Unit): SmartRecycleView {
//         this.onRequestError = onRequestError
//         return this
//     }
//
//     /**
//      * 显示数据的回调，用于给adapter设置数据
//      *
//      * firstPage 是不是第一个页数据
//      */
//     fun <T> setOnListReady(onResultReady: OnListReady<T>): SmartRecycleView {
//         this.onListReady = onResultReady as OnListReady<*>
//         return this
//     }
//
//     /**
//      * 当列表数据为空时的回调，默认第一页数据为空显示空态页，非第一页不处理
//      *
//      * firstPage 是不是第一个页数据
//      */
//     fun setOnListEmpty(onListEmpty: (firstPage: Boolean) -> Unit): SmartRecycleView {
//         this.onListEmpty = onListEmpty
//         return this
//     }
//
//     /**
//      * 当没有更多数据的回调，可用于显示Footer
//      */
//     fun setOnListNoMore(onListNoMore: () -> Unit): SmartRecycleView {
//         this.onListNoMore = onListNoMore
//         return this
//     }
//
//     open fun <T> setData(data: T) {
//         if (data is PageModel<*>) {
//             setDataList(data.list, data.end, data.anchor)
//         } else if (data is List<*>) {
//             setDataList(data)
//         }
//     }
//
//     /**
//      * 当接口返回后调用该方法
//      *
//      * @param dataList 数据列表
//      * @param pageEnd 是否没有更多分页，如果分页请求成功一定要传，其他情况可不传, 不能加载更多时默认为true
//      * @param anchor 为了兼容以anchor为参数当请求，其他情况可不传
//      */
//     @JvmOverloads
//     open fun <T> setDataList(dataList: List<T>?, pageEnd: Boolean = !supportLoadMore, anchor: String? = "") {
//         finishLoad()
//         this.pageEnd = pageEnd
//         if (supportLoadMore) {
//             pageNum++
//             this.anchor = anchor
//         }
//         setEnableLoadMore(supportLoadMore && !pageEnd)
//         if (dataList.isNullOrEmpty()) {
//             onListEmpty.invoke(isFirstPage)
//             return
//         }
//         showNormal.invoke()
//         onListReady?.invoke(dataList, isFirstPage)
//         if (pageEnd) {
//             onListNoMore?.invoke()
//         } else {
//             showFooter(false)
//         }
//         isFirstPage = false
//     }
//
//     fun finishLoad() {
//         isRequesting = false
//         isRefreshing = false
//         if (!isAttachedWindow) {
//             return
//         }
//         if (mEnableRefresh) {
//             finishRefresh()
//         }
//         if (mEnableLoadMore) {
//             finishLoadMore()
//         }
//     }
//
//     /**
//      * 请求失败的回调
//      */
//     open fun onRequestError(apiResponse: ApiResponse<*>) {
//         finishLoad()
//         if (isFirstPage) {
//             showError.invoke(apiResponse)
//             setEnableLoadMore(false)
//         }
//     }
//
//     /**
//      * 请求成功，但返回模型为空的回调
//      */
//     open fun onRequestEmpty() {
//         finishLoad()
//         if (isFirstPage) {
//             showEmpty.invoke()
//             setEnableLoadMore(false)
//         }
//     }
//
//     open fun onListReady(firstPage: Boolean, list: List<*>) {
//         onListReady(firstPage, list, true)
//     }
//
//     open fun onListReady(firstPage: Boolean, list: List<*>, forceUpdate: Boolean = true) {
//         if (firstPage) {
//             adapter.setData(list, forceUpdate)
//         } else {
//             adapter.addAll(list)
//         }
//     }
//
//     /**
//      * 当列表数据为空时的回调，默认第一页数据为空显示空态页，非第一页不处理
//      *
//      * firstPage 是不是第一个页数据
//      */
//     open fun onListEmpty(firstPage: Boolean) {
//         if (firstPage) {
//             showEmpty.invoke()
//         } else {
//             onListNoMore?.invoke()
//         }
//         if (supportLoadMore) {
//             setEnableLoadMore(false)
//         }
//     }
//     //</editor-fold>
//
//     //<editor-fold desc="默认显示状态方法 loading error empty normal state">
//     fun showStatus(stateProperty: StateProperty) {
//         stateView?.showState(stateProperty)
//     }
//
//     fun showStatus(stateProperty: String) {
//         stateView?.showState(stateProperty)
//     }
//
//     @JvmOverloads
//     open fun showNormal(stateProperty: StateProperty? = null) {
//         if (stateProperty == null) {
//             showStatus(CoreState.STATE)
//         } else {
//             showStatus(stateProperty)
//         }
//     }
//
//     @JvmOverloads
//     open fun showLoading(loadingMo: BaseLoadingState.BaseLoadingMo? = null) {
//         if (loadingMo == null) {
//             showStatus(BaseLoadingState.STATE)
//         } else {
//             showStatus(loadingMo)
//         }
//     }
//
//     @JvmOverloads
//     open fun showEmpty(emptyMo: BaseEmptyState.BaseEmptyMo? = null) {
//         if (emptyMo == null) {
//             showStatus(BaseEmptyState.STATE)
//         } else {
//             showStatus(emptyMo)
//         }
//     }
//
//     @JvmOverloads
//     open fun showError(netErrorMo: BaseNetErrorState.BaseNetErrorMo? = null) {
//         if (netErrorMo == null) {
//             showStatus(BaseNetErrorState.STATE)
//         } else {
//             showStatus(netErrorMo)
//         }
//     }
//     //</editor-fold>
//
//     /**
//      * 设置如何显示请求正常态，默认显示内部的正常态，
//      * 只有当重写了[setShowError],[setShowEmpty], [setShowLoading]时才需要重写该方法
//      */
//     fun setShowNormal(showNormal: () -> Unit): SmartRecycleView {
//         this.showNormal = showNormal
//         return this
//     }
//
//     /**
//      * 设置如何显示请求错误页，默认显示通用错误页
//      *
//      * see [showNormal]
//      */
//     fun setShowError(showError: (e: ApiResponse<*>) -> Unit): SmartRecycleView {
//         this.showError = showError
//         return this
//     }
//
//     /**
//      * 设置如何显示空白页，默认显示通用空白页
//      *
//      *  see [showNormal]
//      */
//     fun setShowEmpty(showEmpty: () -> Unit): SmartRecycleView {
//         this.showEmpty = showEmpty
//         return this
//     }
//
//     /**
//      * 设置如何显示loading页，默认显示通用loading页
//      *
//      * see [showNormal]
//      */
//     fun setShowLoading(showLoading: () -> Unit): SmartRecycleView {
//         this.showLoading = showLoading
//         return this
//     }
//
//     //<editor-fold desc="内部支持BXBaseAdapter">
//
//     fun <DATA> setItemType(layoutId: Int, binder: HolderBinder<DATA>): SmartRecycleView {
//         adapter.addItemType(BaseItemDelegate(layoutId, binder))
//         return this
//     }
//
//     //设置条目
//     fun <VH : BaseHolder, DATA> setItemType(typeItemType: ItemTypeDelegate<DATA, VH>): SmartRecycleView {
//         adapter.addItemType(typeItemType)
//         return this
//     }
//
//     fun <VH : BaseHolder, DATA> addItemType(typeItemType: ItemTypeDelegate<DATA, VH>): SmartRecycleView {
//         adapter.addItemType(typeItemType)
//         return this
//     }
//
//     fun <VH : BaseHolder, DATA> addItemType(type: Int, typeItemType: ItemTypeDelegate<DATA, VH>): SmartRecycleView {
//         adapter.addItemType(type, typeItemType)
//         return this
//     }
//
//     fun <VH : BaseHolder, DATA> addItemType(clazz: Class<DATA>, typeItemType: ItemTypeDelegate<DATA, VH>): SmartRecycleView {
//         adapter.addItemType(clazz, typeItemType)
//         return this
//     }
//
//     fun setFooter(layoutId: Int, binder: (BaseHolder.() -> Unit)? = null): SmartRecycleView {
//         adapter.setFooter(layoutId, binder)
//         return this
//     }
//
//     fun setFooter(view: View): SmartRecycleView {
//         adapter.setFooter(view)
//         return this
//     }
//
//     fun showFooter(show: Boolean) {
//         adapter.showFooter = show
//     }
//
//     fun setHeader(layoutId: Int, binder: (BaseHolder.() -> Unit)? = null): SmartRecycleView {
//         adapter.setHeader(layoutId, binder)
//         return this
//     }
//
//     fun setHeader(view: View): SmartRecycleView {
//         adapter.setHeader(view)
//         return this
//     }
//
//     fun showHeader(show: Boolean) {
//         adapter.showHeader = show
//     }
//
//     /**
//      * Set 条目点击事情处理
//      * @param onItemClickListener OnItemClickListener
//      * @return SmartListView
//      */
//     fun setOnItemClickListener(onItemClickListener: OnItemClickListener?): SmartRecycleView {
//         adapter.onItemClickListener = onItemClickListener
//         return this
//     }
//
//     fun setOnItemLongClickListener(onItemClickListener: OnItemLongClickListener?): SmartRecycleView {
//         adapter.onItemLongClickListener = onItemClickListener
//         return this
//     }
//
//     fun setOnItemChildClickListener(onItemClickListener: OnItemChildClickListener?): SmartRecycleView {
//         adapter.onItemChildClickListener = onItemClickListener
//         return this
//     }
//
//     fun setOnItemChildLongClickListener(onItemClickListener: OnItemChildLongClickListener?): SmartRecycleView {
//         adapter.onItemChildLongClickListener = onItemClickListener
//         return this
//     }
//     //</editor-fold>
//
//
//     open fun getLastVisibleItemPosition(recyclerView: RecyclerView): Int {
//         val layoutManager = recyclerView.layoutManager ?: return -1
//         return getVerticalLastVisibleItemPosition(layoutManager)
//     }
//
//     private fun getVerticalLastVisibleItemPosition(layoutManager: RecyclerView.LayoutManager): Int {
//         with(layoutManager) {
//             val rvEnd: Int = height - paddingBottom
//             val start = childCount - 1
//             var visible: View? = null
//             for (i in start downTo 0) {
//                 val child = getChildAt(i) ?: continue
//                 val params = child.layoutParams as RecyclerView.LayoutParams?
//                 if (params != null) {
//                     val childStart = getDecoratedTop(child) - params.topMargin
//                     val childEnd: Int = getDecoratedBottom(child) + params.bottomMargin
//                     if (childStart < rvEnd || childEnd >= rvEnd) {
//                         visible = child
//                         break
//                     }
//                 }
//             }
//             return if (visible == null) RecyclerView.NO_POSITION else getPosition(visible)
//         }
//     }
// }
//
// typealias OnListReady<T> = (list: List<T>, firstPage: Boolean) -> Unit
// typealias OnResultSuccess<T> = (t: T) -> Unit
//
// @JvmOverloads
// fun SmartRecycleView.showBxNoMoreFooter(text: String? = null): SmartRecycleView {
//     setFooter(BxNoMoreDataView(context).apply {
//         layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
//         if (!text.isNullOrBlank()) {
//             setContent(text)
//         }
//     })
//     return this
// }
//
// fun SmartRecycleView.setOnItemClickListener(itemClick: (holder: BaseHolder, data: Any) -> Unit): SmartRecycleView {
//     setOnItemClickListener(object : OnItemClickListener {
//         override fun onItemClick(holder: BaseHolder, data: Any) {
//             itemClick.invoke(holder, data)
//         }
//     })
//     return this
// }
//
// fun SmartRecycleView.setOnItemLongClickListener(itemLongClick: (holder: BaseHolder, data: Any) -> Boolean): SmartRecycleView {
//     setOnItemLongClickListener(object : OnItemLongClickListener {
//         override fun onItemLongClick(holder: BaseHolder, data: Any): Boolean {
//             return itemLongClick.invoke(holder, data)
//         }
//     })
//     return this
// }
//
// fun SmartRecycleView.setOnItemChildClickListener(onItemChildClick: (view: View?, holder: BaseHolder, data: Any) -> Unit): SmartRecycleView {
//     setOnItemChildClickListener(object : OnItemChildClickListener {
//         override fun onItemChildClick(view: View?, holder: BaseHolder, data: Any) {
//             onItemChildClick(view, holder, data)
//         }
//     })
//     return this
// }
//
// fun SmartRecycleView.setOnItemChildLongClickListener(onItemChildLongClick: (view: View?, holder: BaseHolder, data: Any) -> Boolean): SmartRecycleView {
//     setOnItemChildLongClickListener(object : OnItemChildLongClickListener {
//         override fun onItemChildLongClick(view: View?, holder: BaseHolder, data: Any): Boolean {
//             return onItemChildLongClick(view, holder, data)
//         }
//     })
//     return this
// }
//
// //添加sticky header
// fun SmartRecycleView.setStickHeaderCallback(callback: CustomerViewHeaderCallback): SmartRecycleView {
//     recyclerView?.addOnScrollListener(StickyScrollListener(recyclerView, callback))
//     return this
// }
//
