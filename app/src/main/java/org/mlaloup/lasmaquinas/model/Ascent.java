package org.mlaloup.lasmaquinas.model;

import java.util.Date;

public class Ascent implements Comparable<Ascent> {

	private boolean flash;

	private Boulder boulder;

	private Date date;

	public Ascent(Boulder boulder, Date date, boolean flash) {
		this.flash = flash;
		this.boulder = boulder;
		this.date = date;
	}

	public boolean isFlash() {
		return flash;
	}

	public void setFlash(boolean flash) {
		this.flash = flash;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	@Override
	public int compareTo(Ascent otherAscent) {
		int result = otherAscent.boulder.getGrade().getGrade().compareTo(boulder.getGrade().getGrade());
		if (result == 0) {
			result = date.compareTo(otherAscent.date);
			if (result == 0) {
				result = boulder.getName().compareTo(otherAscent.boulder.getName());
				if (result == 0) {
					result = boulder.getArea().getName().compareTo(otherAscent.boulder.getArea().getName());
				}
			}
		}
		return result;
	}

	public Boulder getBoulder() {
		return boulder;
	}
}
