package net.kyouko.cloudier.util;

import android.content.Context;

/**
 * Util class for generating URLs for images and avatars.
 *
 * @author beta
 */
public class ImageUtil {

    public final static int QUALITY_ORIGINAL = 0;
    public final static int QUALITY_HIGH = 1;
    public final static int QUALITY_MEDIUM = 2;
    public final static int QUALITY_LOW = 3;

    public final static String PREFIX_APP = "app";
    public final static String[] PREFIXES = {"t0", "t1", "t2", "t3", "t4", "t5", "t6", "t7", "t8", "t9"};

    private final static String SUFFIX_AVATAR_ORIGINAL = "0";
    private final static String SUFFIX_AVATAR_HIGH = "180";
    private final static String SUFFIX_AVATAR_MEDIUM = "100";
    private final static String SUFFIX_AVATAR_LOW = "50";

    private final static String SUFFIX_IMAGE_ORIGINAL = "2000";
    private final static String SUFFIX_IMAGE_HIGH = "500";
    private final static String SUFFIX_IMAGE_MEDIUM = "320";
    private final static String SUFFIX_IMAGE_LOW = "120";


    private int imageQualityOverCellular;
    private int imageQualityOverWifi;
    private String prefix;


    private ImageUtil(Context context) {
        PreferenceUtil pref = PreferenceUtil.with(context);
        imageQualityOverCellular = pref.getInt(PreferenceUtil.PREF_IMAGE_QUALITY_OVER_CELLULAR);
        imageQualityOverWifi = pref.getInt(PreferenceUtil.PREF_IMAGE_QUALITY_OVER_WIFI);
        prefix = pref.getString(PreferenceUtil.PREF_IMAGE_SOURCE);
    }


    public static ImageUtil getInstance(Context context) {
        return new ImageUtil(context);
    }


    public String parseImageUrl(String originalUrl) {
        // TODO: WiFi or cellular?
        return parseImageUrl(originalUrl, imageQualityOverWifi);
    }


    public String parseImageUrl(String originalUrl, int quality) {
        int indexOfQpic = originalUrl.indexOf("qpic.cn");
        if (indexOfQpic >= 0) {
            String imageUrl = "http://" + prefix + "." + originalUrl.substring(indexOfQpic);

            if (!imageUrl.endsWith("/")) {
                imageUrl += "/";
            }
            switch (quality) {
                case QUALITY_ORIGINAL:
                default:
                    imageUrl += SUFFIX_IMAGE_ORIGINAL;
                    break;
                case QUALITY_HIGH:
                    imageUrl += SUFFIX_IMAGE_HIGH;
                    break;
                case QUALITY_MEDIUM:
                    imageUrl += SUFFIX_IMAGE_MEDIUM;
                    break;
                case QUALITY_LOW:
                    imageUrl += SUFFIX_IMAGE_LOW;
                    break;
            }

            return imageUrl;
        }

        int indexOfQlogo = originalUrl.indexOf("qlogo.cn");
        if (indexOfQlogo >= 0) {
            String imageUrl = "http://" + prefix + "." + originalUrl.substring(indexOfQlogo);

            if (!imageUrl.endsWith("/")) {
                imageUrl += "/";
            }
            switch (quality) {
                case QUALITY_ORIGINAL:
                default:
                    imageUrl += SUFFIX_AVATAR_ORIGINAL;
                    break;
                case QUALITY_HIGH:
                    imageUrl += SUFFIX_AVATAR_HIGH;
                    break;
                case QUALITY_MEDIUM:
                    imageUrl += SUFFIX_AVATAR_MEDIUM;
                    break;
                case QUALITY_LOW:
                    imageUrl += SUFFIX_AVATAR_LOW;
                    break;
            }

            return imageUrl;
        }

        return originalUrl;
    }

}
