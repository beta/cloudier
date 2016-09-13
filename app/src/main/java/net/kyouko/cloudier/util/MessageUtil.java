package net.kyouko.cloudier.util;

import android.content.Context;
import android.support.annotation.StringRes;
import android.widget.Toast;

/**
 * Util class for displaying messages to user.
 *
 * @author beta
 */
public class MessageUtil {

    public static void showToast(Context context, @StringRes int messageId) {
        showToast(context.getApplicationContext(), messageId, Toast.LENGTH_SHORT);
    }


    public static void showToast(Context context, @StringRes int messageId, int duration) {
        Toast.makeText(context.getApplicationContext(), messageId, duration).show();
    }


    public static void showToast(Context context, String message) {
        showToast(context.getApplicationContext(), message, Toast.LENGTH_SHORT);
    }


    public static void showToast(Context context, String message, int duration) {
        Toast.makeText(context.getApplicationContext(), message, duration).show();
    }

}
