package com.grack.adventure.parser.program;

public class NullDirective extends Directive {
	private final String word;

	public NullDirective(String word) {
		this.word = word;
	}

	public String getWord() {
		return word;
	}

	public String toString() {
		return "NULL [" + word + "]";
	};
}
