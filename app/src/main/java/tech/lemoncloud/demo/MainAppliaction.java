package tech.lemoncloud.demo;

import android.app.Application;
import android.content.Context;

/**
 * Created by allen on 2017/12/6.
 */

public class MainAppliaction extends Application {

    private static final String TAG = MainAppliaction.class.getSimpleName();

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;

    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }


}
