package mlaloup.lasmaquinas.activity;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class AddClimberActivity extends AppCompatActivity {

    private static final String TAG = "AddClimberActivity";

    private static final String CLIMBERS_KEY = "climbers";

    private ListView climberListView;

    private ArrayAdapter climbersAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_climber);
        climberListView = (ListView) findViewById(R.id.list_climbers);
        updateUI();
    }

    private void updateUI() {
        List<String> climberList = new ArrayList<>(getClimbers());

        if (climbersAdapter == null) {
            climbersAdapter = new ArrayAdapter<>(this,
                    R.layout.item_climber,
                    R.id.climber_name,
                    climberList);
            climberListView.setAdapter(climbersAdapter);
        } else {
            climbersAdapter.clear();
            climbersAdapter.addAll(climberList);
            climbersAdapter.notifyDataSetChanged();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.climbers_menu, menu);
        return super.onCreateOptionsMenu(menu);
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
        final EditText climberEditText = new EditText(this);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Add a new climber")
                .setMessage("Type bleau.info login name")
                .setView(climberEditText)
                .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String climber = String.valueOf(climberEditText.getText());
                        addClimber(climber);
                        updateUI();
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }

    protected void addClimber(String climber) {
        Set<String> climbers = getClimbers();
        //TODO : check that the climber has a bleau info profile.
        climbers.add(climber);
        settings().edit().putStringSet(CLIMBERS_KEY,climbers).commit();
        Log.d(TAG, "Climber added : " + climber);
    }

    public void removeClimber(View view) {
        View parent = (View) view.getParent();
        TextView climberTextView = (TextView) parent.findViewById(R.id.climber_name);
        String climber = String.valueOf(climberTextView.getText());
        removeClimber(climber);
        updateUI();
    }

    protected void removeClimber(String climber) {
        Set<String> climbers = getClimbers();
        climbers.remove(climber);
        settings().edit().putStringSet(CLIMBERS_KEY,climbers).commit();
        Log.d(TAG, "Climber removed : " + climber);
    }

    @NonNull
    private Set<String> getClimbers() {
        return settings().getStringSet(CLIMBERS_KEY, new LinkedHashSet<String>());
    }

    private SharedPreferences settings(){
        return getPreferences(MODE_PRIVATE);
    }
}
