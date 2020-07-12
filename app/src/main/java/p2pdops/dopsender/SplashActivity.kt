package p2pdops.dopsender

import android.content.Context
import android.content.Intent
import android.net.wifi.p2p.WifiP2pManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorCompat
import p2pdops.dopsender.utils.getDeviceUnSupported


class SplashActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "SplashActivity"
        const val STARTUP_DELAY = 300
        const val ANIM_ITEM_DURATION = 1000
        const val ITEM_DELAY = 300
    }

    private val animationStarted = false
    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.AppTheme_NoActionBar)
        window.decorView.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        if (!hasFocus || animationStarted) {
            return
        }
        animate()
        super.onWindowFocusChanged(hasFocus)
    }

    private fun animate() {
        val logoImageView: ImageView = findViewById<View>(R.id.img_logo) as ImageView
        val container: ViewGroup = findViewById<View>(R.id.container) as ViewGroup
        ViewCompat.animate(logoImageView)
            .translationY(-250f)
            .setStartDelay(STARTUP_DELAY.toLong())
            .setDuration(ANIM_ITEM_DURATION.toLong())
            .setInterpolator(
                DecelerateInterpolator(1.2f)
            ).start()
        for (i in 0 until container.childCount) {
            val v: View = container.getChildAt(i)
            var viewAnimator: ViewPropertyAnimatorCompat
            viewAnimator = if (v !is Button) {
                ViewCompat.animate(v)
                    .translationY(50f).alpha(1f)
                    .setStartDelay(ITEM_DELAY * i + 500.toLong())
                    .setDuration(1000)
            } else {
                ViewCompat.animate(v)
                    .scaleY(1f).scaleX(1f)
                    .setStartDelay(ITEM_DELAY * i + 500.toLong())
                    .setDuration(500)
            }
            viewAnimator.setInterpolator(DecelerateInterpolator()).start()
        }


        Handler(Looper.getMainLooper()).postDelayed({
//            startActivity(Intent(this, NameChooseActivity::class.java))
//            finish()

            if (!isWifiDirectSupported() || getDeviceUnSupported()) {
                startActivity(Intent(this, NoSupportedActivity::class.java))
                finish()
                return@postDelayed
            }

            if (restorePrefData()) {
                val mainActivity = Intent(applicationContext, MainActivity::class.java)
                startActivity(mainActivity)
                finish()
            } else {
                startActivity(Intent(this, IntroActivity::class.java))
                finish()
            }
        }, 1000)
    }

    private fun isWifiDirectSupported(): Boolean {
        val pm = packageManager
        val features = pm.systemAvailableFeatures
        for (info in features) {
            if (info?.name != null && info.name.equals(
                    "android.hardware.wifi.direct",
                    ignoreCase = true
                )
            ) {
                Log.d(TAG, "isWifiDirectSupported: Have Wifi p2p mode")
                return true
            }
        }
        return false
    }

    private fun restorePrefData(): Boolean {
        val pref = applicationContext.getSharedPreferences(
            "dopsender",
            Context.MODE_PRIVATE
        )
        return pref.getBoolean(getString(R.string.is_name_set), false)
    }

}