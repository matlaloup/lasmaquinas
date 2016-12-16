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
import org.mlaloup.lasmaquinas.model.Area;
import org.mlaloup.lasmaquinas.model.Boulder;
import org.mlaloup.lasmaquinas.model.Climber;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

public class ProjectsFragment extends TabFragment<Climber> {

    private String CLIMBER_KEY = "climber";

    private ListView projectList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.activity_projects, container, false);
        projectList = (ListView) result.findViewById(R.id.project_list);

        Climber climber = PreferencesHelper.privateSettings(this.getActivity()).getObject(CLIMBER_KEY, null, Climber.class);
        if (climber != null) {
            updateTabContent(climber);
        }
        return result;
    }

    @Override
    public void updateTabContent(Climber climber) {
        if(climber==null){
            projectList.setAdapter(null);
            return;
        }
        SeparatedListAdapter adapter = new SeparatedListAdapter(this.getActivity(), R.layout.item_separator);
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


}
