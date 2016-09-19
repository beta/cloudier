package net.kyouko.cloudier;

import android.app.Application;
import android.preference.PreferenceManager;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.squareup.otto.Bus;

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

        performInitialSetup();
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


    private void performInitialSetup() {
        PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
    }

}
