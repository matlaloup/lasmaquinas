package org.mlaloup.lasmaquinas.model;

import org.mlaloup.lasmaquinas.parser.BleauInfoParser;

public class Boulder implements Comparable<Boulder> {

    private String id;

    private Area area;

    private String name;

    private Grade grade;

    public Boulder(Grade grade, Area area, String id, String name) {
        this.grade = grade;
        this.area = area;
        this.name = name;
        this.id = id;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Grade getGrade() {
        return grade;
    }

    public void setGrade(Grade grade) {
        this.grade = grade;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public int compareTo(Boulder otherBoulder) {
        int gradeComparison = otherBoulder.getGrade().getGrade().compareTo(getGrade().getGrade());
        if(gradeComparison !=0){
            return  gradeComparison;
        }
        int areaComparison = getArea().getName().compareTo(otherBoulder.getArea().getName());
        if(areaComparison != 0){
            return  areaComparison;
        }
        return getName().compareTo(otherBoulder.getName());
    }

    public String getUrl(){
       return getArea().getURL()+"/"+getId()+".html";
    }


}
