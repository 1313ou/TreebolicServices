/*
 * Copyright (c) Treebolic 2023. Bernard Bou <1313ou@gmail.com>
 */
package org.treebolic.wordnet

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.treebolic.AppCompatCommonPreferenceActivity
import org.treebolic.TreebolicIface
import org.treebolic.preference.OpenEditTextPreference
import org.treebolic.preference.OpenEditTextPreference.Companion.onDisplayPreferenceDialog

/**
 * Settings activity
 *
 * @author Bernard Bou
 */
class SettingsActivity : AppCompatCommonPreferenceActivity() {

    // F R A G M E N T S

    class GeneralPreferenceFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            // inflate
            addPreferencesFromResource(R.xml.pref_general)

            // bind
            val sourcePref = checkNotNull(findPreference<EditTextPreference>(TreebolicIface.PREF_SOURCE))
            sourcePref.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        }
    }

    class DownloadPreferenceFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            // inflate
            addPreferencesFromResource(R.xml.pref_download)

            // bind
            val downloadPref = checkNotNull(findPreference<OpenEditTextPreference>(Settings.PREF_DOWNLOAD))
            downloadPref.summaryProvider = OpenEditTextPreference.SUMMARY_PROVIDER
        }

        override fun onDisplayPreferenceDialog(preference: Preference) {
            if (!onDisplayPreferenceDialog(this, preference)) {
                super.onDisplayPreferenceDialog(preference)
            }
        }
    }

    class ServicePreferenceFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            // inflate
            addPreferencesFromResource(R.xml.pref_service)

            // bind
            val servicePref = checkNotNull(findPreference<ListPreference>(Settings.PREF_SERVICE))
            servicePref.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        }
    }
}
