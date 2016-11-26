package org.mlaloup.lasmaquinas.model.settings;

public class DefaultGradeScoreScale implements GradeScoreScale {

	// 6b = 1
	// 6b+ = 2
	// 6c = 5
	// 6c+ = 10
	// 7a = 25
	// 7a+ = 50
	// 7b = 125
	// 7b+ = 250
	// 7c = 625
	// etc ...

	private static final int supFactor = 2;

	private static int letterFactor = 5;

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
		double factor = Math.pow(letterFactor, fullDiff);
		if (grade.length() > 2) {
			if ('+' == grade.charAt(2)) {
				factor = factor * 2;
			}
		}

		return (int) factor;
	}
}
