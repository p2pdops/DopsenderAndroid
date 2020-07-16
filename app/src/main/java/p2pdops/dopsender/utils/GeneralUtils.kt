package p2pdops.dopsender.utils

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import p2pdops.dopsender.R
import p2pdops.dopsender.modals.FileType
import java.text.CharacterIterator
import java.text.StringCharacterIterator
import java.util.*

fun Context.loadAd(nativeAd: TemplateView) {
    MobileAds.initialize(this)
    val adLoader = AdLoader.Builder(this, getString(R.string.home_ad_id))
        .forUnifiedNativeAd { unifiedNativeAd ->

            val styles = NativeTemplateStyle.Builder()
                .withMainBackgroundColor(
                    ColorDrawable(Color.parseColor("#ffffff"))
                )
                .build()

            nativeAd.setStyles(styles)
            nativeAd.setNativeAd(unifiedNativeAd)
            nativeAd.slideUp()
            Log.d("LoadAd", "${this@loadAd}: Ad shown")
        }
        .build()

    Handler(Looper.getMainLooper()).post {
        adLoader.loadAd(AdRequest.Builder().build())
    }

}

fun Activity.activeNetwork(): Boolean {
    val cm =
        getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
    val activeNetwork: NetworkInfo? = cm!!.activeNetworkInfo
    return activeNetwork != null &&
            activeNetwork.isConnected
}

fun dpToPx(dp: Int): Int {
    return (dp * Resources.getSystem().displayMetrics.density).toInt()
}

fun pxToDp(px: Int): Int {
    return (px / Resources.getSystem().displayMetrics.density).toInt()
}


fun humanizeBytes(_bytes: Long, limit: Int = 2): String? {
    var bytes = _bytes
    if (-1000 < bytes && bytes < 1000) {
        return "$bytes B"
    }
    val ci: CharacterIterator = StringCharacterIterator("KMGTPE")
    while (bytes <= -999950 || bytes >= 999950) {
        bytes /= 1000
        ci.next()
    }
    return String.format(
        Locale.getDefault(),
        "%.${limit}f %cB",
        bytes / 1000.0,
        ci.current()
    )
}

fun Activity.whiteStatusBarBlackText() {
    window.statusBarColor = resources.getColor(R.color.pureWhite)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }
}

fun humanizeTime(_millis: Long): String? {
    var seconds = _millis / 1000
    var out = ""
    val hr = seconds / (60 * 60)
    seconds %= (60 * 60)
    val min = seconds / 60
    seconds %= 60
    var isHours = false
    var isMins = false
    if (hr > 0) {
        out += "$hr Hr" + if (hr == 1L) " " else "s "
    }
    if (min > 0) {
        out += "$min M "
    }
    if (seconds > 0) {
        out += "$seconds s"
    }
    return out
}


val docsColormap = mapOf(
    "pdf" to R.color.color_pdf,
    "csv" to R.color.color_csv,
    "doc" to R.color.color_doc,
    "docx" to R.color.color_docx,
    "xls" to R.color.color_xls,
    "xlsx" to R.color.color_xlsx
)

val fileTypes = FileType.values()
