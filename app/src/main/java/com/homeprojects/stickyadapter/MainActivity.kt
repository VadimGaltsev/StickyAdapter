package com.homeprojects.stickyadapter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.RectF
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.graphics.BitmapCompat
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.homeprojects.stickydecorator.RecyclerViewDecorator
import org.w3c.dom.Attr
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    val recyclerView by lazy { findViewById<RecyclerView>(R.id.recycler_view) }

    enum class Types {
        HeaderImage,
        HeaderText,
        SomeData
    }

    val listItems = listOf(
        Text(),
        TextHeader(),
        Text(),
        HeaderImage(),
        Text(),
        TextHeader(),
        Text(),
        Text(),
        Text(),
        Text(),
        TextHeader(),
        Text(),
        Text(),
        TextHeader(),
        Text(),
        Text(),
        Text(),
        HeaderImage(),
        Text(),
        Text(),
        TextHeader(),
        Text(),
        Text(),
        Text(),
        Text(),
        TextHeader(),
        Text(),
        HeaderImage(),
        Text(),
        HeaderImage(),
        Text(),
        Text(),
        Text(),
        TextHeader(),
        Text(),
        Text(),
        TextHeader(),
        HeaderImage(),
        Text(),
        Text(),
        Text(),
        Text(),
        Text(),
        Text(),
        TextHeader()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val currentDate = "11.12.1990"
        val dateFormat = SimpleDateFormat("dd.mm.yyyy", Locale.getDefault())
        val datea = dateFormat.parse(currentDate).let { dateFormat.format(it) }
        title = datea
        recyclerView.adapter = adapter(listItems)
        recyclerView.addItemDecoration(RecyclerViewDecorator(listOf(Types.HeaderText)))
    }

}

interface typedModel {

    fun getType(): Int
}

data class Text(val type: MainActivity.Types = MainActivity.Types.SomeData) : typedModel {
    override fun getType() = type.ordinal
}

data class HeaderImage(val type: MainActivity.Types = MainActivity.Types.HeaderImage) : typedModel {

    override fun getType() = type.ordinal
}

data class TextHeader(val type: MainActivity.Types = MainActivity.Types.HeaderText) : typedModel {

    override fun getType() = type.ordinal
}

class adapter(val list: List<typedModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun onCreateViewHolder(p0: ViewGroup, p1: Int): RecyclerView.ViewHolder {
        val type = MainActivity.Types.values()[p1]
        val view = when (type) {
            MainActivity.Types.HeaderText -> LayoutInflater.from(p0.context).inflate(R.layout.headertext, p0, false)
            MainActivity.Types.HeaderImage -> LayoutInflater.from(p0.context).inflate(R.layout.header, p0, false)
            MainActivity.Types.SomeData -> LayoutInflater.from(p0.context).inflate(R.layout.notheadertext, p0, false)
        }
        return object : RecyclerView.ViewHolder(view) {}
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(p0: RecyclerView.ViewHolder, p1: Int) {
        p0.itemView.findViewWithTag<TextView>("text").also {
            it.text = "text + $p1"
        }
    }

    override fun getItemViewType(position: Int) = list[position].getType()

}

