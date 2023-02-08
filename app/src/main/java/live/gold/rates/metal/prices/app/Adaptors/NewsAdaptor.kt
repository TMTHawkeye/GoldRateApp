package live.gold.rates.metal.prices.app.Adaptors

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import live.gold.rates.metal.prices.app.Model.ArticleModel
import live.gold.rates.metal.prices.app.databinding.RowForRecyclerviewNewsBinding
import org.jsoup.Jsoup


class NewsAdaptor(var context: Context, var abstractList: ArrayList<ArticleModel>) :
    RecyclerView.Adapter<NewsAdaptor.viewHolder>() {

    lateinit var binding: RowForRecyclerviewNewsBinding

    class viewHolder(var binding: RowForRecyclerviewNewsBinding) :
        RecyclerView.ViewHolder(binding.root) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): viewHolder {
        binding = RowForRecyclerviewNewsBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return viewHolder(binding)
    }

    override fun onBindViewHolder(holder: viewHolder, position: Int) {
        binding.titleNews.text=abstractList.get(position).headline
        binding.dateNews.text=abstractList.get(position).pub_date
        binding.arthorNameId.text=abstractList.get(position).source

        binding.linearArticle.setOnClickListener(View.OnClickListener {
            openURL(abstractList.get(position).weburl)
        })
    }

    private fun setImage(pos:Int) {
        val document = Jsoup.connect(abstractList.get(pos).weburl)
            .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
            .referrer("http://www.google.com")
            .get()

//        if (document != null) {
//            val element = document.select("meta")
//            for (ele in element) {
//                if (ele.siblingElements() != null) {
//                    val sibElemets = ele.siblingElements()
//                    for (sibElemet in sibElemets) {
////                        Log.d("TAG", "setImage: "+sibElemet.text().toString())
//                        break
//                    }
//                }
//            }
//        } else {
//            println("************" + "There is no playstore version available for this app")
//            Toast.makeText(
//                context,
//                "There is no image for url",
//                Toast.LENGTH_SHORT
//            ).show()
//        }
        Log.d("TAG", "setImage: "+document!!.title())
    //        Glide.with(context).load(imageUrl).into(binding.imageArticle);

    }

    override fun getItemCount(): Int {
        return abstractList.size
    }

    fun openURL(weburl: String)
    {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(weburl))
        context.startActivity(browserIntent)
    }
}