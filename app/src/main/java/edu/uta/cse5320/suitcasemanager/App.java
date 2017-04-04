package edu.uta.cse5320.suitcasemanager;

import android.app.Application;

import edu.uta.cse5320.suitcasemanager.R;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Akshay on 4/4/2017.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("font/Comfortaa-Bold.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );
    }
}
