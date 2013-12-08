package com.grack.adventure.kernel.entity;

public class LabelEntity extends Entity {

	public LabelEntity(String name) {
		super(name);
	}

	@Override
	public String toString() {
		return "{LABEL " + name + "}";
	}
	
	@Override
	public LabelEntity clone() {
		return clone(new LabelEntity(name));
	}

}
