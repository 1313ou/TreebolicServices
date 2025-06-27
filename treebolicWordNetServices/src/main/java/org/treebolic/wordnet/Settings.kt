/*
 * Copyright (c) Treebolic 2023. Bernard Bou <1313ou@gmail.com>
 */
package org.treebolic.wordnet

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import org.treebolic.TreebolicIface
import org.treebolic.services.iface.ITreebolicService
import androidx.core.content.edit

/**
 * Settings
 *
 * @author Bernard Bou
 */
object Settings {

    /**
     * Initialized preference name
     */
    const val PREF_INITIALIZED: String = "pref_initialized_" + BuildConfig.VERSION_NAME

    /**
     * Download service type name
     */
    const val PREF_SERVICE: String = "pref_service"

    /**
     * Download preference name
     */
    const val PREF_DOWNLOAD: String = "pref_download"

    /**
     * Set default initial settings
     *
     * @param context context
     */
    @JvmStatic
    @SuppressLint("CommitPrefEdits", "ApplySharedPref")
    fun setDefaults(context: Context) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit(commit = true) {
            putString(PREF_SERVICE, ITreebolicService.TYPE_BROADCAST)
            putString(TreebolicIface.PREF_SOURCE, "love")
            putString(PREF_DOWNLOAD, "http://treebolic.sourceforge.net/data/wordnet/wordnet31.zip")
        }
    }

    /**
     * Put string preference
     *
     * @param context context
     * @param key     key
     * @param value   value
     */
    @JvmStatic
    @SuppressLint("CommitPrefEdits", "ApplySharedPref")
    fun putStringPref(context: Context, key: String?, value: String?) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit(commit = true) { putString(key, value) }
    }

    /**
     * Get string preference
     *
     * @param context context
     * @param key     key
     * @return value
     */
    @JvmStatic
    fun getStringPref(context: Context, key: String?): String? {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        return sharedPref.getString(key, null)
    }

    // U T I L S

    /**
     * Application settings
     *
     * @param context context
     * @param pkgName package name
     */
    @JvmStatic
    fun applicationSettings(context: Context, pkgName: String) {
        val apiLevel = Build.VERSION.SDK_INT
        val intent = Intent()

        if (apiLevel >= 9) {
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            intent.data = "package:$pkgName".toUri()
        } else {
            val appPkgName = if (apiLevel == 8) "pkg" else "com.android.settings.ApplicationPkgName"

            intent.action = Intent.ACTION_VIEW
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails")
            intent.putExtra(appPkgName, pkgName)
        }

        // start Activity
        context.startActivity(intent)
    }
}
