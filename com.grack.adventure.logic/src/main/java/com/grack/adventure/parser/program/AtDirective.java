package com.grack.adventure.parser.program;

import java.util.List;

import com.grack.adventure.parser.SourceLine;

public class AtDirective {
	private final String place;
	private final List<SourceLine> code;

	public AtDirective(String place, List<SourceLine> code) {
		this.place = place;
		this.code = code;
	}
	
	public List<SourceLine> getCode() {
		return code;
	}
	
	public String getPlace() {
		return place;
	}
	
	@Override
	public String toString() {
		return "AT [" + place + "]";
	}
}
