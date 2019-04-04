package com.homeprojects.stickydecorator

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import java.util.*

private const val ZERO_POSTION_Y = 0f
private const val ZERO_POSITION_X = 0f
private const val CACHED_ELEMENT_KEY = 0

// todo delete bitmap, add holder cache for types, cause bitmap has native allocations - too bad for performance
class RecyclerViewDecorator<P : Enum<P>>(
    types: List<P>
) : RecyclerView.ItemDecoration() {

    private val types: List<Int> = types.map { it.ordinal }
    private val cachedHolders = HashMap<Int, RecyclerView.ViewHolder>(types.size)
    private var isDownScroll = false
    private var yTranslation = 0f

    private val scroller = object : RecyclerView.OnScrollListener() {

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            isDownScroll = dy >= 0
        }
    }

    private var isAdded = false

    // todo check should draw if no header on top
    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        parent.adapter?.let {
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

    private fun drawDownScroll(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val topHolder = findHolderOnTop(parent)
        val currentView = topHolder?.itemView
        val secondHolder = findSecondHolder(parent, currentView)
        val currentPosition = topHolder?.adapterPosition ?: 0
        val nextHolder: RecyclerView.ViewHolder? = findHeaderByType(parent, currentPosition)

        if (secondHolder?.itemViewType in types) yTranslation = currentView?.y ?: 0f

        c.translate(0f, yTranslation)

        // todo check it
//        if (topHolder?.itemViewType in types) {
//            drawFromState(state, c)
//            return
//        }

        nextHolder?.let {
            drawNewStickyHolder(it, c, state)
        } ?: run {
            drawFromState(state, c)
        }
    }

    private fun drawUpScroll(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val viewHolder = findHolderOnTop(parent)
        val topView = viewHolder?.itemView
        val nextHolder = findSecondHolder(parent, topView)
        val nextView = nextHolder?.itemView

        state.get<Bitmap>(0)?.let {
            if (nextHolder?.itemViewType in types) {
                yTranslation = -nextView?.height!! + nextView.y
                c.translate(0f, yTranslation)
            }
        }

        viewHolder?.let {
            if (it.itemViewType in types) {
                drawNewStickyHolder(viewHolder, c, state)
            } else {
                drawFromState(state, c)
            }
        }

    }

    private fun findHeaderByType(
        parent: RecyclerView,
        currentPosition: Int
    ): RecyclerView.ViewHolder? {
        var viewHolder: RecyclerView.ViewHolder? = null
        for (i in currentPosition downTo 0) {
            val currentType = parent.adapter?.getItemViewType(i)
            if (currentType in types) {
                viewHolder = if (cachedHolders.containsKey(currentType)) {
                    return cachedHolders[currentType]
                } else createViewHolder(parent, currentType!!)
                viewHolder?.let { parent.adapter?.onBindViewHolder(viewHolder, i) }
                break
            }
        }
        return viewHolder
    }

    private fun createViewHolder(parent: RecyclerView, currentType: Int): RecyclerView.ViewHolder? {
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

    private fun findHolderOnTop(parent: RecyclerView) = parent.findChildViewUnder(0f, 0f)?.let {
        parent.findContainingViewHolder(it)
    }

    private fun findSecondHolder(parent: RecyclerView, currentView: View?): RecyclerView.ViewHolder? {
        return parent.findChildViewUnder(0f, calculateHolderYPositionUnder(currentView))?.let {
            parent.getChildViewHolder(it)
        }
    }

    private fun calculateHolderYPositionUnder(view: View?) = view?.let { it.height + it.y } ?: 0f

    // change it here from class todo
    private fun drawNewStickyHolder(viewHolder: RecyclerView.ViewHolder, canvas: Canvas, state: RecyclerView.State) {
        val itemView = viewHolder.itemView
        yTranslation = 0f
        val bitmap = createBitmap(itemView, state)
        Canvas(bitmap).also { itemView.draw(it) }
        drawBitmap(canvas, bitmap)
        state.put(0, bitmap)
    }

    private fun drawFromState(state: RecyclerView.State, canvas: Canvas) {
        state.get<Bitmap>(0)?.also {
            canvas.drawBitmap(it, null, createRectF(it), null)
        }
    }

    private fun createBitmap(view: View, state: RecyclerView.State): Bitmap {
        return if (state.get<Bitmap>(0) == null) {
            Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        } else state.get(0)

    }

    private fun drawBitmap(canvas: Canvas, bitmap: Bitmap) {
        val rectF = createRectF(bitmap)
        canvas.drawBitmap(bitmap, null, rectF, null)
    }

    private fun createRectF(bitmap: Bitmap) = RectF(0f, 0f, bitmap.width.toFloat(), bitmap.height.toFloat())
}