package com.grack.adventure.parser.program;

import java.util.List;

import com.grack.adventure.parser.SourceLine;

public class InitialDirective extends Directive {
	private final List<SourceLine> code;

	public InitialDirective(List<SourceLine> code) {
		this.code = code;
	}

	public List<SourceLine> getCode() {
		return code;
	}

	public String toString() {
		return "INITIAL";
	};
}
