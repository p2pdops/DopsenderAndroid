package p2pdops.dopsender

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.animation.Animation
import android.view.animation.AnimationUtils

import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_intro.*


class IntroActivity : AppCompatActivity() {

    var position = 0

    private var btnAnim: Animation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // make the activity on full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.activity_intro)

        supportActionBar!!.hide()

        btnAnim = AnimationUtils.loadAnimation(applicationContext, R.anim.button_animation)

        loadLastScreen()

        // Get Started button click listener
        btn_get_started.setOnClickListener {
            val mainActivity = Intent(applicationContext, NameChooseActivity::class.java)
            startActivity(mainActivity)
            finish()
        }
    }

    private fun loadLastScreen() {
//        btn_next.visibility = View.INVISIBLE
        btn_get_started.visibility = View.VISIBLE
        tv_skip!!.visibility = View.INVISIBLE
//        tab_indicator!!.visibility = View.INVISIBLE
//         TODO : ADD an animation the getstarted button
        // setup animation
        btn_get_started.animation = btnAnim
    }
}