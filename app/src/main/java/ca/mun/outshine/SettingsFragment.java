package ca.mun.outshine;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceGroup;
import android.support.v7.preference.PreferenceManager;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = "SettingsFragment";
    SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.pref_settings, rootKey);
        // accessing default shared preference
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        // listener
        onSharedPreferenceChanged(sharedPreferences, getString(R.string.key_sync_frequency));
        initSummary(getPreferenceScreen());
    }

    @Override
    public void onResume() {
        super.onResume();
        // register the preferenceChange listener
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        // unregister the preference change listener
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        String key = preference.getKey();
        if (key.equals(getString(R.string.key_send_feedback))) {
            sendFeedback(getContext());
            return true;
        }
        return false;
        // return super.onPreferenceTreeClick(preference);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        // find the preference
        Preference preference = findPreference(key);
        if (preference instanceof ListPreference) {
            // if it is list preference cast preference to ListPreference
            ListPreference listPreference = (ListPreference) preference;
            // find the index of changed value in settings
            int prefIndex = listPreference.findIndexOfValue(sharedPreferences.getString(key, ""));
            if (prefIndex >= 0) {
                // finally set's it value changed
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (!(preference instanceof CheckBoxPreference)) {
            preference.setSummary(sharedPreferences.getString(key, ""));
        }
    }

    // walks through all preferences
    private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            setPreferenceSummary(p);
        }
    }

    // sets up summary providers for the preferences
    private void setPreferenceSummary(Preference p) {
        // no need to set up preference summaries for checkbox preferences because
        // they can be set up in xml using summaryOff and summary On
        if (p instanceof EditTextPreference) {
            String key = p.getKey();
            if (!sharedPreferences.getString(key, "").isEmpty()) {
                p.setSummary(sharedPreferences.getString(key, ""));
            }
        } else if (p instanceof ListPreference) {
            String key = p.getKey();
            ListPreference listPreference = (ListPreference) p;
            int prefIndex = listPreference.findIndexOfValue(sharedPreferences.getString(key, ""));
            if (prefIndex >= 0) {
                p.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }
    }

    // send support email to developers
    private void sendFeedback(Context context) {
        String body = null;
        try {
            body = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
            body = "\n\n-----------------------------\nPlease don't remove this information\n Device OS: Android \n Device OS version: " +
                    Build.VERSION.RELEASE + "\n App Version: " + body + "\n Device Brand: " + Build.BRAND +
                    "\n Device Model: " + Build.MODEL + "\n Device Manufacturer: " + Build.MANUFACTURER;
        } catch (PackageManager.NameNotFoundException e) {
        }
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"af4166@mun.ca"});
        intent.putExtra(Intent.EXTRA_SUBJECT, "Query from android app");
        intent.putExtra(Intent.EXTRA_TEXT, body);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.choose_email_client)));
    }

}
