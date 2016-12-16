package org.mlaloup.lasmaquinas.activity.tab;

import org.mlaloup.lasmaquinas.model.Climber;

public enum AccountTab {
    SUMMARY(SummaryFragment.class,"Résumé"),
    ASCENTS(AscentsFragment.class,"Répétitions"),
    PROJECTS(ProjectsFragment.class,"Projets");

    private Class<? extends TabFragment<Climber>> fragmentClass;

    private String tabName;

    private AccountTab(Class<? extends TabFragment<Climber>> fragmentClass, String tabName) {
        this.fragmentClass = fragmentClass;
        this.tabName = tabName;
    }

    public String getTabName() {
        return tabName;
    }

    public TabFragment<Climber> getFragment(){
        try {
            return fragmentClass.newInstance();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }



}
