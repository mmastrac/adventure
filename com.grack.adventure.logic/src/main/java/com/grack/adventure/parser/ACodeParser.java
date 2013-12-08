package com.grack.adventure.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.common.base.CharMatcher;
import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.grack.adventure.parser.program.ACodeProgram;
import com.grack.adventure.parser.program.ActionDirective;
import com.grack.adventure.parser.program.AtDirective;
import com.grack.adventure.parser.program.DefineDirective;
import com.grack.adventure.parser.program.InitialDirective;
import com.grack.adventure.parser.program.LabelDirective;
import com.grack.adventure.parser.program.NullDirective;
import com.grack.adventure.parser.program.ObjectDirective;
import com.grack.adventure.parser.program.PlaceDirective;
import com.grack.adventure.parser.program.RepeatDirective;
import com.grack.adventure.parser.program.SynonymDirective;
import com.grack.adventure.parser.program.TextDirective;
import com.grack.adventure.parser.program.VariableDirective;
import com.grack.adventure.parser.program.VerbDirective;

/**
 * Parses an ACODE file into a stream of directives.
 */
public class ACodeParser {
	/**
	 * The ACODE parser truncates names to 12 characters.
	 */
	private static final int MAX_NAME_LENGTH = 12;

	private final ACodeVisitor visitor;

	public ACodeParser(ACodeVisitor visitor) {
		this.visitor = visitor;
	}

	public static ACodeProgram parseProgram(String input) throws ParseException {
		ACodeProgram program = new ACodeProgram();
		ACodeParser parser = new ACodeParser(program);
		parser.parse(input);
		return program;
	}
	
	public void parse(String input) throws ParseException {
		String line;

		String lastOpcodeLine = null;

		List<String> lineBuffer = new ArrayList<String>();

		Iterator<String> it = Splitter.on('\n').split(input).iterator();
		
		while (it.hasNext()) {
			line = it.next();
			boolean isOpcode = (line.length() > 0 && line.charAt(0) != ' ' && line.charAt(0) != '\t');

			// Replace ^Z with a space
			line = line.replace('\u001a', ' ');
			// Trim spaces
			line = line.trim();
			// System.out.println(">" + line + "<");

			// One form of comment
			if (line.startsWith("*"))
				continue;

			// Slash or !@ indicates white-space preservation
			if (line.startsWith("/"))
				line = line.substring(1);
			else if (line.startsWith("!@"))
				line = line.substring(2);

			if (isOpcode) {
				// Major opcode, parse buffer
				if (lastOpcodeLine != null)
					parseLastOpcode(lastOpcodeLine, lineBuffer);

				lineBuffer.clear();

				lastOpcodeLine = removeComments(line);
			} else {
				lineBuffer.add(line);
			}
		}

		if (lastOpcodeLine.isEmpty())
			return;

		parseLastOpcode(lastOpcodeLine, lineBuffer);
	}

	/**
	 * Remove {}-delimited comments.
	 */
	private String removeComments(String line) {
		return line.replaceAll("\\{[^}]*\\}", "").trim();
	}

	private void parseLastOpcode(String lastOpcodeLine, List<String> lineBuffer) throws ParseException {
		String[] bits = lastOpcodeLine.split("\\s+", 2);

		String opcode = bits[0];
		String args = bits.length > 1 ? bits[1] : null;

		// TODO: Move to MajorObcode
		if (opcode.equals("NULL")) {
			parseNull(args);
		} else if (opcode.equals("SYNON")) {
			parseSynonym(args);
		} else if (opcode.equals("PLACE")) {
			parsePlace(args, lineBuffer);
		} else if (opcode.equals("OBJECT")) {
			parseObject(args, lineBuffer);
		} else if (opcode.equals("VERB")) {
			parseVerb(args);
		} else if (opcode.equals("TEXT")) {
			parseText(args, lineBuffer);
		} else if (opcode.equals("DEFINE")) {
			parseDefine(args);
		} else if (opcode.equals("VARIABLE")) {
			parseVariable(args);
		} else if (opcode.equals("LABEL")) {
			parseLabel(args, lineBuffer);
		} else if (opcode.equals("AT")) {
			parseAt(args, lineBuffer);
		} else if (opcode.equals("ACTION")) {
			parseAction(args, lineBuffer);
		} else if (opcode.equals("INITIAL") || opcode.equals("INIT")) {
			parseInitial(args, lineBuffer);
		} else if (opcode.equals("REPEAT")) {
			parseRepeat(args, lineBuffer);
		} else {
			throw new ParseException("Unknown major opcode: " + opcode);
		}
	}

