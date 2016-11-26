package org.mlaloup.lasmaquinas.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.mlaloup.lasmaquinas.activity.util.PreferencesHelper;
import org.mlaloup.lasmaquinas.parser.BleauInfoParser;
import org.mlaloup.lasmaquinas.model.settings.RankingSettings;
import org.mlaloup.lasmaquinas.model.Ranking;
import org.mlaloup.lasmaquinas.model.TickList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String LAST_RANKING_KEY = "lastRanking";


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
        String lastRankingJson = prefs().getString(LAST_RANKING_KEY, null);
        Ranking lastRanking = null;
        if (lastRankingJson != null) {
            try {
                Gson gson = new Gson();
                lastRanking = gson.fromJson(lastRankingJson, Ranking.class);

                //Clean ticklist corrompue (maj de l'application)
                Iterator<TickList> iter = lastRanking.getTickLists().iterator();
                while (iter.hasNext()) {
                    TickList tickList = iter.next();
                    if (tickList.getClimber() == null) {
                        //unknown climber
                        iter.remove();
                        continue;
                    }
                    if (tickList.getClimber().getLogin() == null) {
                        //unknown climber
                        iter.remove();
                        continue;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Unable to  parse json ranking !", e);
            }
        }

        if(lastRanking != null){
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
        String jsonRanking = new Gson().toJson(ranking);
        //saves the new ranking
        prefs().edit().putString(LAST_RANKING_KEY, jsonRanking).commit();
    }


    private SharedPreferences prefs() {
        return PreferencesHelper.prefs(getApplicationContext());
    }

}
