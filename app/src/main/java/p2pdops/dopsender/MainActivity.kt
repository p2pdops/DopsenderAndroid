package p2pdops.dopsender

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity() {

    private var opened: Boolean = false
    private lateinit var appBarConfiguration: AppBarConfiguration


    companion object {
        private const val TAG = "MainActivity"
        const val PERMISSION_LOCATION_AND_LOCATION = 304
    }

    private var mPublisherInterstitialAd: PublisherInterstitialAd? = null

    private var interstitialShown = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mPublisherInterstitialAd = PublisherInterstitialAd(this)
        mPublisherInterstitialAd?.adUnitId = getString(R.string.reward_ad_id)
        mPublisherInterstitialAd?.loadAd(PublisherAdRequest.Builder().build())

        mPublisherInterstitialAd!!.adListener = object : AdListener() {
            override fun onAdLoaded() {
                interstitialShown = false
                invalidateOptionsMenu()
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                Log.d(TAG, "onAdFailedToLoad: ")
            }

            override fun onAdOpened() {

            }

            override fun onAdClicked() {
            }

            override fun onAdLeftApplication() {
            }

            override fun onAdClosed() {
            }
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_about
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

    }

    override fun onSupportNavigateUp(): Boolean {
        Log.d(TAG, "onSupportNavigateUp: ")
        val navController = findNavController(R.id.nav_host_fragment)

        opened = !opened
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return !interstitialShown
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_donate -> {
                if (!interstitialShown)
                    if (mPublisherInterstitialAd!!.isLoaded) {
                        mPublisherInterstitialAd?.show()
                        interstitialShown = true
                        invalidateOptionsMenu()
                    }
                true
            }
            else -> false
        }
    }
}