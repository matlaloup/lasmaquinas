package org.mlaloup.lasmaquinas.model;



import org.mlaloup.lasmaquinas.model.settings.TickListSettings;

import java.util.HashSet;
import java.util.Set;

public class Climber extends ClimberRef {

    private TickList tickList = new TickList(this, TickListSettings.unlimited());

    private Set<Boulder> projects = new HashSet<>();

    public Climber(String login){
        super(login);
    }

    public TickList getTickList() {
        return tickList;
    }

    public Set<Boulder> getProjects() {
        return projects;
    }

    public void setProjects(Set<Boulder> projects) {
        this.projects = projects;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Climber climber = (Climber) o;

        return getLogin().equals(climber.getLogin());

    }

}
