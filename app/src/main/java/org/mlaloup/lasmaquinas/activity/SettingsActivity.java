package org.mlaloup.lasmaquinas.activity;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.support.v7.app.ActionBar;
import android.preference.PreferenceFragment;
import android.view.MenuItem;
import android.widget.NumberPicker;
import static org.mlaloup.lasmaquinas.model.settings.TickListSettings.*;
import static org.mlaloup.lasmaquinas.model.settings.RankingSettings.*;


import java.util.List;

import org.mlaloup.lasmaquinas.activity.util.AppCompatPreferenceActivity;
import org.mlaloup.lasmaquinas.activity.util.PreferencesHelper;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {


    private boolean inFragment;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
                int id = item.getItemId();
        if (id == android.R.id.home) {
            Class<? extends Activity> target = inFragment ? SettingsActivity.class : MainActivity.class;
            startActivity(new Intent(this, target));
            return true;
        }
        return false;
    }

    @Override
    public void onAttachFragment(Fragment fragment) {
        super.onAttachFragment(fragment);
        inFragment = true;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        inFragment = false;
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }


    /** {@inheritDoc} */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
        & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    /** {@inheritDoc} */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
        for (Header h : target) {
            if (h.id == R.id.month_duration) {
                h.summary = String.valueOf(prefs().getInt(MONTH_DURATION_KEY, DEFAULT_MONTH_DURATION));
            }
            if (h.id == R.id.max_ascents) {
                h.summary = String.valueOf(prefs().getInt(MAX_ASCENTS_COUNT_KEY,DEFAULT_MAX_ASCENTS_COUNT));
            }
        }
    }

    private SharedPreferences prefs(){
        return PreferencesHelper.prefs(getApplicationContext());
    }


    @Override
    public void onHeaderClick(Header header, int position) {
        if (header.id == R.id.month_duration) {
            addNumberPickerDialog("Nombre de mois glissants :",MONTH_DURATION_KEY,DEFAULT_MONTH_DURATION,120);
        }
        if (header.id == R.id.max_ascents) {
            addNumberPickerDialog("Nombre max de blocs :",MAX_ASCENTS_COUNT_KEY,DEFAULT_MAX_ASCENTS_COUNT,200);
        }
        super.onHeaderClick(header, position);
    }


   protected void addNumberPickerDialog(String title, final String prefsKey, int defaultValue, int maxValue ){
       AlertDialog.Builder alert = new AlertDialog.Builder(this);

       alert.setTitle(title);
       final NumberPicker np = new NumberPicker(this);
       np.setMinValue(1);
       np.setMaxValue(maxValue);
       np.setWrapSelectorWheel(false);
       int currentValue = prefs().getInt(prefsKey,defaultValue);
       np.setValue(currentValue);

       alert.setView(np);

       alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int whichButton) {
               //indispensable pour faire fonctionner la saisie clavier
               np.clearFocus();

               //sauvegarde de la valeur.
               prefs().edit().putInt(prefsKey,np.getValue()).commit();

               // Trigger the summary text to be updated.
               invalidateHeaders();
           }
       });

       alert.setNegativeButton("Annuler", null);

       alert.show();
   }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    @Override
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || AddClimberFragment.class.getName().equals(fragmentName);
    }




}
