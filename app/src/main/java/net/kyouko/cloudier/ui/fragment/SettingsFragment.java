package net.kyouko.cloudier.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;

import net.kyouko.cloudier.R;
import net.kyouko.cloudier.util.ImageUtil;

import de.psdev.licensesdialog.LicensesDialog;

/**
 * Fragment for app's settings.
 *
 * @author beta
 */
public class SettingsFragment extends PreferenceFragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        Preference openSourceLicensesPref = findPreference("about.licenses");
        openSourceLicensesPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new LicensesDialog.Builder(getActivity())
                        .setNotices(R.raw.notices)
                        .build()
                        .show();
                return true;
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.startsWith("image.")) {
            ImageUtil.recreateInstance(getActivity());
        }
    }

}
