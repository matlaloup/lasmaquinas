package mlaloup.lasmaquinas.activity;

import android.app.Fragment;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mlaloup.lasmaquinas.activity.util.PreferencesHelper;
import mlaloup.lasmaquinas.model.BleauRankSettings;

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
                .setPositiveButton("Ajouter", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String climber = String.valueOf(climberEditText.getText());
                        addClimber(climber);
                        updateUI();
                    }
                })
                .setNegativeButton("Annuler", null)
                .create();
        dialog.show();
    }

    protected void addClimber(String climber) {
        Set<String> climbers = getClimbers();
        //TODO : check that the climber has a bleau info profile.
        climbers.add(climber);
        prefs().edit().putStringSet(SettingsActivity.CLIMBERS_KEY,climbers).commit();
        Log.d(TAG, "Climber added : " + climber);
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
        prefs().edit().putStringSet(SettingsActivity.CLIMBERS_KEY,climbers).commit();
        Log.d(TAG, "Climber removed : " + climber);
    }

    @NonNull
    private Set<String> getClimbers() {
        return prefs().getStringSet(SettingsActivity.CLIMBERS_KEY, BleauRankSettings.load(getResources()).getUsers());
    }

    private SharedPreferences prefs() {
        return PreferencesHelper.prefs(getActivity().getApplicationContext());
    }

}
