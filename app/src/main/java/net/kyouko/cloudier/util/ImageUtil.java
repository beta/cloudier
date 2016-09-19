package net.kyouko.cloudier.util;

import android.content.Context;

/**
 * Util class for generating URLs for images and avatars.
 *
 * @author beta
 */
public class ImageUtil {

    public final static int QUALITY_LOW = 0;
    public final static int QUALITY_MEDIUM = 1;
    public final static int QUALITY_HIGH = 2;
    public final static int QUALITY_ORIGINAL = 3;

    public final static String IMAGE_QUALITY_LOW = "120";
    public final static String IMAGE_QUALITY_MEDIUM = "320";
    public final static String IMAGE_QUALITY_HIGH = "500";
    public final static String IMAGE_QUALITY_ORIGINAL = "2000";

    public final static String AVATAR_QUALITY_LOW = "50";
    public final static String AVATAR_QUALITY_MEDIUM = "100";
    public final static String AVATAR_QUALITY_HIGH = "180";
    public final static String AVATAR_QUALITY_ORIGINAL = "0";


    private String imageSource;
    private String imageQuality;
    private String avatarQuality;


    private ImageUtil(Context context) {
        PreferenceUtil pref = PreferenceUtil.with(context);
        imageSource = pref.getString(PreferenceUtil.PREF_IMAGE_SOURCE);
        imageQuality = pref.getString(PreferenceUtil.PREF_IMAGE_QUALITY);
        avatarQuality = pref.getString(PreferenceUtil.PREF_AVATAR_QUALITY);
    }


    public static ImageUtil getInstance(Context context) {
        return new ImageUtil(context);
    }


    public String parseImageUrl(String originalUrl) {
        return parseImageUrl(originalUrl, imageQuality, avatarQuality);
    }


    public String parseImageUrl(String originalUrl, int quality) {
        switch (quality) {
            case QUALITY_LOW:
                return parseImageUrl(originalUrl, IMAGE_QUALITY_LOW, AVATAR_QUALITY_LOW);
            case QUALITY_MEDIUM:
                return parseImageUrl(originalUrl, IMAGE_QUALITY_MEDIUM, AVATAR_QUALITY_MEDIUM);
            case QUALITY_HIGH:
                return parseImageUrl(originalUrl, IMAGE_QUALITY_HIGH, AVATAR_QUALITY_HIGH);
            case QUALITY_ORIGINAL:
            default:
                return parseImageUrl(originalUrl, IMAGE_QUALITY_ORIGINAL, AVATAR_QUALITY_ORIGINAL);
        }
    }


    public String parseImageUrl(String originalUrl, String imageQuality, String avatarQuality) {
        int indexOfQpic = originalUrl.indexOf("qpic.cn");
        if (indexOfQpic >= 0) {
            String imageUrl = "http://" + imageSource + "." + originalUrl.substring(indexOfQpic);
            if (!imageUrl.endsWith("/")) {
                imageUrl += "/";
            }
            imageUrl += imageQuality;
            return imageUrl;
        }

        int indexOfQlogo = originalUrl.indexOf("qlogo.cn");
        if (indexOfQlogo >= 0) {
            String avatarUrl = "http://" + imageSource + "." + originalUrl.substring(indexOfQlogo);
            if (!avatarUrl.endsWith("/")) {
                avatarUrl += "/";
            }
            avatarUrl += avatarQuality;
            return avatarUrl;
        }

        return originalUrl;
    }

}
