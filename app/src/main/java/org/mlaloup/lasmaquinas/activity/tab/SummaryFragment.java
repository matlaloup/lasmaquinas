package org.mlaloup.lasmaquinas.activity.tab;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.mlaloup.lasmaquinas.activity.R;
import org.mlaloup.lasmaquinas.activity.util.PreferencesHelper;

import org.mlaloup.lasmaquinas.model.Climber;
import org.mlaloup.lasmaquinas.model.TickList;


public class SummaryFragment extends TabFragment<Climber> {

    private String CLIMBER_KEY = "climber";

    private TextView tickListSummary;
    private TextView tickListCount;
    private TextView tickListScore;
    private TextView projectCount;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.activity_summary, container, false);

        tickListSummary = (TextView) result.findViewById(R.id.personal_ticklist_summary);
        tickListCount = (TextView) result.findViewById(R.id.personal_ticklist_count);
        projectCount = (TextView) result.findViewById(R.id.personal_project_list_count);
        tickListScore = (TextView) result.findViewById(R.id.personal_ticklist_score);



        Climber climber = PreferencesHelper.privateSettings(this.getActivity()).getObject(CLIMBER_KEY, null, Climber.class);
        if (climber != null) {
            updateTabContent(climber);
        }
        return result;
    }

    @Override
    public void updateTabContent(Climber climber) {
        if(climber == null){
            return;
        }

        TickList tickList = climber.getTickList();
        tickListSummary.setText(tickList.getSummary());
        tickListCount.setText(String.valueOf(tickList.getAscents().size()));
        projectCount.setText(String.valueOf(climber.getProjects().size()));
        tickListScore.setText(String.valueOf(tickList.getScore()));
    }


}
