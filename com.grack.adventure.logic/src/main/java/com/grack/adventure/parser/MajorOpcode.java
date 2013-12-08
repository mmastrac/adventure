package com.grack.adventure.parser;

/**
 * "Major" opcodes.
 * 
 * TODO: Some of these are still hard-coded in {@link ACodeParser}.
 */
public enum MajorOpcode {
	/**
	 * Specifies an object.
	 */
	OBJECT,

	PLACE,

	/**
	 * Specifies an action that can either be a VERB or OBJECT (DRINK/WATER), or
	 * a VERB/OBJECT and another word (DRINK WATER/WATER DOOR).
	 */
	ACTION,

}
