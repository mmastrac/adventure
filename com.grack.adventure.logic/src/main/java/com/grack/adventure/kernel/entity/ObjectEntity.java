package com.grack.adventure.kernel.entity;

import java.util.List;

import com.grack.adventure.kernel.Constants;
import com.grack.adventure.kernel.VirtualMachine;

public class ObjectEntity extends Entity {
	private final String desc;
	private final List<String> states;

	public ObjectEntity(String name, String desc, List<String> states) {
		super(name);
		this.desc = desc;
		this.states = states;
	}
	
	@Override
	public int getDefaultFlags() {
		return 1 << Constants.XOBJECT;
	}
	
	public String getDesc() {
		return desc;
	}
	
	public List<String> getStates() {
		return states;
	}
	
	public int getLocation(VirtualMachine vm) {
		return vm.getEntityLocation(this);
	}
	
	public void setLocation(VirtualMachine vm, int location) {
		vm.setEntityLocation(this, location);
	}
	
	@Override
	public String getTextValue() {
		return desc;
	}
	
	@Override
	public int getStateCount() {
		return states.size();
	}

	@Override
	public String toString() {
		return "{OBJECT: " + name + "(" + desc + ")}";
	}
	
	@Override
	public Entity clone() {
		ObjectEntity clone = clone(new ObjectEntity(desc, desc, states));
		return clone;
	}
}
