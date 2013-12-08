package com.grack.adventure.parser.program;

public class VariableDirective extends Directive {
	private final String name;

	public VariableDirective(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	@Override
	public String toString() {
		return "VARIABLE [" + name +"]";
	}
}
