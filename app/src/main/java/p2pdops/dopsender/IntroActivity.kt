package p2pdops.dopsender

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils

import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager

import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.BaseOnTabSelectedListener

import p2pdops.dopsender.pagers.IntroViewPagerAdapter
import p2pdops.dopsender.pagers.ScreenItem
import kotlinx.android.synthetic.main.activity_intro.*


class IntroActivity : AppCompatActivity() {
    private var screenPager: ViewPager? = null
    private var introViewPagerAdapter: IntroViewPagerAdapter? = null

    var position = 0

    private var btnAnim: Animation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // make the activity on full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        if (restorePrefData()) {
            val mainActivity = Intent(applicationContext, MainActivity::class.java)
//            startActivity(mainActivity)
//            finish()
        }

        setContentView(R.layout.activity_intro)

        supportActionBar!!.hide()

        btnAnim = AnimationUtils.loadAnimation(applicationContext, R.anim.button_animation)

        val mList: MutableList<ScreenItem> = ArrayList()

        mList.add(
            ScreenItem(
                "Hi, Welcome!",
                "",
                R.mipmap.ic_launcher
            )
        )

        mList.add(
            ScreenItem(
                "Fast Delivery",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua, consectetur  consectetur adipiscing elit",
                R.drawable.ic_video
            )
        )

        mList.add(
            ScreenItem(
                "Easy Payment",
                "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua, consectetur  consectetur adipiscing elit",
                R.drawable.ic_video
            )
        )

        screenPager = findViewById(R.id.screen_viewpager)
        introViewPagerAdapter = IntroViewPagerAdapter(this, mList)
        screenPager?.adapter = introViewPagerAdapter

        tab_indicator.setupWithViewPager(screenPager)

        btn_next.setOnClickListener {
            position = screenPager?.currentItem!!
            if (position < mList.size) {
                position++
                screenPager?.currentItem = position
            }
            if (position == mList.size - 1) { // when we rech to the last screen

                // TODO : show the GETSTARTED Button and hide the indicator and the next button
                loaddLastScreen()
            }
        }

        // tablayout add change listener
        tab_indicator.addOnTabSelectedListener(object : BaseOnTabSelectedListener<TabLayout.Tab?> {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab!!.position == mList.size - 1) {
                    loaddLastScreen()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


        // Get Started button click listener
        btn_get_started.setOnClickListener {


            //open main activity
            val mainActivity = Intent(applicationContext, MainActivity::class.java)
            startActivity(mainActivity)
            // also we need to save a boolean value to storage so next time when the user run the app
            // we could know that he is already checked the intro screen activity
            // i'm going to use shared preferences to that process
            savePrefsData()
            finish()
        }

        // skip button click listener
        tv_skip.setOnClickListener {
            screenPager?.currentItem = mList.size
        }
    }

    private fun restorePrefData(): Boolean {
        val pref = applicationContext.getSharedPreferences(
            "myPrefs",
            Context.MODE_PRIVATE
        )
        return pref.getBoolean("isIntroOpened", false)
    }

    private fun savePrefsData() {
        val pref = applicationContext.getSharedPreferences(
            "myPrefs",
            Context.MODE_PRIVATE
        )
        val editor = pref.edit()
        editor.putBoolean("isIntroOpnend", true)
        editor.apply()
    }

    // show the GETSTARTED Button and hide the indicator and the next button
    private fun loaddLastScreen() {
        btn_next.visibility = View.INVISIBLE
        btn_get_started.visibility = View.VISIBLE
        tv_skip!!.visibility = View.INVISIBLE
        tab_indicator!!.visibility = View.INVISIBLE
//         TODO : ADD an animation the getstarted button
        // setup animation
        btn_get_started.animation = btnAnim
    }
}