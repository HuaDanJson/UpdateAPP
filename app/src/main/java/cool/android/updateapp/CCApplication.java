package cool.android.updateapp;

import android.app.Application;

public class CCApplication extends Application {

    private static final String TAG = CCApplication.class.getSimpleName();

    private static CCApplication INSTANCE;

    public static CCApplication getInstance() {
        return INSTANCE;
    }

    public CCApplication() {
        INSTANCE = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = this;

    }
}
