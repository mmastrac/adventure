package com.grack.adventure.parser.program;

public class TextDirective extends Directive {
	private final String id;
	private final String text;

	public TextDirective(String id, String text) {
		this.id = id;
		this.text = text;
	}

	public String getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public String toString() {
		return "TEXT [" + id + "]";
	};
}
