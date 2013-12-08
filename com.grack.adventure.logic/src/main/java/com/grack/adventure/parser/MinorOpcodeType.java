package com.grack.adventure.parser;

public enum MinorOpcodeType {
	NORMAL,
	CONDITIONAL,
	/**
	 * AND, OR etc.
	 */
	CHAIN,
	BLOCK_END,
	ITERATOR,
	ELSE_BLOCK
}
