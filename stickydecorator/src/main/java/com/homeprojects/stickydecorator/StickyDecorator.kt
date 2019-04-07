package com.homeprojects.stickydecorator

import android.graphics.Canvas
import android.support.v7.widget.RecyclerView
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup

private const val ZERO_POSITION_Y = 0f
private const val ZERO_POSITION_X = 0f
private const val CACHED_ELEMENT_KEY = 0

typealias Holder = RecyclerView.ViewHolder

class RecyclerViewDecorator<P : Enum<P>>(
    types: List<P>
) : RecyclerView.ItemDecoration() {

    private val types: List<Int> = types.map { it.ordinal }
    private val cachedHolders = SparseArray<RecyclerView.ViewHolder>(types.size)
    private var isDownScroll = false
    private var yTranslation = 0f

    private val scroller = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            isDownScroll = dy >= 0
        }
    }

    private var isAdded = false

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        parent.adapter?.let {
            cacheViewHolders(parent)
            if (!isAdded) {
                isAdded = true
                parent.addOnScrollListener(scroller)
            }
            if (isDownScroll) {
                drawUpScroll(c, parent, state)
            } else {
                drawDownScroll(c, parent, state)
            }
        }
    }

    private fun cacheViewHolders(parent: RecyclerView) {
        types.forEach {
            if (cachedHolders.get(it) == null) {
                createViewHolder(parent, it)?.let { vh -> cachedHolders.put(it, vh) }
            }
        }
    }

    private fun drawDownScroll(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val topHolder = findHolderOnTop(parent)
        val currentView = topHolder?.itemView
        val underTopVh = findViewHolderUnderSticky(parent,currentView?.bottom ?: 0)
        val currentPosition = topHolder?.adapterPosition ?: 0
        val nextHolder: RecyclerView.ViewHolder? = findHeaderByType(parent, currentPosition)
        val secondHolder = findHolderUnderZeroPosition(parent, underTopVh!!)

        if (secondHolder?.itemViewType in types) {
            val offset = nextHolder?.let { calculateCurrentOffset(it, secondHolder!!) } ?: 0f
            yTranslation = if (offset <= 0)
                offset
            else ZERO_POSITION_Y
        }

        c.translate(ZERO_POSITION_X, yTranslation)

        nextHolder?.let {
            drawNewStickyHolder(it, c, state)
        } ?: run {
            state.remove(CACHED_ELEMENT_KEY)
        }
    }

    private fun drawUpScroll(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val viewHolder = findHolderOnTop(parent)
        val topView = viewHolder?.itemView
        state.get<Holder>(CACHED_ELEMENT_KEY)?.let {
            val secondHolder = findViewHolderUnderSticky(parent,topView?.bottom ?: 0)
            if (secondHolder?.itemViewType in types) {
                yTranslation = calculateCurrentOffset(it, secondHolder!!)
                if (yTranslation <= 0)
                    c.translate(ZERO_POSITION_X, yTranslation)
            }
        }

        viewHolder?.let {
            if (it.itemViewType in types) {
                val cachedVh = getHolderFromCache(it, parent)
                drawNewStickyHolder(cachedVh, c, state)
            } else {
                drawFromState(state, c)
            }
        }
    }

    private fun calculateCurrentOffset(sticky: Holder, underSticky: Holder): Float {
        return -sticky.itemView.height + underSticky.itemView.y
    }

    private fun getHolderFromCache(viewHolder: Holder, parent: RecyclerView): Holder {
        val cachedVh = cachedHolders.get(viewHolder.itemViewType) ?: viewHolder
        parent.adapter?.onBindViewHolder(cachedVh, viewHolder.adapterPosition)
        return cachedVh
    }

    private fun drawNewStickyHolder(
        viewHolder: Holder,
        canvas: Canvas,
        state: RecyclerView.State
    ) {
        yTranslation = ZERO_POSITION_Y
        viewHolder.itemView.draw(canvas)
        state.put(CACHED_ELEMENT_KEY, viewHolder)
    }

    private fun drawFromState(state: RecyclerView.State, canvas: Canvas) {
        state.get<Holder>(CACHED_ELEMENT_KEY)?.also {
            it.itemView.draw(canvas)
        }
    }

    private fun findHeaderByType(
        parent: RecyclerView,
        currentPosition: Int
    ): Holder? {
        var viewHolder: Holder? = null
        for (i in currentPosition downTo 0) {
            val currentType = parent.adapter?.getItemViewType(i)!!
            if (currentType in types) {
                viewHolder = if (cachedHolders.valueAt(currentType) != null) {
                    cachedHolders[currentType]
                } else {
                    createViewHolder(parent, currentType)
                }
                viewHolder?.let { parent.adapter?.onBindViewHolder(it, i) }
                break
            }
        }
        return viewHolder
    }

    private fun createViewHolder(parent: RecyclerView, currentType: Int): Holder? {
        return parent.adapter?.onCreateViewHolder(parent, currentType)?.apply {
            createView(itemView, parent)
        }
    }

    private fun createView(view: View, parent: RecyclerView) {
        val params = view.layoutParams
        val specWidth = measureSpec(params?.width ?: 0, parent.width)
        val specHeight = measureSpec(params?.height ?: 0, parent.height)
        view.measure(specWidth, specHeight)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
    }

    private fun measureSpec(spec: Int, maxDim: Int): Int {
        return when (spec) {
            ViewGroup.LayoutParams.MATCH_PARENT -> View.MeasureSpec.makeMeasureSpec(maxDim, View.MeasureSpec.EXACTLY)
            ViewGroup.LayoutParams.WRAP_CONTENT -> View.MeasureSpec.makeMeasureSpec(maxDim, View.MeasureSpec.AT_MOST)
            else -> View.MeasureSpec.makeMeasureSpec(spec, View.MeasureSpec.EXACTLY)
        }
    }

    private fun findHolderOnTop(parent: RecyclerView): Holder? {
        return parent.findChildViewUnder(ZERO_POSITION_X, ZERO_POSITION_Y)?.let {
            parent.findContainingViewHolder(it)
        }
    }

    private fun findHolderUnderZeroPosition(parent: RecyclerView, typedHolder: Holder): Holder? {
        return parent.findChildViewUnder(ZERO_POSITION_X, typedHolder.itemView.y)?.let {
            parent.getChildViewHolder(it)
        }
    }

    private fun findViewHolderUnderSticky(parent: RecyclerView, currentYOffset: Int): Holder? {
        val offset = currentYOffset.toFloat()
        return parent.findChildViewUnder(ZERO_POSITION_X, offset)?.let {
            parent.findContainingViewHolder(it)
        }
    }
}