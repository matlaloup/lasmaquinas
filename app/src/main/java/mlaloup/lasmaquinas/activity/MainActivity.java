package mlaloup.lasmaquinas.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import mlaloup.lasmaquinas.activity.util.PreferencesHelper;
import mlaloup.lasmaquinas.model.BleauInfoParser;
import mlaloup.lasmaquinas.model.BleauRankSettings;
import mlaloup.lasmaquinas.model.Ranking;
import mlaloup.lasmaquinas.model.TickList;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String LAST_RANKING_KEY = "lastRanking";


    private ListView rankingList;


    /**
     * Appelé par clic sur le bouton "configure"
     *
     * @param view
     */
    public void configure(View view) {
        //TODO intent de configuration
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
            } catch (Exception e) {
                Log.e(TAG, "Unable to  parse json ranking !", e);
            }
        }

        updateUI(lastRanking);
    }


    private void updateUI(Ranking ranking) {
        TextView textView = (TextView) findViewById(R.id.ranking_title);
        if (ranking == null) {
            textView.setText(null);
            return;
        }


        SortedSet<TickList> ticklists = ranking.getTickLists();
        String rankingName = ranking.getName();
        textView.setText(rankingName);


        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        int rank = 1;

        for (TickList tickList : ticklists) {
            String user = tickList.getUser();
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
        AsyncTask<String, Void, Ranking> task = new AsyncTask<String, Void, Ranking>() {
            @Override
            protected Ranking doInBackground(String... params) {
                try {
                    BleauRankSettings settings = BleauRankSettings.loadFromPreferences(MainActivity.this);

                    BleauInfoParser parser = new BleauInfoParser();
                    Ranking ranking = parser.parseTickLists(settings);
                    saveRankings(ranking);
                    return ranking;
                } catch (Exception e) {
                    Log.e(TAG, "Error while parsing ranking !", e);
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Ranking ranking) {
                updateUI(ranking);
            }
        };

        AsyncTask<String, Void, Ranking> async = task.execute();
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
