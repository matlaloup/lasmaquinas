package org.mlaloup.lasmaquinas.model.settings;

public class SmoothGradeScoreScale implements GradeScoreScale {

	// 6b = 2
	// 6b+ = 3
	// 6c = 6
	// 6c+ = 9
	// 7a = 18
	// 7a+ = 27
	// 7b = 54
	// 7b+ = 81
	// 7c = 162
	// etc ...

	private static final double supFactor = 1.5;

	private static int letterFactor = 3;

	private static int baseValue = 2;

	private static int baseNumber = 6;

	private static char baseLetter = 'b';

	@Override
	public int computeScore(String grade) {
		if (grade.length() < 2) {
			return 0;
		}
		Character firstChar = grade.charAt(0);
		int firstNumber = Integer.valueOf(firstChar.toString());
		char letter = grade.charAt(1);
		if (firstNumber < baseNumber) {
			return 0;
		}
		int numberDiff = (firstNumber - baseNumber) * 3;
		int letterDiff = letter - baseLetter;
		int fullDiff = numberDiff + letterDiff;
		if (fullDiff < 0) {
			return 0;
		}
		double factor = baseValue * Math.pow(letterFactor, fullDiff);
		if (grade.length() > 2) {
			if ('+' == grade.charAt(2)) {
				factor = factor * supFactor;
			}
		}

		return (int) factor;
	}

	public static void main(String[] args){
		GradeScoreScale scale = new SmoothGradeScoreScale();
		System.out.println(scale.computeScore("6a"));
		System.out.println(scale.computeScore("6a+"));
		System.out.println(scale.computeScore("6b"));
		System.out.println(scale.computeScore("6b+"));
		System.out.println(scale.computeScore("6c"));
		System.out.println(scale.computeScore("6c+"));
		System.out.println(scale.computeScore("7a"));
		System.out.println(scale.computeScore("7a+"));
		System.out.println(scale.computeScore("7b"));
		System.out.println(scale.computeScore("7b+"));
		System.out.println(scale.computeScore("7c"));

	}
}
