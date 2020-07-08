package p2pdops.dopsender


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_dummyy.*
import p2pdops.dopsender.modals.ConnectionItem
import p2pdops.dopsender.send_helpers.ConnectionMessageAdapter
import p2pdops.dopsender.send_helpers.setupSendOptions


class DummyyActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DummyyActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dummyy)

        rippleBackground.startRippleAnimation()
    }
}