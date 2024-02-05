package com.example.newspro
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
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
    private var isValueInverted = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var recyclerview: RecyclerView = findViewById(R.id.my_recyclerview)
        var progessbar: ProgressBar = findViewById(R.id.pbLoading)
        recyclerview.layoutManager = LinearLayoutManager(this)
        mAdapter = NewsListAdapter(this)
        // linking adapter with recycler view
        recyclerview.adapter = mAdapter
        var sorticon : ImageView = findViewById(R.id.cornerIcon)
        if (isInternetAvailable(applicationContext)) {
            // Internet is available, proceed with your task
            // Making instance of adapter
            fetchData()
            sorticon.setOnClickListener{
                if (!isValueInverted)
                     sorticon.setImageResource(R.drawable.baseline_clear_all_24)
                else
                    sorticon.setImageResource(R.drawable.baseline_sort_24)
                isValueInverted = !isValueInverted
                mAdapter.sort(isValueInverted)
            }

        } else {
            Toast.makeText(applicationContext, "No Internet", Toast.LENGTH_LONG).show()
            progessbar.visibility = View.INVISIBLE
        }
    }
    private fun fetchData() {
        var progessbar: ProgressBar = findViewById(R.id.pbLoading)
        Thread {
            // Create a URL object from the provided URL string
            val url = URL(Constants.BASE_URL)
            // Open a connection to the URL
            val urlConnection = url.openConnection() as HttpURLConnection
            // Set up the connection properties
            urlConnection.requestMethod = "GET"
            urlConnection.readTimeout = Constants.NEWS_TIME_DELAY // milliseconds
            urlConnection.connectTimeout = Constants.CONNECTION_TIMEOUT // milliseconds
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
                    Toast.makeText(applicationContext, "No Internet", Toast.LENGTH_LONG).show()
                    println("Error: ${urlConnection.responseCode}")
                }
            }
            finally {
                progessbar.visibility = View.INVISIBLE
                urlConnection.disconnect()
            }
        }.start()
    }
    fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork
            val capabilities = connectivityManager.getNetworkCapabilities(network)
            return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } else {
            // For devices below Android M
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            return activeNetworkInfo != null && activeNetworkInfo.isConnected
        }
    }
    override fun onItemClicked(item: News) {
        val builder = CustomTabsIntent.Builder()
        val customTabsIntent = builder.build()
        customTabsIntent.launchUrl(this, Uri.parse(item.url))
    }
}