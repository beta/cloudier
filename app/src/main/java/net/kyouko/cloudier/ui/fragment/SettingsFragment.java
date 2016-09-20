package net.kyouko.cloudier.ui.fragment;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.customtabs.CustomTabsIntent;

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

        Preference gitHubPref = findPreference("about.github");
        gitHubPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                String url = getString(R.string.text_setting_summary_github);

                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, url);
                PendingIntent pendingIntent = PendingIntent.getActivity(
                        getActivity().getApplicationContext(), 0, shareIntent, 0);

                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
                CustomTabsIntent customTabsIntent = builder
                        .setToolbarColor(getResources().getColor(R.color.light_blue_700))
                        .setShowTitle(true)
                        .addMenuItem(getString(R.string.title_action_share_via), pendingIntent)
                        .setStartAnimations(getActivity(), R.anim.slide_in_from_right, R.anim.slide_out_to_left)
                        .setExitAnimations(getActivity(), R.anim.slide_in_from_left, R.anim.slide_out_to_right)
                        .build();
                customTabsIntent.launchUrl(getActivity(), Uri.parse(url));
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
