package p2pdops.dopsender.ui.main_home.slideshow

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.fragment_about.view.*
import p2pdops.dopsender.R


class AboutFragment : Fragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        val root = inflater.inflate(R.layout.fragment_about, container, false)

        root.github.setOnClickListener {
            val url = "https://github.com/p2pdops"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        root.facebook.setOnClickListener {
            val url = "https://facebook.com/p2pdops"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        root.twitter.setOnClickListener {
            val url = "https://twitter.com/p2pdops"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
        root.instagram.setOnClickListener {
            val url = "https://instagram.com/p2pdops"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }

        root.viewLicenses.setOnClickListener {
            val url = "https://sites.google.com/view/dopsender/licences"
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(url)
            startActivity(i)
        }
        return root
    }
}