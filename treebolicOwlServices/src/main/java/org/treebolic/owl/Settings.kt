/*
 * Copyright (c) Treebolic 2023. Bernard Bou <1313ou@gmail.com>
 */
package org.treebolic.owl

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.preference.PreferenceManager
import org.treebolic.TreebolicIface
import org.treebolic.owl.BuildConfig
import org.treebolic.services.iface.ITreebolicService
import org.treebolic.storage.Storage.getTreebolicStorage

/**
 * Settings
 *
 * @author Bernard Bou
 */
object Settings {

    /**
     * Demo archive
     */
    const val DEMOZIP: String = "owl.zip"

    /**
     * Demo
     */
    private const val DEMO: String = "pizza.owl"

    /**
     * Initialized preference name
     */
    const val PREF_INITIALIZED: String = "pref_initialized_" + BuildConfig.VERSION_NAME

    /**
     * Service preference name
     */
    const val PREF_SERVICE: String = "pref_service"

    /**
     * Download preference name
     */
    const val PREF_DOWNLOAD: String = "pref_download"

    /**
     * Save source and base to preferences
     *
     * @param context context
     * @param source  source
     * @param base    base
     */
    @JvmStatic
    fun save(context: Context, source: String?, base: String?) {
        putStringPref(context, TreebolicIface.PREF_SOURCE, source)
        putStringPref(context, TreebolicIface.PREF_BASE, base)
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

    /**
     * Put string preference
     *
     * @param context context
     * @param key     key
     * @param value   value
     */
    @SuppressLint("CommitPrefEdits", "ApplySharedPref")
    fun putStringPref(context: Context, key: String?, value: String?) {
        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        sharedPref.edit().putString(key, value).commit()
    }

    // U T I L S

    /**
     * Set default initial settings
     *
     * @param context context
     */
    @JvmStatic
    @SuppressLint("CommitPrefEdits", "ApplySharedPref")
    fun setDefaults(context: Context) {
        val treebolicStorage = getTreebolicStorage(context)
        val uri = Uri.fromFile(treebolicStorage)
        val treebolicBase = "$uri/"

        val sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        val editor = sharedPref.edit()
        editor.putString(PREF_SERVICE, ITreebolicService.TYPE_BROADCAST)
        editor.putString(TreebolicIface.PREF_BASE, treebolicBase)
        editor.putString(TreebolicIface.PREF_IMAGEBASE, treebolicBase)
        editor.putString(TreebolicIface.PREF_SOURCE, DEMO)
        editor.commit()
    }

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
            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.setData(Uri.parse("package:$pkgName"))
        } else {
            val appPkgName = if (apiLevel == 8) "pkg" else "com.android.settings.ApplicationPkgName"

            intent.setAction(Intent.ACTION_VIEW)
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails")
            intent.putExtra(appPkgName, pkgName)
        }

        // start Activity
        context.startActivity(intent)
    }
}
