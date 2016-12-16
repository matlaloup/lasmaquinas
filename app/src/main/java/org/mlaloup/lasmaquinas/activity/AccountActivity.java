package org.mlaloup.lasmaquinas.activity;


import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.lang3.StringUtils;
import org.mlaloup.lasmaquinas.activity.tab.AccountTab;
import org.mlaloup.lasmaquinas.activity.tab.TabFragment;
import org.mlaloup.lasmaquinas.activity.util.PreferencesHelper;
import org.mlaloup.lasmaquinas.model.Climber;
import org.mlaloup.lasmaquinas.parser.BleauInfoParser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class AccountActivity extends AppCompatActivity {

    private static final String TAG = "AccountActivity";

    public static final String COOKIES_PROVIDED = "COOKIES_PROVIDED";

    public static final String LOGIN_KEY = "login";

    public static final String PASSWORD_KEY = "password";

    private String CLIMBER_KEY = "climber";

    private TabLayout tabLayout;

    private Map<AccountTab, TabFragment<Climber>> fragmentMap = new HashMap<>(AccountTab.values().length);

    private class TabAdapter extends FragmentPagerAdapter {

        public TabAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public android.support.v4.app.Fragment getItem(int position) {
            AccountTab tab = AccountTab.values()[position];
            TabFragment<Climber> result = tab.getFragment();
            fragmentMap.put(tab,result);
            return result;
        }

        @Override
        public int getCount() {
            return AccountTab.values().length;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            super.destroyItem(container, position, object);
            fragmentMap.remove(object);
            Log.i(TAG,"Destroying tab item "+object);
        }
    }

    private void addTabs() {
        AccountTab[] accountTabs = AccountTab.values();
        for (AccountTab accountTab : accountTabs) {
            TabLayout.Tab tab = tabLayout.newTab();
            tab.setTag(accountTab);
            tab.setText(accountTab.getTabName());
            tabLayout.addTab(tab);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        setupActionBar();
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        addTabs();

        setupViewPager();

        Climber climber = PreferencesHelper.privateSettings(this).getObject(CLIMBER_KEY, null, Climber.class);
        if (climber != null) {
            updateUI(climber);
        } else {
            //on force la mise à jour (permier affichage de l'écran)
            //le paramètre view n'est pas utilisé.
            synchronizeAccount(null);
        }

    }

    private void setupViewPager() {
        final ViewPager viewPager =
                (ViewPager) findViewById(R.id.pager);
        final PagerAdapter adapter = new TabAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(new
                TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }

        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            startActivity(new Intent(this, MainActivity.class));
            return true;
        }
        return false;
    }


    private void updateUI(Climber climber) {
        TextView accountTitle = (TextView)  findViewById(R.id.personal_account_title);
        String title = climber.getFullName();
        if(StringUtils.isNotBlank(climber.getCountryBigram())){
            title+=" ("+climber.getCountryBigram()+")";
        }
        accountTitle.setText(title);

        Collection<TabFragment<Climber>> fragmentsToUpdate = fragmentMap.values();
        for(TabFragment<Climber> fragment : fragmentsToUpdate){
            if(fragment != null){
                fragment.updateTabContent(climber);
            }
        }

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

    /**
     * Déclenché par le bouton de synchrnoisation du compte
     * @param view
     */
    public void synchronizeAccount(View view) {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            boolean cookiesProvided = extras.getBoolean(COOKIES_PROVIDED);
            if (cookiesProvided) {
                Set<String> keys = extras.keySet();
                Map<String, String> cookies = new HashMap<>();
                for (String key : keys) {
                    if (!COOKIES_PROVIDED.equals(key)) {
                        cookies.put(key, extras.getString(key));
                    }
                }
                //TODO : optimisation des cookies et gestion des erreurs.
            }
        }

        String[] credentials = PreferencesHelper.getCredentials(this);
        if (credentials == null) {
            Toast toast = Toast.makeText(AccountActivity.this, "Erreur de connexion !", Toast.LENGTH_SHORT);
            toast.show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return;
        }

        String login = credentials[0];
        String password = credentials[1];

        synchronizeClimberDataHandlingTimeout(login, password);
    }

    protected void synchronizeClimberDataHandlingTimeout(String login, String password) {
        final AsyncTask<String, Void, Climber> task = retrieveClimberData(login, password);
        task.execute();

        //permet d'annuler la tâche après 20 secondes d'exécution.
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run() {
                if ( task.getStatus() == AsyncTask.Status.RUNNING ){
                    task.cancel(true);

                }
            }
        }, 20000 );
    }

    @NonNull
    private AsyncTask<String, Void, Climber> retrieveClimberData(final String login, final String password) {
        return new AsyncTask<String, Void, Climber>() {
            @Override
            protected Climber doInBackground(String... params) {
                try {

                    BleauInfoParser parser = new BleauInfoParser();
                    Map<String, String> cookies = parser.logIn(login, password);
                    Climber climber = parser.parsePrivateProfile(cookies);
                    PreferencesHelper.privateSettings(AccountActivity.this).saveObject(CLIMBER_KEY, climber);
                    return climber;
                } catch (Exception e) {
                    Toast toast = Toast.makeText(AccountActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                    toast.show();
                    return null;
                }
            }

            @Override
            protected void onCancelled() {
                Toast toast = Toast.makeText(AccountActivity.this, "Impossible de synchroniser les données bleau.info. Vérifiez votre connexion internet.", Toast.LENGTH_SHORT);
                toast.show();
            }

            @Override
            protected void onPostExecute(Climber climber) {
                updateUI(climber);
            }
        };
    }
}
