package com.example.newsapplication.adapters
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.newspro.R
import models.News
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class NewsListAdapter(private val listener : NewsItemClicked): RecyclerView.Adapter<NewsViewHolder>() {

    private val items : ArrayList<News> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_news,parent,false)
        val viewHolder = NewsViewHolder(view)

        //adding listener for handling clicks of items
        view.setOnClickListener{
            listener.onItemClicked(items[viewHolder.adapterPosition])
        }
        return viewHolder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: NewsViewHolder, position: Int) {
        val currentItem = items[position]
        holder.titleView.text = currentItem.title
        val inputString = currentItem.publishedAt
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
        val dateTime = LocalDateTime.parse(inputString, formatter)
        holder.posted.text = dateTime.dayOfYear.toString() + " hours ago"
        holder.author.text = currentItem.author
        Glide.with(holder.itemView.context).load(currentItem.urlToImage).into(holder.image)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun updateNews(updatedNews : ArrayList<News>)
    {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            if (updatedNews != null) {
                items.clear()
                items.addAll(updatedNews.sortedBy{ LocalDateTime.parse(it.publishedAt, formatter) })
                Handler(Looper.getMainLooper()).post {
                    // UI operations
                    notifyDataSetChanged()
                }
            }
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun sort(boolean: Boolean)
    {
        try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
            if (items != null) {
                val sortedNewsArray : List<News>
                if (boolean){
                    sortedNewsArray = items.sortedByDescending {  LocalDateTime.parse(it.publishedAt, formatter) }
                }
                else{
                    sortedNewsArray = items.sortedBy {  LocalDateTime.parse(it.publishedAt, formatter) }
                }
                items.clear()
                items.addAll(sortedNewsArray)
                Handler(Looper.getMainLooper()).post {
                    // UI operations
                    notifyDataSetChanged()
                }
            }
        }
        catch (e: Exception){
            e.printStackTrace()
        }
    }

}

class NewsViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){

    val titleView : TextView = itemView.findViewById(R.id.item_title)
    val image : ImageView = itemView.findViewById(R.id.image)
    val author : TextView = itemView.findViewById(R.id.author)
    val posted : TextView = itemView.findViewById(R.id.timePosted)
}

interface NewsItemClicked
{
    fun onItemClicked(item : News)
}