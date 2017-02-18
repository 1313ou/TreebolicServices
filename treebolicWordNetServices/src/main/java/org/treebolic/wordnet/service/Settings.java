package org.treebolic.wordnet.service;

import org.treebolic.TreebolicIface;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;

/**
 * Settings
 *
 * @author Bernard Bou
 */
public class Settings
{
	/**
	 * Initialized preference name
	 */
	public static final String PREF_INITIALIZED = "pref_initialized"; //$NON-NLS-1$

	/**
	 * Download service type name
	 */
	public static final String PREF_SERVICE = "pref_service"; //$NON-NLS-1$

	/**
	 * Download preference name
	 */
	public static final String PREF_DOWNLOAD = "pref_download"; //$NON-NLS-1$

	/**
	 * Set default initial settings
	 *
	 * @param context
	 *            context
	 */
	static public void setDefaults(final Context context)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		final Editor editor = sharedPref.edit();
		editor.putString(TreebolicIface.PREF_SOURCE, "force"); //$NON-NLS-1$
		editor.putString(Settings.PREF_DOWNLOAD, "http://treebolic.sourceforge.net/data/wordnet/wordnet31.zip"); //$NON-NLS-1$
		editor.putString(Settings.PREF_SERVICE, "IntentService"); //$NON-NLS-1$
		editor.commit();
	}

	/**
	 * Get string preference
	 *
	 * @param context
	 *            context
	 * @param key
	 *            key
	 * @return value
	 */
	static public String getStringPref(final Context context, final String key)
	{
		final SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
		final String result = sharedPref.getString(key, null);
		return result;
	}

	// U T I L S

	/**
	 * Application settings
	 *
	 * @param context
	 *            context
	 * @param pkgName
	 *            package name
	 */
	static public void applicationSettings(final Context context, final String pkgName)
	{
		final int apiLevel = Build.VERSION.SDK_INT;
		final Intent intent = new Intent();

		if (apiLevel >= 9)
		{
			intent.setAction(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
			intent.setData(Uri.parse("package:" + pkgName)); //$NON-NLS-1$
		}
		else
		{
			final String appPkgName = apiLevel == 8 ? "pkg" : "com.android.settings.ApplicationPkgName"; //$NON-NLS-1$ //$NON-NLS-2$

			intent.setAction(Intent.ACTION_VIEW);
			intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails"); //$NON-NLS-1$ //$NON-NLS-2$
			intent.putExtra(appPkgName, pkgName);
		}

		// start Activity
		context.startActivity(intent);
	}
}
