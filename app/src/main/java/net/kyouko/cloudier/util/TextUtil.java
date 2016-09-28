package net.kyouko.cloudier.util;

import android.app.Activity;
import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.view.View;

import net.kyouko.cloudier.ui.style.ClickableWithoutUnderlineSpan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Util class for manipulating texts.
 *
 * @author beta
 */
public class TextUtil {

    public static SpannableStringBuilder addLinkToUrlsInText(final Activity activity, String text,
                                                             final boolean clickable) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        return addLinkToUrlsInText(activity, builder, clickable);
    }


    public static SpannableStringBuilder addLinkToUrlsInText(final Activity activity,
                                                             SpannableStringBuilder builder,
                                                             final boolean clickable) {
        String regexUrl = "http://url\\.cn/[0-9a-zA-Z]{7}";

        Matcher urlMatcher = Pattern.compile(regexUrl).matcher(builder.toString());
        while (urlMatcher.find()) {
            final String url = builder.toString().substring(urlMatcher.start(), urlMatcher.end());

            ClickableWithoutUnderlineSpan linkSpan = new ClickableWithoutUnderlineSpan() {
                @Override
                public void onClick(View widget) {
                    if (clickable) {
                        UrlUtil.openUrl(activity, url);
                    }
                }
            };

            builder.setSpan(linkSpan, urlMatcher.start(), urlMatcher.end(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return builder;
    }


    public static SpannableStringBuilder addLinkToTopicsInText(final Context context, String text,
                                                               final boolean clickable) {
        SpannableStringBuilder builder = new SpannableStringBuilder(text);
        return addLinkToTopicsInText(context, builder, clickable);
    }


    public static SpannableStringBuilder addLinkToTopicsInText(final Context context,
                                                               SpannableStringBuilder builder,
                                                               final boolean clickable) {
        int topicStart = -1, topicEnd;
        for (int i = 0; i < builder.length(); i += 1) {
            if (builder.charAt(i) == '#') {
                if (topicStart < 0) {
                    topicStart = i;
                } else {
                    topicEnd = i + 1;

                    ClickableWithoutUnderlineSpan linkSpan = new ClickableWithoutUnderlineSpan() {
                        @Override
                        public void onClick(View widget) {
                            if (clickable) {
                                // TODO: view topic
                            }
                        }
                    };

                    builder.setSpan(linkSpan, topicStart, topicEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    topicStart = -1;
                }
            }
        }

        return builder;
    }

}
