package p2pdops.dopsender.adapters

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import androidx.core.graphics.drawable.toBitmap
import com.squareup.picasso.Picasso
import com.squareup.picasso.Request
import com.squareup.picasso.RequestHandler
import java.io.IOException


class AppIconRequestHandler(context: Context) : RequestHandler() {
    private val mPackageManager: PackageManager = context.packageManager
    override fun canHandleRequest(data: Request): Boolean {
        return true
    }

    @Throws(IOException::class)
    override fun load(request: Request, networkPolicy: Int): Result? {
        val packageName: String = request.uri.toString()
        val drawable: Drawable
        drawable = try {
            mPackageManager.getApplicationIcon(packageName)
        } catch (ignored: PackageManager.NameNotFoundException) {
            return null
        }
        val bitmap = drawable.toBitmap()
        return Result(bitmap, Picasso.LoadedFrom.DISK)
    }

    companion object {
        fun getUri(packageName: String?): Uri {
            return Uri.parse(packageName)// Uri.fromParts(SCHEME_APP_ICON, packageName, null)
        }
    }

}
