package net.kyouko.cloudier.util;

import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Util class for converting usernames to nicknames.
 *
 * @author beta
 */
public class NicknameUtil {

    public static SpannableStringBuilder replaceUsernameWithNicknameInContent(
            String originalContent, HashMap<String, String> users) {
        SpannableStringBuilder builder = new SpannableStringBuilder(originalContent);
        return replaceUsernameWithNicknameInContent(builder, users);
    }


    public static SpannableStringBuilder replaceUsernameWithNicknameInContent(
            SpannableStringBuilder originalContent, HashMap<String, String> users) {
        for (LinkedHashMap.Entry<String, String> entry : users.entrySet()) {
            if (entry.getValue().length() == 0) {
                continue;
            }

            int lastPosition = 0;
            int start = originalContent.toString().toLowerCase()
                    .indexOf("@" + entry.getKey().toLowerCase(), lastPosition);
            while (start >= 0) {
                int end = start + entry.getKey().length() + 1;
                originalContent.replace(start, end, entry.getValue());

                int spanStart = start;
                int spanEnd = spanStart + entry.getValue().length();
                originalContent.setSpan(new StyleSpan(Typeface.BOLD), spanStart, spanEnd, 0);

                lastPosition = spanEnd;
                start = originalContent.toString().toLowerCase()
                        .indexOf("@" + entry.getKey().toLowerCase(), lastPosition);
            }
        }

        return originalContent;
    }

}
