package org.mlaloup.lasmaquinas.model;

public class Boulder {

    private String area;

    private String name;

    private Grade grade;

    public Boulder(Grade grade, String area, String name) {
        this.grade = grade;
        this.area = area;
        this.name = name;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
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


}
