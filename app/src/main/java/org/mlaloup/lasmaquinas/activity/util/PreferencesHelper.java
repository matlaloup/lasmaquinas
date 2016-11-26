package org.mlaloup.lasmaquinas.activity.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ApplicationTestCase;


public class PreferencesHelper {

    public static SharedPreferences prefs(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

}
