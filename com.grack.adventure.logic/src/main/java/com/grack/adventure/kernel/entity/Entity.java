package com.grack.adventure.kernel.entity;

import com.grack.adventure.kernel.KernelValue;
import com.grack.adventure.kernel.VirtualMachine;

public abstract class Entity implements KernelValue, Cloneable {
	/**
	 * Where IDs for entities start. IDs grow upward. Note that it doesn't start
	 * against the floor of {@link Integer} to avoid underflow when performing
	 * entity ID math.
	 */
	public static final int ENTITY_ID_START = Integer.MIN_VALUE + 0x1000;

	/**
	 * End of entity ID range.
	 */
	public static final int ENTITY_ID_END = ENTITY_ID_START + 0x100000;

	public static boolean isEntityId(int id) {
		return id < ENTITY_ID_END;
	}

	/**
	 * All entities have an id (the index into the global entity list).
	 */
	protected int id;

	protected final String name;

	public Entity(String name) {
		this.name = name;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	/**
	 * All entities have an integer value that can be set or retrieved.
	 */
	public int getIntValue(VirtualMachine vm) {
		return vm.getEntityValue(this);
	}

	public void setIntValue(VirtualMachine vm, int value) {
		vm.setEntityValue(this, value);
	}

	/**
	 * All entities have a set of bit flags.
	 */
	public void setFlag(VirtualMachine vm, int flag) {
		int flags = vm.getFlags(this);
		flags |= 1 << flag;
		vm.setFlags(this, flags);
	}

	public void clearFlag(VirtualMachine vm, int value) {
		int flags = vm.getFlags(this);
		flags &= ~(1 << value);
		vm.setFlags(this, flags);
	}

	public boolean hasFlag(VirtualMachine vm, int flag) {
		int flags = vm.getFlags(this);
		return (flags & (1 << flag)) != 0;
	}

	public int getFlags(VirtualMachine vm) {
		int flags = vm.getFlags(this);
		return flags;
	}

	public void setFlags(VirtualMachine vm, int flags) {
		vm.setFlags(this, flags);
	}

	public String getName() {
		return name;
	}

	public String getTextValue() {
		// Bad SAY
		return "";
	}

	public int getDefaultFlags() {
		return 0;
	}

	public int getStateCount() {
		return 0;
	}

	public <T extends Entity> T clone(T clone) {
		clone.id = id;
		return clone;
	}

	public abstract Entity clone();
}
