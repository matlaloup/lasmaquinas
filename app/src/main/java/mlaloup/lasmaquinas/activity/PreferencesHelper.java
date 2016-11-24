package mlaloup.lasmaquinas.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.test.ApplicationTestCase;

/**
 * Created by Matthieu on 24/11/2016.
 */

public class PreferencesHelper {

    public static SharedPreferences prefs(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

}
