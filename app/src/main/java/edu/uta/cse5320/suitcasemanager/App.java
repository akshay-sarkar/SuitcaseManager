package edu.uta.cse5320.suitcasemanager;

import android.app.Application;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
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
