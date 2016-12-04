package org.mlaloup.lasmaquinas.activity;


import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import org.mlaloup.lasmaquinas.activity.util.PreferencesHelper;
import org.mlaloup.lasmaquinas.activity.util.SeparatedListAdapter;
import org.mlaloup.lasmaquinas.model.Area;
import org.mlaloup.lasmaquinas.model.Boulder;
import org.mlaloup.lasmaquinas.model.Climber;
import org.mlaloup.lasmaquinas.parser.BleauInfoParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class ProjectsActivity extends AppCompatActivity {

    private static final String TAG = "ProjectsActivity";

    public static final String COOKIES_PROVIDED = "COOKIES_PROVIDED";

    public static final String PASSWORD_KEY = "password";

    public static final String LOGIN_KEY = "login";

    private String CLIMBER_KEY = "climber";

    private ListView projectList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        setContentView(R.layout.activity_projects);
        projectList = (ListView) findViewById(R.id.project_list);

        Climber climber = PreferencesHelper.privateSettings(this).getObject(CLIMBER_KEY, null, Climber.class);
        if (climber != null) {
            updateUI(climber);
        }
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
        SeparatedListAdapter adapter = new SeparatedListAdapter(this, R.layout.item_area);
        Set<Boulder> projects = climber.getProjects();

        Map<String, Set<Boulder>> projectsByArea = new TreeMap<>();
        for (Boulder project : projects) {
            Area area = project.getArea();
            String areaName = area.getName();
            Set<Boulder> boulders = projectsByArea.get(areaName);
            if (boulders == null) {
                boulders = new TreeSet<>();
                projectsByArea.put(areaName, boulders);
            }
            boulders.add(project);
        }

        for (Map.Entry<String, Set<Boulder>> entry : projectsByArea.entrySet()) {
            Set<Boulder> boulders = entry.getValue();
            final List<Boulder> boulderList = new ArrayList<>(boulders);
            String[] array = new String[boulders.size()];
            for (int i = 0; i < boulderList.size(); i++) {
                Boulder boulder = boulderList.get(i);
                array[i] = boulder.getName() + " | " + boulder.getGrade().getGrade();
            }
            adapter.addSection(entry.getKey(), new ArrayAdapter<String>(this,
                    R.layout.item_project, array) {

                @NonNull
                @Override
                public View getView(final int position, View convertView, ViewGroup parent) {
                    TextView result = (TextView) super.getView(position, convertView, parent);
                    result.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            Boulder boulder = boulderList.get(position);
                            String url = boulder.getUrl();
                            i.setData(Uri.parse(url));
                            startActivity(i);
                        }
                    });
                    return result;
                }
            });


        }

        projectList.setAdapter(adapter);

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
            Toast toast = Toast.makeText(ProjectsActivity.this, "Erreur de connexion !", Toast.LENGTH_SHORT);
            toast.show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            return;
        }

        String login = credentials[0];
        String password = credentials[1];
        AsyncTask<String, Void, Climber> task = retrieveClimberData(login, password);
        task.execute();
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
                    PreferencesHelper.privateSettings(ProjectsActivity.this).saveObject(CLIMBER_KEY, climber);
                    return climber;
                } catch (Exception e) {
                    Toast toast = Toast.makeText(ProjectsActivity.this, e.getMessage(), Toast.LENGTH_SHORT);
                    toast.show();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Climber climber) {
                updateUI(climber);
            }
        };
    }
}
