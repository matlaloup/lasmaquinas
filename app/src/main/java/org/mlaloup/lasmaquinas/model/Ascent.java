package org.mlaloup.lasmaquinas.model;

import java.util.Date;

public class Ascent implements Comparable<Ascent> {

	private boolean flash;

	private String area;

	private String name;

	private Grade grade;

	private Date date;

	public Ascent(String name, Grade grade, boolean flash, String area, Date date) {
		this.flash = flash;
		this.area = area;
		this.name = name;
		this.grade = grade;
		this.date = date;
	}

	public boolean isFlash() {
		return flash;
	}

	public void setFlash(boolean flash) {
		this.flash = flash;
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

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public int compareTo(Ascent otherAscent) {
		int result = otherAscent.grade.getGrade().compareTo(grade.getGrade());
		if (result == 0) {
			result = date.compareTo(otherAscent.date);
			if (result == 0) {
				result = name.compareTo(otherAscent.name);
				if (result == 0) {
					result = area.compareTo(otherAscent.area);
				}
			}
		}
		return result;
	}

}
