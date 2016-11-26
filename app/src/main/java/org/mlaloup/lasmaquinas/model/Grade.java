package org.mlaloup.lasmaquinas.model;

import org.mlaloup.lasmaquinas.model.settings.GradeScoreScale;

public class Grade {

	private String grade;

	public String getGrade() {
		return grade;
	}

	public Grade(String grade) {
		this.grade = grade;
	}

	public int getScore(GradeScoreScale scale) {
		return scale.computeScore(grade);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((grade == null) ? 0 : grade.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Grade other = (Grade) obj;
		if (grade == null) {
			if (other.grade != null)
				return false;
		} else if (!grade.equals(other.grade))
			return false;
		return true;
	}

}
