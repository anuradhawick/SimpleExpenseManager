package lk.ac.mrt.cse.dbs.simpleexpensemanager;

import android.app.Application;
import android.content.Context;

/**
 * Created by Anuradha Sanjeewa on 04/12/2015.
 */
public class ContextClass extends Application {
    private static Context context;

    @Override
    public void onCreate() {
        context = getApplicationContext();
    }

    public static Context getCustomAppContext() {
        return context;
    }
}
