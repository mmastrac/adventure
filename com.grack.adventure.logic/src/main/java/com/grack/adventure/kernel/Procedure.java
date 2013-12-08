package com.grack.adventure.kernel;

/**
 * Contains pointers to the start and end of a procedure. The code is stored in
 * the global code collection.
 */
public class Procedure {
	private final int start;
	private final int end;

	public Procedure(int start, int end) {
		this.start = start;
		this.end = end;
	}

	public int getStart() {
		return start;
	}

	public int getEnd() {
		return end;
	}

}
