package com.grack.adventure.kernel.entity;

import com.grack.adventure.kernel.VirtualMachine;

public class VariableEntity extends Entity {
	public VariableEntity(String name) {
		super(name);
	}

	public boolean isEntity(VirtualMachine vm) {
		return Entity.isEntityId(vm.getEntityValue(this));
	}

	public void setEntity(VirtualMachine vm, Entity entity) {
		setEntity(vm, entity.getId());
	}

	public void setEntity(VirtualMachine vm, int id) {
		assert Entity.isEntityId(id) || id == 9999 : "Not a valid entity ID: " + id;
		super.setIntValue(vm, id);
	}

	@Override
	public String toString() {
		return "{VARIABLE " + name + "}";
	}

	public void setFrom(VirtualMachine vm, VariableEntity other) {
		setIntValue(vm, vm.getEntityValue(other));
		setFlags(vm, vm.getFlags(other));
	}
	
	@Override
	public Entity clone() {
		return clone(new VariableEntity(name));
	}
}
