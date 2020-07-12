package p2pdops.dopsender.ui.main_home.home

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.getkeepsafe.taptargetview.TapTarget
import com.getkeepsafe.taptargetview.TapTargetView
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.fragment_home.view.*
import kotlinx.android.synthetic.main.item_home_item.view.*
import p2pdops.dopsender.*
import p2pdops.dopsender.utils.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


class HomeFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    companion object {
        private const val TAG = "HomeFragment"
    }

    private var sendButton: Button? = null

    class HomeItemHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class HomeItem(val title: String, val subtitle: String, val icon: Int)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_home, container, false)

        sendButton = root.findViewById(R.id.sendButton)

        MobileAds.initialize(requireActivity())
        val adLoader = AdLoader.Builder(requireActivity(), getString(R.string.home_ad_id))
            .forUnifiedNativeAd { unifiedNativeAd ->

                val styles = NativeTemplateStyle.Builder()
                    .withMainBackgroundColor(
                        ColorDrawable(Color.parseColor("#ffffff"))
                    )
                    .build()

                root.native_ad.setStyles(styles)
                root.native_ad.setNativeAd(unifiedNativeAd)
                root.native_ad.slideUp()
                Log.d(TAG, "onCreateView: ad shown")
            }
            .build()

        root.change_btn_tv.setOnClickListener {
            startActivity(Intent(context, NameChooseActivity::class.java))
        }

        Handler(Looper.getMainLooper()).post {
            adLoader.loadAd(AdRequest.Builder().build())
        }
        root.app_user_name.text = context?.getLocalName()

        requireContext().getLocalDpRes().let { root.profile_image.setImageResource(it) }
        root.mobile_info.text = Build.MODEL
        root.mobile_info_device.text =
            Build.PRODUCT[0].toUpperCase() + Build.PRODUCT.substring(1, Build.PRODUCT.length)

        val freeSpace = FileUtils.getAvailableInternalMemorySize()

        root.free_space.text = humanizeBytes(freeSpace)

        root.home_items.setHasFixedSize(true)
        root.home_items.layoutManager = LinearLayoutManager(context)

        val data = arrayOf(
            HomeItem("Received Files", "Explore file received", R.drawable.ic_download),
            HomeItem(
                "Invite Friends",
                "Share this awesome app with your friends.",
                R.drawable.ic_add_user
            )
        )

        root.home_items.adapter = object : RecyclerView.Adapter<HomeItemHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeItemHolder {
                return HomeItemHolder(
                    LayoutInflater.from(context).inflate(R.layout.item_home_item, parent, false)
                )
            }

            override fun getItemCount(): Int = data.size

            override fun onBindViewHolder(holder: HomeItemHolder, position: Int) {
                Glide.with(holder.itemView).load(data[position].icon)
                    .into(holder.itemView.icon_view)
                holder.itemView.title.text = data[position].title
                holder.itemView.sub_title.text = data[position].subtitle

                holder.itemView.setOnClickListener {
                    when (position) {
                        0 -> startActivity(Intent(context, ReceivedActivity::class.java))
                        else -> {
                            try {
                                val shareIntent = Intent(Intent.ACTION_SEND)
                                shareIntent.type = "text/plain"
                                shareIntent.putExtra(
                                    Intent.EXTRA_SUBJECT,
                                    "Dopsender: Share Files Using Wifi-Direct"
                                )
                                var shareMessage =
                                    "\nHey Friend! Install this amazing app to share file using wifi direct at amazing speeds.\n\n No hotspot, no odd permissions. \n\n"
                                shareMessage =
                                    """
                                    $shareMessage Dopsender: Share Files Using Wifi-Direct${'\n'}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}
                                    """.trimIndent()
                                shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
                                startActivity(
                                    Intent.createChooser(
                                        shareIntent,
                                        "Send Message in..."
                                    )
                                )
                            } catch (e: Exception) {
                                Toast.makeText(
                                    requireContext(),
                                    "Something went wrong!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                        }
                    }
                }
            }

        }

        methodRequiresTwoPermission()

        if (!requireContext().getHomeHelperShown()) {

            TapTargetView.showFor(requireActivity(),
                TapTarget.forView(
                    root.sendButton,
                    "Click on Connect",
                    "On both sending and receiving devices."
                ).outerCircleColor(R.color.colorPrimary) // Specify a color for the outer circle
                    .outerCircleAlpha(0.98f) // Specify the alpha amount for the outer circle
                    .targetCircleColor(R.color.pureWhite) // Specify a color for the target circle
                    .titleTextSize(20) // Specify the size (in sp) of the title text
                    .titleTextColor(R.color.pureWhite) // Specify the color of the title text
                    .descriptionTextSize(16) // Specify the size (in sp) of the description text
                    .descriptionTextColor(R.color.pureWhite) // Specify the color of the description text
                    .textColor(R.color.pureWhite) // Specify a color for both the title and description text
                    .textTypeface(Typeface.SANS_SERIF) // Specify a typeface for the text
                    .dimColor(R.color.pureBlack) // If set, will dim behind the view with 30% opacity of the given color
                    .drawShadow(true) // Whether to draw a drop shadow or not
                    .cancelable(true) // Whether tapping outside the outer circle dismisses the view
                    .tintTarget(true) // Whether to tint the target view's color
                    .transparentTarget(true) // Specify whether the target is transparent (displays the content underneath)
                    .targetRadius(20),  // Specify the target radius (in dp)
                object : TapTargetView.Listener() {
                    override fun onTargetClick(view: TapTargetView?) {
                        super.onTargetClick(view)
                        Toast.makeText(requireContext(), "Ready to go!", Toast.LENGTH_SHORT).show()
                    }
                })
            requireContext().setHomeHelperShown()
        }
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