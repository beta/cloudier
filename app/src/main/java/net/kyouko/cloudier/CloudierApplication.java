package net.kyouko.cloudier;

import android.app.Application;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.squareup.otto.Bus;

import net.kyouko.cloudier.util.ImageUtil;
import net.kyouko.cloudier.util.PreferenceUtil;

/**
 * Entry class.
 *
 * @author beta
 */
public class CloudierApplication extends Application {

    private static Bus bus;


    @Override
    public void onCreate() {
        super.onCreate();

        Fresco.initialize(this);

        if (!hasInitialSetup()) {
            performInitialSetup();
        }
    }


    public static Bus getBus() {
        if (bus == null) {
            synchronized (CloudierApplication.class) {
                if (bus == null) {
                    bus = new Bus();
                }
            }
        }

        return bus;
    }


    private boolean hasInitialSetup() {
        return PreferenceUtil.with(this).getBoolean(PreferenceUtil.PREF_APP_HAS_INITIAL_SETUP);
    }


    private void performInitialSetup() {
        PreferenceUtil.with(this).edit()
                .set(PreferenceUtil.PREF_IMAGE_QUALITY_OVER_CELLULAR,
                        ImageUtil.QUALITY_LOW)
                .set(PreferenceUtil.PREF_IMAGE_QUALITY_OVER_WIFI,
                        ImageUtil.QUALITY_HIGH)
                .set(PreferenceUtil.PREF_IMAGE_SOURCE,
                        ImageUtil.PREFIX_APP)
                .set(PreferenceUtil.PREF_APP_HAS_INITIAL_SETUP, true)
                .save();
    }

}
