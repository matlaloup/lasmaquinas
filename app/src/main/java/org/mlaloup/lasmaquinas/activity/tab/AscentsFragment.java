package org.mlaloup.lasmaquinas.activity.tab;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.mlaloup.lasmaquinas.activity.R;
import org.mlaloup.lasmaquinas.activity.util.PreferencesHelper;
import org.mlaloup.lasmaquinas.activity.util.SeparatedListAdapter;
import org.mlaloup.lasmaquinas.model.Ascent;
import org.mlaloup.lasmaquinas.model.Boulder;
import org.mlaloup.lasmaquinas.model.Climber;
import org.mlaloup.lasmaquinas.model.TickList;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AscentsFragment extends TabFragment<Climber> {

    private String CLIMBER_KEY = "climber";

    private ListView ascentList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.activity_ascents, container, false);
        ascentList = (ListView) result.findViewById(R.id.ascent_list);

        Climber climber = PreferencesHelper.privateSettings(this.getActivity()).getObject(CLIMBER_KEY, null, Climber.class);
        if (climber != null) {
            updateTabContent(climber);
        }
        return result;
    }

    @Override
    public void updateTabContent(Climber climber) {
        if(climber==null){
            ascentList.setAdapter(null);
            return;
        }
        SeparatedListAdapter adapter = new SeparatedListAdapter(this.getActivity(), R.layout.item_separator);

        TickList tickList = climber.getTickList();
        Map<String, Set<Ascent>> ascentsByGrade = tickList.getAscentsByGrade();


        for (Map.Entry<String, Set<Ascent>> entry : ascentsByGrade.entrySet()) {
            Set<Ascent> ascents = entry.getValue();
            final List<Ascent> ascentList = new ArrayList<>(ascents);
            String[] array = new String[ascents.size()];
            for (int i = 0; i < ascentList.size(); i++) {
                Ascent ascent = ascentList.get(i);
                Boulder boulder = ascent.getBoulder();
                array[i] = boulder.getName() + " | " + boulder.getArea().getName()+" | "+ DateFormat.getDateInstance().format(ascent.getDate());
            }
            adapter.addSection(entry.getKey(), new ArrayAdapter<String>(this.getActivity(),
                    R.layout.item_project, array) {

                @NonNull
                @Override
                public View getView(final int position, View convertView, ViewGroup parent) {
                    TextView result = (TextView) super.getView(position, convertView, parent);
                    result.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            Ascent ascent = ascentList.get(position);
                            String url = ascent.getBoulder().getUrl();
                            i.setData(Uri.parse(url));
                            startActivity(i);
                        }
                    });
                    return result;
                }
            });


        }

        ascentList.setAdapter(adapter);

    }


}
