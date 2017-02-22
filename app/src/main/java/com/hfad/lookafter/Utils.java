package com.hfad.lookafter;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.Toast;
import android.app.Activity;


public class Utils {

    private static int theme;
    public final static int LIGHT_THEME = 1;
    public final static int DARK_THEME = 2;
    /**
     * Set the theme of the Activity, and restart it by creating a new Activity of the same type.
     */
    public static void changeToTheme(Activity activity, int theme)
    {
        switch (theme)
        {
            default:
                activity.setTheme(R.style.LightTheme);
                break;
            case LIGHT_THEME:
                activity.setTheme(R.style.LightTheme);
                break;
            case DARK_THEME:
                activity.setTheme(R.style.DarkTheme);
                break;
        }
//        Utils.theme = theme;
//        activity.finish();
//        activity.startActivity(new Intent(activity, activity.getClass()));
    }

    public static AppTheme getApplicationTheme(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        String theme = sharedPref.getString(context.getString(R.string.style_preference), "DarkTheme");

        return AppTheme.valueOf(theme);
    }
    /** Set the theme of the activity, according to the configuration. */
    public static void onActivityCreateSetTheme(Activity activity)
    {

    }

}
