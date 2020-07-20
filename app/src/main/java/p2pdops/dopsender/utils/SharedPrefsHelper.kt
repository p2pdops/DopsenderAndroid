package p2pdops.dopsender.utils

import android.app.Activity
import android.content.Context
import p2pdops.dopsender.R

fun getDpForKey(key: String) = dpsMap[key] ?: R.drawable.dp_boy_1

fun Context.getLocalDpKey(): String {
    val pref = applicationContext.getSharedPreferences(
        "dopsender",
        Context.MODE_PRIVATE
    )
    return pref.getString(getString(R.string.app_user_dp), "boy1") ?: "boy1"
}

fun Context.getLocalDpRes(): Int {
    return dpsMap[getLocalDpKey()] ?: R.drawable.dp_boy_1
}

fun Context.getLocalName(): String {
    val pref = applicationContext.getSharedPreferences(
        "dopsender",
        Context.MODE_PRIVATE
    )
    return pref.getString(getString(R.string.app_user_name), "") ?: ""
}

fun Activity.setLocalName(name: String) {
    val pref = applicationContext.getSharedPreferences(
        "dopsender",
        Context.MODE_PRIVATE
    )
    val editor = pref.edit()
    editor.putBoolean(getString(R.string.is_name_set), true)
    editor.putString(getString(R.string.app_user_name), name)
    editor.apply()
}

fun Activity.setLocalDpKey(key: String) {
    val pref = applicationContext.getSharedPreferences(
        "dopsender",
        Context.MODE_PRIVATE
    )
    val editor = pref.edit()
    editor.putBoolean(getString(R.string.is_name_set), true)
    editor.putString(getString(R.string.app_user_dp), key)
    editor.apply()
}


fun Context.getHomeHelperShown(): Boolean {
    val pref = applicationContext.getSharedPreferences(
        "dopsender",
        Context.MODE_PRIVATE
    )
    return pref.getBoolean(getString(R.string.app_main_home_helper), false)
}

fun Context.setHomeHelperShown() {
    val pref = applicationContext.getSharedPreferences(
        "dopsender",
        Context.MODE_PRIVATE
    )
    val editor = pref.edit()
    editor.putBoolean(getString(R.string.app_main_home_helper), true)
    editor.apply()
}

fun Context.getDeviceUnSupported(): Boolean {
    val pref = applicationContext.getSharedPreferences("dopsender", Context.MODE_PRIVATE)
    return pref.getBoolean(getString(R.string.app_device_unsupported), false)
}

