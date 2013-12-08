package com.grack.adventure.kernel.entity;

public class SynonymEntity extends Entity {
	public SynonymEntity(String name) {
		super(name);
	}
	
	@Override
	public Entity clone() {
		return clone(new SynonymEntity(getName()));
	}

}
