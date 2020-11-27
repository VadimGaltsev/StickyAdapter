package com.homeprojects.stickyadapter

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.homeprojects.stickydecorator.RecyclerViewDecorator
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    val recyclerView by lazy { findViewById<RecyclerView>(R.id.recycler_view) }
    val stickyHeaderDecorator = RecyclerViewDecorator(listOf(Types.HeaderImage, Types.SomeData))

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
        recyclerView.addItemDecoration(stickyHeaderDecorator)
    }

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        stickyHeaderDecorator.clearCacheHolder()
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

