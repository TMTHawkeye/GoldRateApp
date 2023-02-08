package live.gold.rates.metal.prices.app

import android.app.FragmentTransaction
import android.content.Context
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener
import com.loopj.android.http.AsyncHttpClient
import com.loopj.android.http.JsonHttpResponseHandler
import live.gold.rates.metal.prices.app.Adaptors.NewsAdaptor
import live.gold.rates.metal.prices.app.Model.ArticleModel
import live.gold.rates.metal.prices.app.databinding.ActivityMainBinding
import live.gold.rates.metal.prices.app.databinding.FragmentNewsBinding
import cz.msebera.android.httpclient.Header
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import android.os.Build
import androidx.navigation.fragment.NavHostFragment


class NewsFragment : Fragment() {
    var beginDate: String? = null
    var endDate: String? = null
    lateinit var navController: NavController

    lateinit var binding: FragmentNewsBinding



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNewsBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val connManager =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        val mWifi = connManager!!.getNetworkInfo(ConnectivityManager.TYPE_WIFI)

        if (mWifi!!.isConnected) {
            getSharedPreferencesSettings()
            fetchArticles()
        } else {
            binding.progress.visibility = View.GONE
            binding.imageNews.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "No internet connection", Toast.LENGTH_SHORT)
                .show()

        }

        binding.pullToRefresh.setOnRefreshListener(OnRefreshListener {
            binding.pullToRefresh.setRefreshing(false)
        })
    }



    fun getSharedPreferencesSettings() {
        Log.d("TAG", "loadContent: " + "Hello News")

        val c = Calendar.getInstance().time
        val df = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val formattedDate = df.format(c)
        endDate = formattedDate

        val calendar = Calendar.getInstance()
        calendar.time = c
        calendar.add(Calendar.DAY_OF_YEAR, -60)
        val newDate = calendar.time
        val df1 = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
        val formattedDate1 = df1.format(newDate)
        beginDate = formattedDate1
        Log.d("TAG", "getDataWithDateRange: " + beginDate)

    }

//    private fun initNavHost() {
//        val navHostFragment =
//            requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//        navController = navHostFragment.navController
//    }


    private fun fetchArticles() {
        val client = AsyncHttpClient()
        val url =
            "https://api.nytimes.com/svc/search/v2/articlesearch.json?q=gold&begin_date=" + beginDate + "&end_date=" + endDate + "&sort=newest&api-key=r8MH1tGk3cFTmmZb2A8U0INJANCtPA3S"

        client.get(url, object : JsonHttpResponseHandler() {
            override fun onSuccess(
                statusCode: Int,
                headers: Array<out Header>?,
                response: JSONObject?
            ) {
                var articleJsonResults: JSONArray? = null
                try {
                    articleJsonResults =
                        response!!.getJSONObject("response").getJSONArray("docs")

                    requireActivity().runOnUiThread {
                        binding.progress.visibility = View.GONE
                        getJSONObject(articleJsonResults)
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    requireActivity().runOnUiThread {
                        binding.progress.visibility = View.GONE
                        binding.imageNews.visibility = View.VISIBLE
                        binding.imageNews.setImageResource(R.drawable.no_data_svg)
                    }
                }
            }

            override fun onFailure(
                statusCode: Int,
                headers: Array<out Header>?,
                responseString: String?,
                throwable: Throwable?
            ) {
                super.onFailure(statusCode, headers, responseString, throwable)
                requireActivity().runOnUiThread {
                    binding.progress.visibility = View.GONE
                    binding.imageNews.visibility = View.VISIBLE
                    binding.imageNews.setImageResource(R.drawable.no_data_svg)
                }
            }
        })

    }

    fun getJSONObject(responseString: JSONArray) {
        try {
            var articleModel = kotlin.collections.ArrayList<ArticleModel>()
            for (i in 0 until responseString.length()) {
                var e: JSONObject? = null
                try {
                    e = responseString.getJSONObject(i)
                    val headline = e.getString("headline")  //headline object
                    val website = e.getString("web_url")   //web_url
                    val pub_date = e.getString("pub_date")   //published date
                    var h_line = JSONObject(headline)
                    val final_headline = h_line.getString("main")   //get main headline
                    val source = e.getString("source")
                    //model arraylist to show recyclerview items
                    articleModel.add(
                        ArticleModel(
                            final_headline,
                            website,
                            convertDate(pub_date)!!,
                            source
                        )
                    )
                } catch (e1: JSONException) {
                    e1.printStackTrace()
                }
            }
            setAdaptor(articleModel)


        } catch (e: JSONException) {
            // TODO Auto-generated catch block
//            e.printStackTrace()
        }


    }

    private fun setAdaptor(abstractList: ArrayList<ArticleModel>) {
        binding.recyclerViewNews.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewNews.adapter = NewsAdaptor(requireContext(), abstractList)
    }

    fun convertDate(dateString: String?): String? {
        var dateFormat: SimpleDateFormat? =
            SimpleDateFormat("yyyy-MM-dd")
        val date = dateFormat!!.parse(dateString)
        val datestring: String = dateFormat.format(date)
        return datestring
    }


}


