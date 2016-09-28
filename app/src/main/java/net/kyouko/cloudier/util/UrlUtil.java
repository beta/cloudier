package net.kyouko.cloudier.util;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;

import net.kyouko.cloudier.R;

/**
 * Util class for opening URLs.
 *
 * @author beta
 */
public class UrlUtil {

    public static void openUrl(Activity activity, String url) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0,
                shareIntent, 0);

        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
        CustomTabsIntent customTabsIntent = builder
                .setToolbarColor(activity.getResources().getColor(R.color.light_blue_700))
                .setShowTitle(true)
                .addMenuItem(activity.getString(R.string.title_action_share_via), pendingIntent)
                .setStartAnimations(activity, R.anim.slide_in_from_right, R.anim.slide_out_to_left)
                .setExitAnimations(activity, R.anim.slide_in_from_left, R.anim.slide_out_to_right)
                .build();
        customTabsIntent.launchUrl(activity, Uri.parse(url));
    }

}
