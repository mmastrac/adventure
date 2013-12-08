package com.grack.adventure.parser;

import java.util.List;

public class SourceLine {
	private final MinorOpcode opcode;

	private final List<String> args;

	public SourceLine(MinorOpcode opcode, List<String> args) {
		this.opcode = opcode;
		this.args = args;
	}

	public MinorOpcode getOpcode() {
		return opcode;
	}

	public List<String> getArgs() {
		return args;
	}
	
	@Override
	public String toString() {
		if (args == null)
			return opcode.toString();
		
		return opcode + " " + args;
	}
}
