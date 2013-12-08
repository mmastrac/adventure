package com.grack.adventure.parser.program;

import java.util.List;

public class VerbDirective {
	private final String name;
	private final List<String> synonyms;

	public VerbDirective(String name, List<String> synonyms) {
		this.name = name;
		this.synonyms = synonyms;
	}
	
	public String getName() {
		return name;
	}
	
	public List<String> getSynonyms() {
		return synonyms;
	}
	
	@Override
	public String toString() {
		return "VERB [" + name + "]";
	}
}
