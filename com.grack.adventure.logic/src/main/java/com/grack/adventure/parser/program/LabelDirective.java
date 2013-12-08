package com.grack.adventure.parser.program;

import java.util.List;

import com.grack.adventure.parser.SourceLine;

public class LabelDirective extends Directive {
	private final String name;
	private final List<SourceLine> code;

	public LabelDirective(String name, List<SourceLine> code) {
		this.name = name;
		this.code = code;
	}

	public List<SourceLine> getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return "LABEL [" + name + "]";
	};
}
