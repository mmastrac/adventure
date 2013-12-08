package com.grack.adventure.parser.program;

import java.util.List;

public class PlaceDirective extends Directive {
	private final String name;
	private final String shortDesc;
	private final List<String> states;

	public PlaceDirective(String name, String shortDesc, List<String> states) {
		this.name = name;
		this.shortDesc = shortDesc;
		this.states = states;
	}

	public List<String> getStates() {
		return states;
	}

	public String getName() {
		return name;
	}

	public String getShortDesc() {
		return shortDesc;
	}

	@Override
	public String toString() {
		return "PLACE [" + name + "]";
	}
}
