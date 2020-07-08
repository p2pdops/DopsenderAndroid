package p2pdops.dopsender.ui.main_home.home

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.fragment_home.view.*
import p2pdops.dopsender.MainActivity
import p2pdops.dopsender.R
import p2pdops.dopsender.SenderActivity
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class HomeFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var sendButton: Button? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_home, container, false)

        sendButton = root.findViewById(R.id.sendButton)


        MobileAds.initialize(requireActivity())
        val adLoader = AdLoader.Builder(requireActivity(), getString(R.string.nativeAdId))
            .forUnifiedNativeAd { unifiedNativeAd ->
                val styles = NativeTemplateStyle.Builder()
                    .withMainBackgroundColor(
                        ColorDrawable(Color.parseColor("#ffffff"))
                    )
                    .build()

                root.native_ad.setStyles(styles)
                root.native_ad.setNativeAd(unifiedNativeAd)
            }
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

        methodRequiresTwoPermission()
        return root
    }

    @AfterPermissionGranted(MainActivity.PERMISSION_LOCATION_AND_LOCATION)
    private fun methodRequiresTwoPermission() {
        val perms = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (EasyPermissions.hasPermissions(this.requireContext(), *perms)) {
            sendButton!!.setOnClickListener {
                startActivity(Intent(requireContext(), SenderActivity::class.java))
            }
        } else {

            EasyPermissions.requestPermissions(
                this,
                "Need location permission to scan devices around you and storage permission to read and share your files. ",
                MainActivity.PERMISSION_LOCATION_AND_LOCATION,
                *perms
            )
        }
    }


    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        methodRequiresTwoPermission()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            // Do something after user returned from app settings screen, like showing a Toast.
            Toast.makeText(context, "From settings app", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

    }
}