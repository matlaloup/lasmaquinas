package org.mlaloup.lasmaquinas.model;


import org.apache.commons.lang3.StringUtils;
import org.mlaloup.lasmaquinas.model.settings.TickListSettings;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Climber {

    private String login;

    private String fullName;

    private String countryBigram;

    private TickList tickList = new TickList(this, TickListSettings.unlimited());

    private Set<Boulder> projects = new HashSet<>();

    public Climber(String login){
        this.login = login;
        if(login==null){
            throw new IllegalArgumentException("Null login !");
        }
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getCountryBigram() {
        return countryBigram;
    }

    public void setCountryBigram(String countryBigram) {
        this.countryBigram = countryBigram;
    }

    public String getLogin() {
        return login;
    }

    public String getDisplayName(){
        if(StringUtils.isNotBlank(fullName)){
            return fullName;
        }
        return login;
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

        return login.equals(climber.login);

    }

    @Override
    public int hashCode() {
        return login.hashCode();
    }
}
