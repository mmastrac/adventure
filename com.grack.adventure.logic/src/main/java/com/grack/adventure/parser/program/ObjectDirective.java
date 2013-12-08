package com.grack.adventure.parser.program;

import java.util.List;

public class ObjectDirective extends Directive {
	private final String name;
	private final String desc;
	private final List<String> states;

	public ObjectDirective(String name, String desc, List<String> states) {
		this.name = name;
		this.desc = desc;
		this.states = states;
	}
	
	public String getDesc() {
		return desc;
	}
	
	public String getName() {
		return name;
	}
	
	public List<String> getStates() {
		return states;
	}
	
	@Override
	public String toString() {
		return "OBJECT [" + name + "]";
	}
}
