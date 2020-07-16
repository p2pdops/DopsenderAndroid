package p2pdops.dopsender

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.internal.common.CommonUtils.hideKeyboard
import kotlinx.android.synthetic.main.activity_name_choose.*
import kotlinx.android.synthetic.main.dp_item.view.*
import p2pdops.dopsender.utils.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions


class NameChooseActivity : AppCompatActivity(), EasyPermissions.PermissionCallbacks {

    companion object {
        private const val TAG = "NameChooseActivity"
    }


    val dps = dpsMap.entries.toList()

    class DpHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.activity_name_choose)

        supportActionBar!!.hide()

        getLocalDpRes().let { circleImageView.setImageResource(it) }
        getLocalName().let { nameInp.setText(it) }


        nameInp.setOnFocusChangeListener { v: View, hasFocus: Boolean ->
            if (!hasFocus) {
                hideKeyboard(this, v)
            }
        }

        dps_recycler.setHasFixedSize(true)
        dps_recycler.layoutManager = GridLayoutManager(this, 4)
        dps_recycler.adapter = object : RecyclerView.Adapter<DpHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DpHolder {
                return DpHolder(
                    LayoutInflater.from(this@NameChooseActivity)
                        .inflate(R.layout.dp_item, parent, false)
                )
            }

            override fun getItemCount(): Int = dps.size

            override fun onBindViewHolder(holder: DpHolder, position: Int) {
                holder.itemView.dpView.setImageDrawable(
                    AppCompatResources.getDrawable(
                        this@NameChooseActivity,
                        dps[position].value
                    )
                )

                holder.itemView.setOnClickListener {
                    setLocalDpKey(dps[position].key)
                    circleImageView.setImageResource(dps[position].value)
                }
            }
        }
        continue_btn.setOnClickListener {
            if (nameInp.editableText.toString().isNotEmpty())
                methodRequiresTwoPermission()
            else {
                nameInp.error = "You must enter your name"
            }
        }
    }

    @AfterPermissionGranted(MainActivity.PERMISSION_LOCATION_AND_LOCATION)
    private fun methodRequiresTwoPermission() {
        val perms = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (EasyPermissions.hasPermissions(this, *perms)) {
            setLocalName(nameInp.editableText.toString())
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "Dopsender needs location permission to scan devices around you, storage permission to read and share your files. This app works only if you approve them.",
                MainActivity.PERMISSION_LOCATION_AND_LOCATION,
                *perms
            )
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AppSettingsDialog.DEFAULT_SETTINGS_REQ_CODE) {
            Toast.makeText(this, "Got Result From Settings app.", Toast.LENGTH_SHORT).show()
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}