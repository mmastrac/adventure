package com.grack.adventure.parser.program;

import java.util.List;

public class SynonymDirective extends Directive {
	private final String word;
	private final List<String> synonyms;

	public SynonymDirective(String word, List<String> synonyms) {
		this.word = word;
		this.synonyms = synonyms;
	}

	public List<String> getSynonyms() {
		return synonyms;
	}

	public String getWord() {
		return word;
	}

	public String toString() {
		return "SYNONYM " + synonyms + " = [" + word + "]";
	};
}
