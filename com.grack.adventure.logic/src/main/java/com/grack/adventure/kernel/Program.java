package com.grack.adventure.kernel;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * The collection of code for the entire game program.
 */
public class Program {
	private List<CompiledLine> code = Lists.newArrayList();

	public Program() {
	}
	
	public List<CompiledLine> getCode() {
		return code;
	}
}
