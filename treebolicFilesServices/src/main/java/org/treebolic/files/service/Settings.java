/*
 * Copyright (c) Treebolic 2019. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.files.service;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;

import org.treebolic.TreebolicIface;
import org.treebolic.storage.Storage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

/**
 * Settings
 *
 * @author Bernard Bou
 */
@SuppressWarnings("WeakerAccess")
public class Settings
{
	/**
	 * Initialized preference name
	 */
	public static final String PREF_INITIALIZED = "pref_initialized";

	/**
	 * Service type preference name
	 */
	public static final String PREF_SERVICE = "pref_service";

	/**
	 * Set default initial settings
	 *
	 * @param context context
	 */
	@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
	static public void setDefaults(final Context context)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

		final String externalStorage = Storage.getExternalStorage();
		//final File treebolicStorage = Storage.getTreebolicStorage(context);
		//final Uri uri = Uri.fromFile(treebolicStorage);

		final Editor editor = sharedPref.edit();
		editor.putString(Settings.PREF_SERVICE, "IntentService");
		editor.putString(TreebolicIface.PREF_SOURCE, externalStorage);
		editor.commit();
	}

	/**
	 * Get string preference
	 *
	 * @param context context
	 * @param key     key
	 * @return value
	 */
	@Nullable
	static public String getStringPref(final Context context, final String key)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		return sharedPref.getString(key, null);
	}

	/**
	 * Put string preference
	 *
	 * @param context context
	 * @param key     key
	 * @param value   value
	 */
	@SuppressLint({"CommitPrefEdits", "ApplySharedPref"})
	static public void putStringPref(final Context context, @SuppressWarnings("SameParameterValue") final String key, final String value)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		sharedPref.edit().putString(key, value).commit();
	}

	// U T I L S

	/**
	 * Application settings
	 *
	 * @param context context
	 * @param pkgName package name
	 */
	static public void applicationSettings(@NonNull final Context context, @SuppressWarnings("SameParameterValue") final String pkgName)
	{
		final int apiLevel = Build.VERSION.SDK_INT;
		final Intent intent = new Intent();

		if (apiLevel >= 9)
		{
			intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			intent.setData(Uri.parse("package:" + pkgName));
		}
		else
		{
			final String appPkgName = apiLevel == 8 ? "pkg" : "com.android.settings.ApplicationPkgName";

			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
			intent.putExtra(appPkgName, pkgName);
		}

		// start Activity
		context.startActivity(intent);
	}
}
