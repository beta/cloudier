package net.kyouko.cloudier.util;

import android.content.Context;
import android.content.Intent;

import net.kyouko.cloudier.model.Account;
import net.kyouko.cloudier.ui.activity.AuthActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Util class for account authorization.
 *
 * @author beta
 */
public class AuthUtil {


    public static boolean hasAuthorized(Context context) {
        return PreferenceUtil.with(context).getBoolean(PreferenceUtil.PREF_HAS_AUTHORIZED);
    }


    /**
     * Starts {@link AuthActivity} for user authorization.
     *
     * @param context {@link Context} used for start the activity.
     */
    public static void startAuth(Context context) {
        Intent authIntent = new Intent(context, AuthActivity.class);
        authIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(authIntent);
    }


    /**
     * Reads a saved account from the preferences.
     *
     * @param context {@link Context} for reading preferences.
     * @return an account.
     */
    public static Account readAccount(Context context) {
        PreferenceUtil pref = PreferenceUtil.with(context);

        Account account = new Account();
        account.accessToken = pref.getString(PreferenceUtil.PREF_ACCOUNT_ACCESS_TOKEN);
        account.expiresIn = pref.getString(PreferenceUtil.PREF_ACCOUNT_EXPIRES_IN);
        account.refreshToken = pref.getString(PreferenceUtil.PREF_ACCOUNT_REFRESH_TOKEN);
        account.username = pref.getString(PreferenceUtil.PREF_ACCOUNT_USERNAME);

        return account;
    }


    /**
     * Saves an account into preferences.
     *
     * @param context {@link Context} for writing preferences.
     * @param account {@link Account} to save.
     */
    public static void saveAccount(Context context, Account account) {
        PreferenceUtil pref = PreferenceUtil.with(context);

        pref.set(PreferenceUtil.PREF_ACCOUNT_ACCESS_TOKEN, account.accessToken)
                .set(PreferenceUtil.PREF_ACCOUNT_EXPIRES_IN, String.valueOf(account.expiresIn))
                .set(PreferenceUtil.PREF_ACCOUNT_REFRESH_TOKEN, account.refreshToken)
                .set(PreferenceUtil.PREF_ACCOUNT_USERNAME, account.username)
                .set(PreferenceUtil.PREF_HAS_AUTHORIZED, true)
                .save();
    }


    /**
     * Parses an account from the callback URL of user authorization.
     *
     * @param url callback URL containing account information.
     * @return an Account.
     */
    public static Account parseAccountFromUrl(String url) {
        String paramPart = url.substring(url.indexOf("#") + 1);

        Map<String, String> params = new HashMap<>();
        for (String param : paramPart.split("&")) {
            String[] parts = param.split("=");
            if (parts.length == 2) {
                params.put(parts[0], parts[1]);
            }
        }

        Account account = new Account();
        account.accessToken = params.get("access_token");
        account.expiresIn = params.get("expires_in");
        account.refreshToken = params.get("refresh_token");
        account.username = params.get("name");

        return account;
    }

}
