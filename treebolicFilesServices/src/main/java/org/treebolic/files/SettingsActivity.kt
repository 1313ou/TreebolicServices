/*
 * Copyright (c) Treebolic 2023. Bernard Bou <1313ou@gmail.com>
 */
package org.treebolic.files

import android.os.Bundle
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import org.treebolic.AppCompatCommonPreferenceActivity
import org.treebolic.TreebolicIface

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
            val sourcePref: Preference = findPreference(TreebolicIface.PREF_SOURCE)!!
            sourcePref.summaryProvider = EditTextPreference.SimpleSummaryProvider.getInstance()
        }
    }

    class ServicePreferenceFragment : PreferenceFragmentCompat() {

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            // inflate
            addPreferencesFromResource(R.xml.pref_service)

            // bind
            val servicePref: Preference = findPreference(Settings.PREF_SERVICE)!!
            servicePref.summaryProvider = ListPreference.SimpleSummaryProvider.getInstance()
        }
    }
}
