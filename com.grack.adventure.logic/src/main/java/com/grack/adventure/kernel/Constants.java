package com.grack.adventure.kernel;

public class Constants {
	/**
	 * Set on STATUS if the player moved since the last time this was cleared.
	 */
	public static final int STATUS_MOVED = 0;

	/**
	 * Only show the short description for places if the player has not been
	 * here before.
	 */
	public static final int STATUS_BRIEF = 1;

	/**
	 * Never show the long descriptions for places unless LOOKING is set.
	 */
	public static final int STATUS_FAST = 2;

	/**
	 * Show the full description for a place.
	 */
	public static final int STATUS_LOOKING = 3;

	/**
	 * Specifies that the interpreter should attempt to combine the previous and
	 * next INPUT.
	 */
	public static final int STATUS_PLSCLRFY = 4;

	/**
	 * Set on STATUS if the player picked up or dropped anything since this bit
	 * was cleared.
	 */
	public static final int STATUS_JUGGLED = 5;

	/**
	 * Set on a PLACE if the player has been here before.
	 */
	public static final int PLACE_BEEN = 1;

	/**
	 * Set on an OBJECT if it appears in the location and the location with ID -
	 * 1.
	 */
	public static final int OBJECT_DUAL = 3;

	/**
	 * Set on ARG1 or ARG2 to indicate that it holds an object. Set on STATUS to
	 * indicate that the first word was an OBJECT. Set on all OBJECTs.
	 */
	public static final int XOBJECT = 15;
	
	/**
	 * Set on ARG1 or ARG2 to indicate that it holds an object. Set on STATUS to
	 * indicate that the first word was a VERB. Set on all VERBs.
	 */
	public static final int XVERB = 14;

	/**
	 * Set on ARG1 or ARG2 to indicate that it holds an object. Set on STATUS to
	 * indicate that the first word was an PLACE. Set on all PLACEes.
	 */
	public static final int XPLACE = 13;
	
	/**
	 * Flag/value set when input is considered an unknown word.
	 */
	public static final int ARG_BADWORD = 12;

	/**
	 * Special entity ID of the badword. 
	 */
	public static final int ARG_VALUE_BADWORD = 9999;

	/**
	 * The ID of an object's location if it's being held.
	 */
	public static final int OBJECT_LOCATION_INHAND = -1;
}
