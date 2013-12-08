package com.grack.adventure.parser.program;

import java.util.List;

import com.grack.adventure.parser.SourceLine;

public class RepeatDirective extends Directive {
	private final List<SourceLine> code;

	public RepeatDirective(List<SourceLine> code) {
		this.code = code;
	}
	
	public List<SourceLine> getCode() {
		return code;
	}
	
	@Override
	public String toString() {
		return "REPEAT";
	}
}
