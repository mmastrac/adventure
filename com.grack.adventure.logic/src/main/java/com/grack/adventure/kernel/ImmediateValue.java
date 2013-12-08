package com.grack.adventure.kernel;

public class ImmediateValue implements KernelValue {
	private final int value;

	public ImmediateValue(int value) {
		this.value = value;
	}

	public int getIntValue(VirtualMachine vm) {
		return value;
	}

	public String toString() {
		return "" + value;
	};
}
