package com.grack.adventure.parser.program;

public class DefineDirective {
	private final String name;

	public DefineDirective(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return "DEFINE [" + name + "]";
	};
}
