package com.example.newspro
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapplication.adapters.NewsItemClicked
import com.example.newsapplication.adapters.NewsListAdapter
import com.google.gson.Gson
import models.News
import models.NewsResponse
import utils.Constants
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity(), NewsItemClicked {

    private lateinit var mAdapter: NewsListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var recyclerview: RecyclerView = findViewById(R.id.my_recyclerview)
        recyclerview.layoutManager = LinearLayoutManager(this)
        // Making instance of adapter
        fetchData()
        mAdapter = NewsListAdapter(this)
        // linking adapter with recycler view
        recyclerview.adapter = mAdapter
    }

    private fun fetchData() {
        Thread {
            // Create a URL object from the provided URL string
            val url = URL(Constants.BASE_URL)
            // Open a connection to the URL
            val urlConnection = url.openConnection() as HttpURLConnection
            // Set up the connection properties
            urlConnection.requestMethod = "GET"
            urlConnection.readTimeout = Constants.NEWS_TIME_DELAY // milliseconds
            urlConnection.connectTimeout = Constants.CONNECTION_TIMEOUT // milliseconds
            urlConnection.setRequestProperty("Content-Type", "application/json; utf-8")
            // Connect to the URL
            try {
                urlConnection.connect()
                // Check if the request was successful (HTTP 200 OK)
                if (urlConnection.responseCode == HttpURLConnection.HTTP_OK) {
                    // Get the InputStream from the connection
                    val inputStream = urlConnection.inputStream
                    // Read the InputStream and convert it to a String
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val stringBuilder = StringBuilder()
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        stringBuilder.append(line)
                    }
                    // Close the InputStream and disconnect the HttpURLConnection
                    inputStream.close()
                    // Process the response data
                    val responseData = stringBuilder.toString()
                    println(responseData)
                    val gson = Gson();
                    val newsResponsejson = gson
                        .fromJson(responseData, NewsResponse::class.java);
                    val newsArray = ArrayList<News>()

                    newsResponsejson.articles.forEach {
                        val news = News(
                            it.title ?: "",
                            it.author ?: "",
                            it.url ?: "",
                            it.urlToImage ?: ""
                        )
                        newsArray.add(news)
                    }
                    mAdapter.updateNews(newsArray)

                } else {
                    // Handle the error (e.g., non-200 response code)
                    println("Error: ${urlConnection.responseCode}")
                }
            }
            finally {
                urlConnection.disconnect()
            }
        }.start()
    }

    override fun onItemClicked(item: News) {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(item.url))
    }
}