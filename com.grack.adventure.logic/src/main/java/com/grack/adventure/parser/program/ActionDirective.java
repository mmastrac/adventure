package com.grack.adventure.parser.program;

import java.util.List;

import com.grack.adventure.parser.SourceLine;

public class ActionDirective extends Directive {
	private final String name;
	private final List<SourceLine> code;
	private final List<String> keywords;

	public ActionDirective(String name, List<String> keywords, List<SourceLine> code) {
		this.name = name;
		this.keywords = keywords;
		this.code = code;
	}

	public List<SourceLine> getCode() {
		return code;
	}

	public String getName() {
		return name;
	}
	
	public List<String> getKeywords() {
		return keywords;
	}
	
	@Override
	public String toString() {
		return "ACTION [" + name + " " + keywords + "]";
	}
}
