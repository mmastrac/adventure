package com.grack.adventure.kernel;

import java.util.Map;

import com.google.common.collect.Maps;
import com.grack.adventure.kernel.entity.Entity;
import com.grack.adventure.parser.ACodeParser;
import com.grack.adventure.parser.ParseException;

/**
 * Tracks the relationship between named {@link Entity} objects and their ids.
 */
public class IdResolver {
	private final Map<String, Integer> addresses;
	private final Map<String, Integer> constants;
	private final Map<String, String> synonyms;
	private final Map<String, Integer> stateCounts;

	public IdResolver() {
		this.addresses = Maps.newHashMap();
		this.constants = Maps.newHashMap();
		this.synonyms = Maps.newHashMap();
		this.stateCounts = Maps.newHashMap();
	}

	public void addConstant(String name, int value) {
		constants.put(name, value);
	}

	public void addSynonym(String synonymName, String word) {
		synonyms.put(synonymName, word);
	}

	public void addEntity(String name, int stateCount, int id) {
		addresses.put(name, id);
		stateCounts.put(name, stateCount);
	}

	public int getIntegerValue(String arg) throws ParseException {
		if (arg.matches("-?[0-9]+"))
			return Integer.parseInt(arg);

		if (arg.contains("-") || arg.contains("+")) {
			int value = 0;
			boolean negative = false;
			StringBuilder soFar = new StringBuilder();
			for (int i = 0; i < arg.length(); i++) {
				char c = arg.charAt(i);
				if (c == '-' || c == '+') {
					if (soFar.length() > 0) {
						int bit = getValue(soFar.toString());
						if (negative) {
							value -= bit;
						} else {
							value += bit;
						}
						soFar.setLength(0);
					}

					if (c == '-') {
						negative = true;
					} else
						negative = false;
				} else {
					soFar.append(c);
				}
			}

			int bit = getValue(soFar.toString());
			if (negative) {
				value -= bit;
			} else {
				value += bit;
			}

			return value;
		}

		arg = ACodeParser.trim(arg);

		return getValue(arg);
	}

	private int getValue(String s) throws ParseException {
		if (s.matches("[0-9]+"))
			return Integer.parseInt(s);

		if (s.startsWith("@"))
			return stateCounts.get(s.substring(1));

		s = ACodeParser.trim(s);

		while (synonyms.containsKey(s))
			s = synonyms.get(s);

		if (constants.containsKey(s))
			return constants.get(s);

		return getEntityIndex(s);
	}

	public int getEntityIndex(String name) throws ParseException {
		name = ACodeParser.trim(name);

		while (synonyms.containsKey(name))
			name = synonyms.get(name);

		Integer address = addresses.get(name);
		if (address == null)
			throw new ParseException("Unknown entity: " + name);

		return address;
	}
}
