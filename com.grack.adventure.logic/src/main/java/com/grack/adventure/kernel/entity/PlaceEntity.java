package com.grack.adventure.kernel.entity;

import java.util.List;

import com.grack.adventure.kernel.Constants;

public class PlaceEntity extends Entity {
	private final String shortDesc;
	private final List<String> states;

	public PlaceEntity(String name, String shortDesc, List<String> states) {
		super(name);
		this.shortDesc = shortDesc;
		this.states = states;
	}

	@Override
	public int getDefaultFlags() {
		return 1 << Constants.XPLACE;
	}

	public String getShortDesc() {
		return shortDesc;
	}

	public String getLongDesc() {
		return states.get(0);
	}

	@Override
	public int getStateCount() {
		return states.size();
	}

	@Override
	public String toString() {
		return "{PLACE " + name + "}";
	}
	
	@Override
	public PlaceEntity clone() {
		return clone(new PlaceEntity(shortDesc, shortDesc, states));
	}
}
