package net.kyouko.cloudier.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Util class for reading and writing preferences.
 *
 * @author beta
 */
public class PreferenceUtil {

    public final static String PREF_ACCOUNT_ACCESS_TOKEN = "account.access_token";
    public final static String PREF_ACCOUNT_EXPIRES_IN = "account.expires_in";
    public final static String PREF_ACCOUNT_REFRESH_TOKEN = "account.refresh_token";
    public final static String PREF_ACCOUNT_USERNAME = "account.username";
    public final static String PREF_ACCOUNT_NICKNAME = "account.nickname";
    public final static String PREF_ACCOUNT_AVATAR_URL = "account.avatar_url";
    public final static String PREF_ACCOUNT_OPEN_ID = "account.open_id";

    public final static String PREF_HAS_AUTHORIZED = "has_authorized";

    public final static String PREF_IMAGE_SOURCE = "image.source";
    public final static String PREF_IMAGE_QUALITY = "image.quality";
    public final static String PREF_AVATAR_QUALITY = "avatar.quality";


    private SharedPreferences preferences;


    private PreferenceUtil(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }


    /**
     * Creates a new instance of with the given {@link Context}.
     *
     * @param context {@link Context} to get shared preferences.
     * @return an instance of this util class.
     */
    public static PreferenceUtil with(Context context) {
        return new PreferenceUtil(context);
    }


    /**
     * Reads an integer value from the preferences with the given key.
     * If the preference cannot be found, 0 is returned.
     *
     * @param key key of the preference to be read.
     * @return an integer value or 0.
     */
    public int getInt(String key) {
        return getInt(key, 0);
    }


    /**
     * Reads an integer value from the preferences with the given key.
     * If the preference cannot be found, {@code defaultValue} is returned.
     *
     * @param key          key of the preference to be read.
     * @param defaultValue default value to be returned if the preference fails to be read.
     * @return an integer value or {@code defaultValue}.
     */
    public int getInt(String key, int defaultValue) {
        return preferences.getInt(key, defaultValue);
    }


    /**
     * Reads a boolean value from the preferences with the given key.
     * If the preference cannot be found, {@code false} is returned.
     *
     * @param key key of the preference to be read.
     * @return a boolean value or {@code false}.
     */
    public boolean getBoolean(String key) {
        return getBoolean(key, false);
    }


    /**
     * Reads a boolean value from the preferences with the given key.
     * If the preference cannot be found, {@code defaultValue} is returned.
     *
     * @param key          key of the preference to be read.
     * @param defaultValue default value to be returned if the preference fails to be read.
     * @return a boolean value or {@code defaultValue}.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        return preferences.getBoolean(key, defaultValue);
    }


    /**
     * Reads a string value from the preferences with the given key.
     * If the preference cannot be found, an empty string is returned.
     *
     * @param key key of the preference to be read.
     * @return a string value or an empty string.
     */
    public String getString(String key) {
        return getString(key, "");
    }


    /**
     * Reads a string value from the preferences with the given key.
     * If the preference cannot be found, {@code defaultValue} is returned.
     *
     * @param key          key of the preference to be read.
     * @param defaultValue default value to be returned if the preference fails to be read.
     * @return a string value or {@code defaultValue}.
     */
    public String getString(String key, String defaultValue) {
        return preferences.getString(key, defaultValue);
    }


    /**
     * Returns a {@link PreferenceEditor} for editing preferences.
     *
     * @return a {@link PreferenceEditor} for editing preferences.
     */
    public PreferenceEditor edit() {
        return new PreferenceEditor(preferences.edit());
    }


    /**
     * Sets an integer value into the preferences.
     *
     * @param key   key of the preference.
     * @param value value to be set.
     * @return a {@link PreferenceEditor} for continuous use.
     */
    public PreferenceEditor set(String key, int value) {
        return new PreferenceEditor(preferences.edit()).set(key, value);
    }


    /**
     * Sets a boolean value into the preferences.
     *
     * @param key   key of the preference.
     * @param value value to be set.
     * @return a {@link PreferenceEditor} for continuous use.
     */
    public PreferenceEditor set(String key, boolean value) {
        return new PreferenceEditor(preferences.edit()).set(key, value);
    }


    /**
     * Sets a string value into the preferences.
     *
     * @param key   key of the preference.
     * @param value value to be set.
     * @return a {@link PreferenceEditor} for continuous use.
     */
    public PreferenceEditor set(String key, String value) {
        return new PreferenceEditor(preferences.edit()).set(key, value);
    }


    /**
     * Editor for writing and deleting preferences.
     */
    public class PreferenceEditor {

        private SharedPreferences.Editor editor;


        private PreferenceEditor(SharedPreferences.Editor editor) {
            this.editor = editor;
        }


        /**
         * Sets an integer value into the preferences.
         *
         * @param key   key of the preference.
         * @param value value to be set.
         * @return the current {@link PreferenceEditor} for continuous use.
         */
        public PreferenceEditor set(String key, int value) {
            editor.putInt(key, value);
            return this;
        }


        /**
         * Sets a boolean value into the preferences.
         *
         * @param key   key of the preference.
         * @param value value to be set.
         * @return the current {@link PreferenceEditor} for continuous use.
         */
        public PreferenceEditor set(String key, boolean value) {
            editor.putBoolean(key, value);
            return this;
        }


        /**
         * Sets a string value into the preferences.
         *
         * @param key   key of the preference.
         * @param value value to be set.
         * @return the current {@link PreferenceEditor} for continuous use.
         */
        public PreferenceEditor set(String key, String value) {
            editor.putString(key, value);
            return this;
        }


        /**
         * Deletes a preference with the given {@code key}.
         *
         * @param key key of the preference to be deleted.
         * @return the current {@link PreferenceEditor} for continuous use.
         */
        public PreferenceEditor delete(String key) {
            editor.remove(key);
            return this;
        }


        /**
         * Saves all the changes to the preferences.
         */
        public void save() {
            editor.apply();
        }

    }

}
