/*
 * Copyright (c) Treebolic 2023. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.owl;

import android.os.Bundle;

import org.treebolic.AppCompatCommonPreferenceActivity;
import org.treebolic.TreebolicIface;
import org.treebolic.preference.OpenEditTextPreference;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

/**
 * Settings activity
 *
 * @author Bernard Bou
 */
public class SettingsActivity extends AppCompatCommonPreferenceActivity
{
	// F R A G M E N T S

	@SuppressWarnings("WeakerAccess")
	public static class GeneralPreferenceFragment extends PreferenceFragmentCompat
	{
		@Override
		public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey)
		{
			// inflate
			addPreferencesFromResource(R.xml.pref_general);

			// bind
			final EditTextPreference sourcePref = findPreference(TreebolicIface.PREF_SOURCE);
			assert sourcePref != null;
			sourcePref.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());

			final EditTextPreference basePref = findPreference(TreebolicIface.PREF_BASE);
			assert basePref != null;
			basePref.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());

			final EditTextPreference imageBasePref = findPreference(TreebolicIface.PREF_IMAGEBASE);
			assert imageBasePref != null;
			imageBasePref.setSummaryProvider(EditTextPreference.SimpleSummaryProvider.getInstance());

			final ListPreference servicePref = findPreference(Settings.PREF_SERVICE);
			assert servicePref != null;
			servicePref.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
		}
	}

	@SuppressWarnings("WeakerAccess")
	public static class ServicesPreferenceFragment extends PreferenceFragmentCompat
	{
		@Override
		public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey)
		{
			// inflate
			addPreferencesFromResource(R.xml.pref_service);

			// bind
			final ListPreference servicePref = findPreference(Settings.PREF_SERVICE);
			assert servicePref != null;
			servicePref.setSummaryProvider(ListPreference.SimpleSummaryProvider.getInstance());
		}

		@Override
		public void onDisplayPreferenceDialog(@NonNull final Preference preference)
		{
			if (!OpenEditTextPreference.onDisplayPreferenceDialog(this, preference))
			{
				super.onDisplayPreferenceDialog(preference);
			}
		}
	}

	@SuppressWarnings("WeakerAccess")
	public static class DownloadPreferenceFragment extends PreferenceFragmentCompat
	{
		@Override
		public void onCreatePreferences(final Bundle savedInstanceState, final String rootKey)
		{
			// inflate
			addPreferencesFromResource(R.xml.pref_download);

			final OpenEditTextPreference downloadPref = findPreference(Settings.PREF_DOWNLOAD);
			assert downloadPref != null;
			downloadPref.setSummaryProvider(OpenEditTextPreference.SUMMARY_PROVIDER);
		}

		@Override
		public void onDisplayPreferenceDialog(@NonNull final Preference preference)
		{
			if (!OpenEditTextPreference.onDisplayPreferenceDialog(this, preference))
			{
				super.onDisplayPreferenceDialog(preference);
			}
		}
	}
}
