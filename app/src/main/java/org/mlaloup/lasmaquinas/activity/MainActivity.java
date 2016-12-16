package org.mlaloup.lasmaquinas.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.concurrent.TimeUnit;

import org.mlaloup.lasmaquinas.activity.util.EncryptionHelper;
import org.mlaloup.lasmaquinas.activity.util.PreferencesHelper;
import org.mlaloup.lasmaquinas.parser.BleauInfoParser;
import org.mlaloup.lasmaquinas.model.settings.RankingSettings;
import org.mlaloup.lasmaquinas.model.Ranking;
import org.mlaloup.lasmaquinas.model.TickList;
import org.mlaloup.lasmaquinas.parser.CredentialsException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    public static final String LAST_RANKING_KEY = "lastRanking";

    public static final String FORCE_UPDATE = "forceUpdateIfRequested";


    private ListView rankingList;


    /**
     * Appelé par clic sur le bouton "paramètres"
     *
     * @param view
     */
    public void configure(View view) {
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivity(settingsIntent);
    }

    /**
     * Appelé par clic sur le bouton "actualiser"
     *
     * @param view
     */
    public void update(View view) {
        updateRankingsFromBleauInfo();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();


        setContentView(R.layout.activity_main);
        rankingList = (ListView) findViewById(R.id.last_ranking);

        loadLastRanking();

        forceUpdateIfRequested();
    }

    /**
     * Mets a jour le classement si le paramètre FORCE UPDATE est fourni.
     */
    protected void forceUpdateIfRequested() {
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            Object forceUpdate = getIntent().getExtras().get(FORCE_UPDATE);
            if (forceUpdate != null) {
                updateRankingsFromBleauInfo();
            }
        }
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
            actionBar.setIcon(R.drawable.muscle_toolbar_32);
        }
    }

    private void loadLastRanking() {
        Ranking lastRanking = PreferencesHelper.globalSettings(this).getObject(LAST_RANKING_KEY,null,Ranking.class);

        if (lastRanking != null) {
            updateUI(lastRanking);
        }
    }


    private void updateUI(Ranking ranking) {
        TextView textView = (TextView) findViewById(R.id.ranking_title);
        if (ranking == null) {
            Toast toast = Toast.makeText(this, "Impossible d'actualiser le classement. Vérifiez la connexion internet !", Toast.LENGTH_SHORT);
            toast.show();
            return;
        }

        SortedSet<TickList> ticklists = ranking.getTickLists();
        String rankingName = ranking.getName();
        textView.setText(rankingName);


        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        int rank = 1;

        for (TickList tickList : ticklists) {
            String user = tickList.getClimber().getDisplayName();
            int score = tickList.getScore();
            Map<String, String> datum = new HashMap<>(2);
            datum.put("ranking", rank + " : " + user + " | " + score + " points");
            datum.put("details", tickList.getSummary());
            data.add(datum);
            rank++;

        }

        SimpleAdapter adapter = new SimpleAdapter(this, data,
                R.layout.item_ranking,
                new String[]{"ranking", "details"},
                new int[]{R.id.climber_name,
                        R.id.ticklist_details});
        rankingList.setAdapter(adapter);


    }


    protected void updateRankingsFromBleauInfo() {
        AsyncTask<String, Void, Ranking> task = retrieveRankingAsyncTask();
        task.execute();
    }

    @NonNull
    private AsyncTask<String, Void, Ranking> retrieveRankingAsyncTask() {
        return new AsyncTask<String, Void, Ranking>() {
            @Override
            protected Ranking doInBackground(String... params) {
                try {
                    RankingSettings settings = RankingSettings.loadFromPreferences(MainActivity.this);

                    BleauInfoParser parser = new BleauInfoParser();
                    Ranking ranking = parser.parseTickLists(settings);
                    saveRankings(ranking);
                    return ranking;
                } catch (Exception e) {
                    Log.e(TAG, "Error while parsing ranking ! Check internet connexion !", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Ranking ranking) {
                updateUI(ranking);
            }
        };
    }

    protected void saveRankings(Ranking ranking) {
        PreferencesHelper.globalSettings(this).saveObject(LAST_RANKING_KEY,ranking);
    }

    /**
     * Appelé par le bouton "Mon compte".
     * @param view
     */
    public void openAccount(View view) {
        SharedPreferences globalSettings = PreferencesHelper.globalSettings(this).get();
        String[] credentials = PreferencesHelper.getCredentials(this);
        if(credentials !=null){
            openAccountActivity(null);
        } else {
            openLoginDialog();
        }

    }

    protected void openAccountActivity(Map<String, String> cookies){
        Intent accountIntent = new Intent(this, AccountActivity.class);
        if(cookies != null) {
            accountIntent.putExtra(AccountActivity.COOKIES_PROVIDED, true);
            for (String key : cookies.keySet()) {
                accountIntent.putExtra(key, cookies.get(key));
            }
        }
        startActivity(accountIntent);
    }

    protected void openLoginDialog() {
        LayoutInflater inflater = (LayoutInflater)getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View loginView = inflater.inflate(R.layout.login, null);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Se connecter")
                .setView(loginView)
                .setPositiveButton("OK", null)
                .setNegativeButton("Annuler", null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                final Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        EditText loginInput = (EditText) loginView.findViewById(R.id.input_login);
                        String userName = String.valueOf(loginInput.getText()).trim();

                        EditText passwordInput = (EditText) loginView.findViewById(R.id.input_password);
                        String password = String.valueOf(passwordInput.getText());
                        try {
                            Map<String, String> cookies = loginBleauInfo(userName, password);

                            //Sauvegarde du login et mot de passe.
                            String something = EncryptionHelper.doSomething(userName);
                            String encryptedPassword = EncryptionHelper.encrypt(password,something);
                            SharedPreferences globalSettings = PreferencesHelper.globalSettings(MainActivity.this).get();
                            globalSettings.edit().putString(AccountActivity.LOGIN_KEY,userName).commit();
                            globalSettings.edit().putString(AccountActivity.PASSWORD_KEY,encryptedPassword).commit();

                            openAccountActivity(cookies);

                            //Ferme la boite de dialogue
                            dialog.dismiss();
                        } catch (CredentialsException e) {
                            displayLoginError(e.getMessage());
                            return;
                        } catch (Exception e) {
                            Log.w(TAG, "Cannot log in to bleau.info " + e.getMessage());
                            displayLoginError("Impossible de se connecter à bleau.info. Vérifiez votre connexion internet.");
                            return;

                        }
                    }
                    protected void displayLoginError(String message) {
                        Toast toast = Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }
        });

        dialog.show();

}

    protected Map<String,String> loginBleauInfo(String userName, String password) throws Exception {
        AsyncTask<String, Void, Object> task = createLogInBleauInfoTask(userName,password);
        try {
            task.execute();
            Object object = task.get(10, TimeUnit.SECONDS);
            if(object instanceof Exception){
                throw (Exception)object;
            }
            return  (Map<String,String>) object;
        } catch (Exception e){
            throw e;
        }
    }

    /**
     * Se connecte à Bleau.info. Renvoie la map de cookies, si la connection est correcte. Une exception si celle-ci n'a pas réussi.
     * @param userName
     * @param password
     * @return
     */
    protected AsyncTask<String, Void, Object> createLogInBleauInfoTask(final String userName, final String password) {
        return new AsyncTask<String, Void, Object>() {
            @Override
            protected Object doInBackground(String... params) {
                try {
                    Map<String, String> cookies = new BleauInfoParser().logIn(userName,password);
                    return cookies;
                } catch (Exception e) {
                    Log.e(TAG, "Error while checking profile. Check internet connexion !", e);
                    //On autorise la modification offline : on ne verifie pas.
                    return e;
                }
            }

        };
    }


}
