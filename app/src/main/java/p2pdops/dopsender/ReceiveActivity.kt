package p2pdops.dopsender


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_receive.*


class ReceiveActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "ReceiveActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive)
        BottomSheetBehavior.from(devicesSheet).state = BottomSheetBehavior.STATE_HIDDEN

//        loadSendOptions(send_options_recycler)
    }

    fun openDevicesSheet(view: View) {
        closeSendSheet(View(this))
        BottomSheetBehavior.from(devicesSheet).state = BottomSheetBehavior.STATE_HALF_EXPANDED
        BottomSheetBehavior.from(devicesSheet).isHideable = false
    }

    fun closeDevicesSheet(view: View) {
        BottomSheetBehavior.from(devicesSheet).isHideable = true
        BottomSheetBehavior.from(devicesSheet).state = BottomSheetBehavior.STATE_HIDDEN
    }

    fun openSendSheet(view: View) {
        closeDevicesSheet(View(this))
        BottomSheetBehavior.from(sendSheet).state = BottomSheetBehavior.STATE_HALF_EXPANDED
        BottomSheetBehavior.from(sendSheet).isHideable = false
    }

    fun closeSendSheet(view: View) {
        BottomSheetBehavior.from(sendSheet).isHideable = true
        BottomSheetBehavior.from(sendSheet).state = BottomSheetBehavior.STATE_HIDDEN
    }

}