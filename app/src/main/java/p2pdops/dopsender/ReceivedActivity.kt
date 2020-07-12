package p2pdops.dopsender

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.activity_received.*
import kotlinx.android.synthetic.main.fragment_home.view.*
import p2pdops.dopsender.ui.received_files.SectionsPagerAdapter
import p2pdops.dopsender.utils.slideUp


class ReceivedActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ReceivedActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_received)
        supportActionBar?.title = "Your received files..."
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager)
        view_pager.adapter = sectionsPagerAdapter
        tabs.setupWithViewPager(view_pager)

        MobileAds.initialize(this)
        val adLoader = AdLoader.Builder(this, getString(R.string.sender_ad_id))
            .forUnifiedNativeAd { unifiedNativeAd ->
                val styles = NativeTemplateStyle.Builder()
                    .withMainBackgroundColor(
                        ColorDrawable(Color.parseColor("#ffffff"))
                    )
                    .build()

                native_ad.setStyles(styles)
                native_ad.setNativeAd(unifiedNativeAd)
                native_ad.slideUp()
            }
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

    }

}