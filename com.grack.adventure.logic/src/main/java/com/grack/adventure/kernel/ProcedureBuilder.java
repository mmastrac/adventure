package com.grack.adventure.kernel;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.grack.adventure.kernel.entity.Entity;
import com.grack.adventure.kernel.world.World;
import com.grack.adventure.parser.SourceLine;
import com.grack.adventure.parser.MinorOpcodeType;
import com.grack.adventure.parser.MinorOpcode;
import com.grack.adventure.parser.ParseException;

public class ProcedureBuilder {
	private static final Map<MinorOpcode, CompiledBooleanOp> BOOLEAN_OP_MAP = Maps.newHashMap();

	static {
		BOOLEAN_OP_MAP.put(MinorOpcode.AND, CompiledBooleanOp.AND);
		BOOLEAN_OP_MAP.put(MinorOpcode.OR, CompiledBooleanOp.OR);
		BOOLEAN_OP_MAP.put(MinorOpcode.EOR, CompiledBooleanOp.EOR);
	}

	/**
	 * Maps a raw list of {@link SourceLine}s to a set of {@link CompiledLine}
	 * objects. This resolves entities and constants, as well as folds
	 * conditionals into the lines they affect to make interpreting easier.
	 */
	public static Procedure build(World world, IdResolver resolver, List<SourceLine> code) throws ParseException {
		CompiledBooleanOp booleanOp = null;
		boolean lastWasNot = false;

		List<CompiledLine> parsedCode = Lists.newArrayList();
		
		// Merge adjacent ANYOF lines first
		for (int i = 0; i < code.size(); i++) {
			SourceLine line = code.get(i);
			if (line.getOpcode() == MinorOpcode.ANYOF) {
				while (i < code.size() - 1 && code.get(i + 1).getOpcode() == MinorOpcode.ANYOF) {
					SourceLine newLine = new SourceLine(line.getOpcode(), Lists.newArrayList(Iterables.concat(line.getArgs(),
							code.get(i + 1).getArgs())));
					code.set(i, newLine);
					code.remove(i + 1);
				}
			}
		}

		for (SourceLine line : code) {
			List<KernelValue> parsedArgs = null;

			switch (line.getOpcode()) {
			case NOT:
				// Toggle the last NOT state
				lastWasNot = !lastWasNot;
				continue;
			case AND:
			case OR:
			case EOR:
				if (lastWasNot)
					throw new ParseException("NOT before conditional");
				if (booleanOp != null)
					throw new ParseException(line.getOpcode() + " after " + booleanOp);
				booleanOp = BOOLEAN_OP_MAP.get(line.getOpcode());
				continue;
			default:
				if (booleanOp != null) {
					if (line.getOpcode().getType() != MinorOpcodeType.CONDITIONAL) {
						throw new ParseException("AND/OR/EOR instruction without a conditional");
					}
				}
			}

			if (lastWasNot) {
				if (line.getOpcode().getType() != MinorOpcodeType.CONDITIONAL) {
					throw new ParseException("NOT instruction without a conditional");
				}
			}

			if (line.getArgs() != null) {
				parsedArgs = Lists.newArrayList();
				for (String arg : line.getArgs()) {
					// Resolve the argument to an integer, then to an entity if
					// that's what it resolves to
					int value = resolver.getIntegerValue(arg);
					if (Entity.isEntityId(value)) {
						Entity entity = world.getEntityById(value);
						if (entity == null)
							throw new ParseException("Expression does not resolve to an entity: " + arg);
						parsedArgs.add(entity);
					} else {
						parsedArgs.add(new ImmediateValue(value));
					}
				}
			}

			boolean first = true;

			switch (line.getOpcode()) {
			case AT:
				for (KernelValue value : parsedArgs) {
					parsedCode
							.add(new CompiledLine(MinorOpcode.IFAT, Arrays.asList(value), first ? null : CompiledBooleanOp.AND, true));
					first = false;
				}
				parsedCode.add(new CompiledLine(MinorOpcode.PROCEED, null, null, false));
				parsedCode.add(new CompiledLine(MinorOpcode.FIN, null, null, false));
				break;
			case NEAR:
				if (parsedArgs.size() > 1)
					throw new ParseException("NEAR expects a single argument");
				for (KernelValue value : parsedArgs) {
					parsedCode.add(new CompiledLine(MinorOpcode.IFNEAR, Arrays.asList(value), first ? null : CompiledBooleanOp.AND,
							true));
					first = false;
				}
				parsedCode.add(new CompiledLine(MinorOpcode.PROCEED, null, null, false));
				parsedCode.add(new CompiledLine(MinorOpcode.FIN, null, null, false));
				break;
			case KEYWORD:
				for (KernelValue value : parsedArgs) {
					parsedCode
							.add(new CompiledLine(MinorOpcode.IFKEY, Arrays.asList(value), first ? null : CompiledBooleanOp.OR, true));
					first = false;
				}
				parsedCode.add(new CompiledLine(MinorOpcode.PROCEED, null, null, false));
				parsedCode.add(new CompiledLine(MinorOpcode.FIN, null, null, false));
				break;
			case HAVE:
				if (parsedArgs.size() > 1)
					throw new ParseException("HAVE expects a single argument");
				for (KernelValue value : parsedArgs) {
					parsedCode.add(new CompiledLine(MinorOpcode.IFHAVE, Arrays.asList(value), first ? null : CompiledBooleanOp.AND,
							true));
					first = false;
				}
				parsedCode.add(new CompiledLine(MinorOpcode.PROCEED, null, null, false));
				parsedCode.add(new CompiledLine(MinorOpcode.FIN, null, null, false));
				break;
			case ANYOF:
				// ANYOF key1, key2
				// ->
				// NOT
				// IFKEY key1
				// AND
				// NOT
				// IFKEY key2
				// ...
				for (KernelValue value : parsedArgs) {
					parsedCode
							.add(new CompiledLine(MinorOpcode.IFKEY, Arrays.asList(value), first ? null : CompiledBooleanOp.AND, true));
					first = false;
				}
				parsedCode.add(new CompiledLine(MinorOpcode.PROCEED, null, null, false));
				parsedCode.add(new CompiledLine(MinorOpcode.FIN, null, null, false));
				break;
			case EOF:
				// TODO: hack!
				parsedCode.add(new CompiledLine(MinorOpcode.FIN, null, null, false));
				parsedCode.add(new CompiledLine(MinorOpcode.FIN, null, null, false));
				parsedCode.add(new CompiledLine(MinorOpcode.FIN, null, null, false));
				parsedCode.add(new CompiledLine(MinorOpcode.FIN, null, null, false));
				parsedCode.add(new CompiledLine(MinorOpcode.FIN, null, null, false));
				parsedCode.add(new CompiledLine(MinorOpcode.FIN, null, null, false));
				break;
			default:
				CompiledLine parsed = new CompiledLine(line.getOpcode(), parsedArgs, booleanOp, lastWasNot);
				parsedCode.add(parsed);
			}

			lastWasNot = false;
			booleanOp = null;
		}

		int start = world.getProgram().getCode().size();
		world.getProgram().getCode().addAll(parsedCode);
		int end = world.getProgram().getCode().size();
		return new Procedure(start, end);
	}
}
