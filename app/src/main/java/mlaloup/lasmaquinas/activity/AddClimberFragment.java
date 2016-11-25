package mlaloup.lasmaquinas.activity;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import static mlaloup.lasmaquinas.model.settings.RankingSettings.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import mlaloup.lasmaquinas.activity.util.PreferencesHelper;
import mlaloup.lasmaquinas.model.Ranking;
import mlaloup.lasmaquinas.model.settings.RankingSettings;
import mlaloup.lasmaquinas.parser.BleauInfoParser;

public class AddClimberFragment extends Fragment  {

    private static final String TAG = "AddClimberFragment";

    private ListView climberListView;

    private Button removeButton;

    private ArrayAdapter<String> climbersAdapter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.activity_add_climber, container, false);
        climberListView = (ListView) result.findViewById(R.id.climber_list);
        setHasOptionsMenu(true);


        updateUI();
        return result;
    }

    private void updateUI() {
        List<String> climberList = new ArrayList<>(getClimbers());

        if (climbersAdapter == null) {
            climbersAdapter = new ArrayAdapter<String>(getActivity(),
                    R.layout.item_climber,
                    R.id.climber_name,
                    climberList) {

                @NonNull
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View result = super.getView(position, convertView, parent);
                    Button button = (Button) result.findViewById(R.id.action_remove_climber);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            removeClimber(v);
                        }
                    });
                    return  result;

            }
            };
            climberListView.setAdapter(climbersAdapter);
        } else {
            climbersAdapter.clear();
            climbersAdapter.addAll(climberList);
            climbersAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu,menuInflater);
        menuInflater.inflate(R.menu.climbers_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add_climber:
                addClimber();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void addClimber() {
        final EditText climberEditText = new EditText(getActivity());
        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setTitle("Ajouter une machine !")
                .setMessage("Saisissez le login bleau.info")
                .setView(climberEditText)
                .setPositiveButton("Ajouter", null)
                .setNegativeButton("Annuler", null)
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(final DialogInterface dialog) {

                Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        String climber = String.valueOf(climberEditText.getText());
                        String userName = climber.trim();
                        if(climberAlreadyDefined(userName)){
                            displayAddClimberError("Machine déjà ajoutée !");
                            return;
                        }

                        boolean added = addClimberIfValid(userName);
                        if(!added){
                            displayAddClimberError("Login bleau.info invalide !");
                            return;
                        }
                        updateUI();

                        //Ferme la boite de dialogue
                        dialog.dismiss();
                    }

                    protected void displayAddClimberError(String message) {
                        Toast toast = Toast.makeText(getActivity().getApplicationContext(), message, Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            }
        });

        dialog.show();
    }

    /**
     * Ajoute le grimpeur, s'il possède un profil bleau.info. Renvoie faux si ce n'est pas le cas. Renvoie vrai si on est offline.
     * @param climber
     * @return
     */
    protected boolean addClimberIfValid(String climber) {
        AsyncTask<String, Void, Boolean> task = checkProfileExistsTask(climber);
        task.execute();
        boolean isValid = true;
        try {
            isValid = task.get(10, TimeUnit.SECONDS);
        } catch (Exception e){
            Log.e(TAG,"Error while checking profile. Took more than 10 seconds. Check internet connexion !");
            //On autorise la modification offline : on ne verifie pas.
        }
        if(!isValid){
            Log.d(TAG, "Unknown climber. it won't be added : " + climber);
            return false;
        }

        addClimber(climber);

        Log.d(TAG, "Climber added : " + climber);
        return true;
    }


    /**
     * Renvoi vrai si le grimpeur est déjà défini.
     * @param userName
     * @return
     */
    protected boolean climberAlreadyDefined(String userName) {
        Set<String> climbers = getClimbers();
        return climbers.contains(userName);
    }
    /**
     * Ajoute le nouveau grimpeur
     * @param userName
     */
    protected void addClimber(String userName) {
        Set<String> climbers = getClimbers();
        climbers.add(userName);
        prefs().edit().putStringSet(CLIMBERS_KEY,climbers).commit();
    }

    protected AsyncTask<String, Void, Boolean> checkProfileExistsTask(final String userName) {
        return new AsyncTask<String, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    boolean exists = new BleauInfoParser().checkProfileExists(userName);
                    return exists;
                } catch (Exception e) {
                    Log.e(TAG, "Error while checking profile. Check internet connexion !", e);
                    //On autorise la modification offline : on ne verifie pas.
                    return true;
                }
            }

            @Override
            protected void onPostExecute(Boolean valid) {

            }
        };
    }


    protected void removeClimber(View view) {
        View parent = (View) view.getParent();
        TextView climberTextView = (TextView) parent.findViewById(R.id.climber_name);
        String climber = String.valueOf(climberTextView.getText());
        removeClimber(climber);
        updateUI();
    }


    protected void removeClimber(String climber) {
        Set<String> climbers = getClimbers();
        climbers.remove(climber);
        prefs().edit().putStringSet(CLIMBERS_KEY,climbers).commit();
        Log.d(TAG, "Climber removed : " + climber);
    }

    @NonNull
    private Set<String> getClimbers() {
        return prefs().getStringSet(CLIMBERS_KEY, RankingSettings.DEFAULT_CLIMBERS);
    }

    private SharedPreferences prefs() {
        return PreferencesHelper.prefs(getActivity().getApplicationContext());
    }

}
