package com.grack.adventure.kernel.entity;

import com.grack.adventure.kernel.Constants;

public class VerbEntity extends Entity {

	public VerbEntity(String name) {
		super(name);
	}

	@Override
	public int getDefaultFlags() {
		return 1 << Constants.XVERB;
	}

	@Override
	public String toString() {
		return "{VERB: " + name + "}";
	}
	
	@Override
	public VerbEntity clone() {
		return clone(new VerbEntity(name));
	}
}
