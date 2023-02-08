package live.gold.rates.metal.prices.app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import com.infideap.drawerbehavior.BuildConfig
import live.gold.rates.metal.prices.app.ViewPager.ViewPagerAdaptor
import live.gold.rates.metal.prices.app.ViewPager.ViewPagerFragment
import live.gold.rates.metal.prices.app.databinding.FragmentHomeBinding
import java.util.concurrent.Executors


class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    var mExecutorService = Executors.newSingleThreadExecutor()
    val handler = Handler(Looper.getMainLooper())
    lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)

//        open 3D drwaer
        openNavDrawer()
        //set content for 3D drawer
        setNavContent()

       loadContent()
        return binding.root
    }

    private fun openNavDrawer() {
        binding.menuIcon.setOnClickListener(View.OnClickListener {
            binding.drawer.openDrawer(Gravity.LEFT)
            binding.drawer.setViewRotation(GravityCompat.START, 15f)
        })
    }

    private fun setNavContent() {
        binding.navView.setNavigationItemSelectedListener(
            { item ->
                when (item.itemId) {
                    R.id.rate_us -> try {
                        val marketUri = Uri.parse("market://details?id=packageName"+requireContext().packageName)
                        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                        startActivity(marketIntent)
                    } catch (e: ActivityNotFoundException) {
                        val marketUri =
                            Uri.parse("https://play.google.com/store/apps/dev?id="+requireContext().packageName)
                        val marketIntent = Intent(Intent.ACTION_VIEW, marketUri)
                        startActivity(marketIntent)
                    }
                    R.id.share -> try {
                        val shareIntent = Intent(Intent.ACTION_SEND)
                        shareIntent.type = "text/plain"
                        shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Wifi Key Master")
                        var shareMessage = "\nLet me recommend you this application\n\n"
                        shareMessage =
                            """
                    ${shareMessage}https://play.google.com/store/apps/dev?id=${BuildConfig.APPLICATION_ID}
                  
                    """.trimIndent()
                        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                        startActivity(Intent.createChooser(shareIntent, "choose one"))
                    } catch (e: Exception) {
                    }
                    R.id.feedback -> {
                        val feedbackIntent = Intent(Intent.ACTION_SEND)
                        val recipients = arrayOf("smartianztech@gmail.com")
                        feedbackIntent.putExtra(Intent.EXTRA_EMAIL, recipients)
                        feedbackIntent.putExtra(
                            Intent.EXTRA_SUBJECT,
                            "Feedback : " + R.string.app_name
                        )
                        feedbackIntent.type = "text/html"
                        feedbackIntent.setPackage("com.google.android.gm")
                        startActivity(Intent.createChooser(feedbackIntent, "Send mail"))
                    }
                    R.id.privacy -> {
                        val uri =
                            Uri.parse("https://smartianztech.blogspot.com/2023/01/welcome-to-smartianz-tech-inc.html")
                        val intent = Intent(Intent.ACTION_VIEW, uri)
                        startActivity(intent)
                    }
                    R.id.moreApps -> {
                        val moreAppUri =
                            Uri.parse("https://play.google.com/store/apps/developer?id=Smartianz+Tech")
                        val moreApp = Intent(Intent.ACTION_VIEW, moreAppUri)
                        startActivity(moreApp)
                    }
                    else -> {}
                }
                binding.drawer.closeDrawer(GravityCompat.START)
                true
            })
    }



    fun loadContent()
    {
        Log.d("TAG", "loadContent: "+"Hello Home")

        var adapter =
            ViewPagerAdaptor(
                childFragmentManager
            )
        mExecutorService.execute {
            adapter.addFragment(
                ViewPagerFragment("gold"
                ), "Gold"
            )

            adapter.addFragment(
                ViewPagerFragment("silver"
                ), "Silver"
            )

            adapter.addFragment(
                ViewPagerFragment("platinum"
                ), "Platinum"
            )

            handler.post {
                //set adapter
                binding.viewpager.adapter = adapter
                var tab = binding.tabs
                //set 2 tabs (dat and winmail)
                tab.setupWithViewPager(binding.viewpager)

            }
        }
    }
}

