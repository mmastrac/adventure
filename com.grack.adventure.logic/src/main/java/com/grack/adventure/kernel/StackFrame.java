package com.grack.adventure.kernel;

import java.util.Arrays;
import java.util.List;

/**
 * Points to a specific {@link CompiledLine} in a {@link Procedure}.
 * 
 * TODO: This shouldn't be Procedure-aware to support game serialization.
 */
public class StackFrame implements SaveAware {
	private int ip;
	private int end;

	public StackFrame(Procedure procedure) {
		this.ip = procedure.getStart();
		this.end = procedure.getEnd();
	}

	public int getInstructionPointer() {
		return ip;
	}

	public int getEnd() {
		return end;
	}
	
	public void setOpcode(int opcode) {
		this.ip = opcode;
	}

	public void step() {
		this.ip++;
	}

	public List<String> save() {
		return Arrays.asList("" + ip, "" + end);
	}
}