	private void parsePlace(String name, List<String> lineBuffer) {
		List<String> descriptions = parseDescriptions(lineBuffer);
		if (descriptions.size() == 1)
			descriptions.add("");

		visitor.visitPlace(new PlaceDirective(trim(name), descriptions.get(0), descriptions.subList(1, descriptions.size())));
	}

	private List<String> parseDescriptions(List<String> lineBuffer) {
		List<String> descriptions = new ArrayList<String>();
		StringBuffer currentDescription = new StringBuffer();
		for (String line : lineBuffer) {
			if (line.startsWith("%")) {
				descriptions.add(trimEnd(currentDescription.toString()));
				currentDescription.setLength(0);
				line = line.substring(1);
			}

			if (line.startsWith(">$<") || line.startsWith("*")) {
				// comment
				line = "";
			}

			currentDescription.append(line);
			currentDescription.append("\n");
		}

		descriptions.add(trimEnd(currentDescription.toString()));

		return descriptions;
	}

	private void parseRepeat(String args, List<String> lineBuffer) {
		visitor.visitRepeat(new RepeatDirective(parseCodeSection(lineBuffer)));
	}

	private void parseInitial(String args, List<String> lineBuffer) {
		visitor.visitInitial(new InitialDirective(parseCodeSection(lineBuffer)));
	}

	private void parseAction(String verb, List<String> lineBuffer) {
		Iterator<String> it = Splitter.on(CharMatcher.WHITESPACE).trimResults().omitEmptyStrings().split(verb).iterator();
		String actionName = trim(it.next());
		List<String> keywords = Lists.newArrayList(Iterators.transform(it, new Function<String, String>() {
			public String apply(String input) {
				return trim(input);
			}
		}));
		visitor.visitAction(new ActionDirective(actionName, keywords, parseCodeSection(lineBuffer)));
	}

	private void parseAt(String place, List<String> lineBuffer) {
		visitor.visitAt(new AtDirective(trim(place), parseCodeSection(lineBuffer)));
	}

	private void parseLabel(String name, List<String> lineBuffer) {
		visitor.visitLabel(new LabelDirective(trim(name), parseCodeSection(lineBuffer)));
	}

	private void parseVariable(String args) {
		for (String arg : splitComma(args))
			visitor.visitVariable(new VariableDirective(arg));
	}

	private void parseDefine(String args) {
		for (String arg : splitComma(args))
			visitor.visitDefine(new DefineDirective(arg));
	}

	private void parseText(String name, List<String> lineBuffer) {
		StringBuffer text = new StringBuffer();
		boolean first = true;
		for (String line : lineBuffer) {
			if (first)
				first = false;
			else
				text.append('\n');
			text.append(line);
		}

		visitor.visitText(new TextDirective(trim(name), text.toString()));
	}

	private void parseVerb(String args) {
		String[] allArgs = splitComma(args);
		List<String> synonyms = Arrays.asList(allArgs).subList(1, allArgs.length);
		visitor.visitVerb(new VerbDirective(allArgs[0], synonyms));
	}

	private void parseSynonym(String args) {
		String[] allArgs = splitComma(args);
		List<String> synonyms = Arrays.asList(allArgs).subList(1, allArgs.length);
		visitor.visitSynonym(new SynonymDirective(allArgs[0], synonyms));
	}

	private void parseObject(String name, List<String> lineBuffer) {
		List<String> descriptions = parseDescriptions(lineBuffer);
		visitor.visitObject(new ObjectDirective(trim(name), descriptions.get(0), descriptions.subList(1, descriptions.size())));
	}

	private void parseNull(String args) {
		for (String arg : splitComma(args))
			visitor.visitNullWord(new NullDirective(arg));
	}

	private List<SourceLine> parseCodeSection(List<String> lineBuffer) throws ParseException {
		List<SourceLine> code = new ArrayList<SourceLine>();

		for (String line : lineBuffer) {
			line = removeComments(line);
			if (line.isEmpty())
				continue;

			String[] bits = line.split("\\s+", 2);
			List<String> args = bits.length == 1 ? null : Arrays.asList(splitComma(bits[1]));
			code.add(new SourceLine(MinorOpcode.lookup(bits[0]), args));
		}

		return code;
	}

	private String[] splitComma(String args) {
		String[] split = args.split(",");
		for (int i = 0; i < split.length; i++)
			split[i] = split[i].trim();
		return split;
	}

	public static String trim(String name) {
		if (name != null && name.length() > MAX_NAME_LENGTH)
			name = name.substring(0, MAX_NAME_LENGTH);
		return name;
	}

	public static String trimEnd(String s) {
		s = s.replaceAll("[ ]+$", "");
		if (s.endsWith("\n"))
			s = s.substring(0, s.length() - 1);
		return s;
	}
}
