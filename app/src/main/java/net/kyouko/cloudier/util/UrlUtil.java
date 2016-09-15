package net.kyouko.cloudier.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Util class for parsing and generating URLs.
 *
 * @author beta
 */
public class UrlUtil {

    public static String getValueFromUrl(String url, String key) {
        if (!url.endsWith("&")) {
            url += "&";
        }

        int position = url.indexOf(key);
        if (position < 0) {
            return null;
        }
        int positionStart = url.indexOf("=", position) + 1;
        int positionEnd = url.indexOf("&", position);

        String value = null;
        try {
            // Decode the url.
            value = URLDecoder.decode(url.substring(positionStart, positionEnd), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            // Ignore.
        }

        return value;
    }


    public static Map<String, String> getParamsFromUrl(String url) {
        Map<String, String> params = new HashMap<>();

        if (url.contains("?")) {
            String paramPart = url.substring(url.indexOf("?") + 1);
            String[] paramStrings = paramPart.split("&");
            for (String paramString : paramStrings) {
                int equalSignIndex = paramString.indexOf("=");
                String key = paramString.substring(0, equalSignIndex);
                String value = paramString.substring(equalSignIndex + 1);

                params.put(key, value);
            }
        }

        return params;
    }


    public static void openUrl(Context context, String url) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(browserIntent);
    }

}
