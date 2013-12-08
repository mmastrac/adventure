package com.grack.adventure.kernel;

import java.util.List;

import com.grack.adventure.parser.MinorOpcode;
import com.grack.adventure.parser.SourceLine;

/**
 * A compiled version of {@link SourceLine}. To simplify things, we suck the
 * boolean operation into the compiled line (potentially with an inverted
 * condition).
 */
public class CompiledLine {
	private final MinorOpcode opcode;
	private final List<KernelValue> args;
	private final CompiledBooleanOp booleanOp;
	private final boolean not;

	public CompiledLine(MinorOpcode opcode, List<KernelValue> args, CompiledBooleanOp booleanOp, boolean not) {
		this.opcode = opcode;
		this.args = args;
		this.booleanOp = booleanOp;
		this.not = not;
	}

	public boolean isNot() {
		return not;
	}

	public CompiledBooleanOp getBooleanOp() {
		return booleanOp;
	}

	public MinorOpcode getOpcode() {
		return opcode;
	}

	public List<KernelValue> getArgs() {
		return args;
	}

	public KernelValue getArg(int index) {
		return args.get(index);
	}

	public String toString() {
		String prefix = "";
		if (booleanOp != null)
			prefix += booleanOp + " ";
		prefix += not ? "NOT " : "";

		if (args == null)
			return prefix + opcode.toString();

		return prefix + opcode + " " + args;
	};
}
