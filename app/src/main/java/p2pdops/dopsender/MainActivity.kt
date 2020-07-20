package p2pdops.dopsender

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import p2pdops.dopsender.utils.getHomeHelperShown
import p2pdops.dopsender.utils.notifyUpdate
import p2pdops.dopsender.utils.setHomeHelperShown


class MainActivity : AppCompatActivity() {

    private var opened: Boolean = false
    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var remoteConfig: FirebaseRemoteConfig

    companion object {
        private const val TAG = "MainActivity"
        const val PERMISSION_LOCATION_AND_LOCATION = 304
    }

    private var mPublisherInterstitialAd: PublisherInterstitialAd? = null

    private var interstitialShown = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Handler(Looper.getMainLooper()).postDelayed({
            mPublisherInterstitialAd = PublisherInterstitialAd(this@MainActivity)
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
                    Toast.makeText(this@MainActivity, "Thank you ! \uD83D\uDE0D", Toast.LENGTH_LONG)
                        .show()
                }

                override fun onAdLeftApplication() {
                    Toast.makeText(this@MainActivity, "Thank you ! \uD83D\uDE0D", Toast.LENGTH_LONG)
                        .show()
                }

                override fun onAdClosed() {
                    Toast.makeText(this@MainActivity, "Thank you ! \uD83D\uDE0D", Toast.LENGTH_LONG)
                        .show()
                }
            }

        }, 500)

        CoroutineScope(Dispatchers.IO).launch {
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 3600
            }

            remoteConfig = Firebase.remoteConfig

            remoteConfig.setConfigSettingsAsync(configSettings)
            remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
            remoteConfig.fetchAndActivate().addOnCompleteListener {
                if (remoteConfig["current_version_code"].asLong()
                        .toInt() > BuildConfig.VERSION_CODE
                ) {
                    notifyUpdate(remoteConfig)
                }
            }
        }

        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.nav_home, R.id.nav_about), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        if (!getHomeHelperShown())
        AlertDialog.Builder(this)
            .setTitle("Guide")
            .setMessage(getString(R.string.start_guide))
            .setNeutralButton("Ok") { dialogInterface, i -> dialogInterface.dismiss() }
            .setPositiveButton("Don't show again") { dialogInterface, i -> setHomeHelperShown(); dialogInterface.dismiss() }
            .show()
    }

    override fun onSupportNavigateUp(): Boolean {
        Log.d(TAG, "onSupportNavigateUp: ")
        val navController = findNavController(R.id.nav_host_fragment)

        opened = !opened
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        menu?.findItem(R.id.action_donate)?.isVisible = !interstitialShown
        return true
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
            R.id.action_help -> {
                AlertDialog.Builder(this)
                    .setTitle("Guide")
                    .setMessage(getString(R.string.start_guide))
                    .setPositiveButton("Ok") { dialogInterface, i -> dialogInterface.dismiss() }
                    .show()
                true
            }
            else -> false
        }
    }
}