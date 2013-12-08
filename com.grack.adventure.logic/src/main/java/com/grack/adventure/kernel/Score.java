package com.grack.adventure.kernel;

public class Score {
	private int score, maxScore;
	
	public Score(int score, int maxScore) {
		this.score = score;
		this.maxScore = maxScore;
	}
	
	public int getMaxScore() {
		return maxScore;
	}
	
	public int getScore() {
		return score;
	}
}
