/*
 * Copyright (c) Treebolic 2023. Bernard Bou <1313ou@gmail.com>
 */

package org.treebolic.files;

import android.os.Bundle;

import org.treebolic.AppCompatCommonPreferenceActivity;
import org.treebolic.TreebolicIface;
import org.treebolic.files.service.R;

import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
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
	}
}
