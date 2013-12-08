package com.grack.adventure.kernel;

/**
 * Something that has a value.
 */
public interface KernelValue {
	int getIntValue(VirtualMachine vm);
}
