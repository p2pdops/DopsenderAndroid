package p2pdops.dopsender.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.get
import kotlinx.android.synthetic.main.lay_pop_update_app.view.*
import p2pdops.dopsender.BuildConfig
import p2pdops.dopsender.R


fun Activity.notifyUpdate(remoteConfig: FirebaseRemoteConfig) {
    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
    val layoutInflater = layoutInflater

    val root: View = layoutInflater.inflate(R.layout.lay_pop_update_app, null)

    root.changeData.text =
        "${BuildConfig.VERSION_NAME} to ${remoteConfig["current_version_name"].asString()}, ${remoteConfig["current_version_size"].asString()}"
    root.newFeatures.text = remoteConfig["change_log"].asString()
    builder.setView(root)
    val dialog = builder.create()
    dialog.show()

    root.ignoreUpdate.setOnClickListener {
        dialog.dismiss()
    }
    root.storeUpdate.setOnClickListener {
        val uri: Uri = Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID)
                )
            )
        }
    }

}