package live.gold.rates.metal.prices.app.Activities

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Window
import androidx.cardview.widget.CardView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import live.gold.rates.metal.prices.app.BuildConfig
import live.gold.rates.metal.prices.app.R
import live.gold.rates.metal.prices.app.databinding.ActivityMainBinding
import com.qamar.curvedbottomnaviagtion.CurvedBottomNavigation

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    companion object {
        // you can put any unique id here, but because I am using Navigation Component I prefer to put it as
        // the fragment id.
        const val HOME_ITEM = R.id.homeFragment
        const val NEWS_ITEM = R.id.newsfragment
        const val PURITY_ITEM = R.id.purityfragment
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (supportActionBar != null)
            this.supportActionBar!!.hide();
        binding = ActivityMainBinding.inflate(layoutInflater)
        with(binding) {
            setContentView(root)
            initNavHost()
            setUpBottomNavigation()
        }
    }
    private fun ActivityMainBinding.setUpBottomNavigation() {
        val bottomNavigationItems = mutableListOf(
            CurvedBottomNavigation.Model(HOME_ITEM, getString(R.string.home), R.drawable.home_svg),
            CurvedBottomNavigation.Model(
                NEWS_ITEM, getString(R.string.news),
                R.drawable.news_icon_svg
            ),
            CurvedBottomNavigation.Model(
                PURITY_ITEM, getString(R.string.purity),
                R.drawable.purity_svg
            )
        )
        bottomNavigation.apply {
            bottomNavigationItems.forEach { add(it) }
            setOnClickMenuListener {
                navController.navigate(it.id)
            }
            // optional
            setupNavController(navController)
        }
    }


    private fun initNavHost() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }


    // if you need your backstack of your items always back to home please override this method
    override fun onBackPressed() {

        val dialog = Dialog(this, R.style.AppTheme_Dialog)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setContentView(R.layout.dialog_backpress)
        val rateus = dialog.findViewById<CardView>(R.id.ratenow)
        val later = dialog.findViewById<CardView>(R.id.exitbtn)
        dialog.show()

        later.setOnClickListener {
            dialog.dismiss()
            finishAffinity()
        }
        rateus.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)
                    )
                )
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }
    }

}