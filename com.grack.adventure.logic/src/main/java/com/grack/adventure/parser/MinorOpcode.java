package com.grack.adventure.parser;

import static com.grack.adventure.parser.MinorOpcodeType.BLOCK_END;
import static com.grack.adventure.parser.MinorOpcodeType.CHAIN;
import static com.grack.adventure.parser.MinorOpcodeType.CONDITIONAL;
import static com.grack.adventure.parser.MinorOpcodeType.ELSE_BLOCK;
import static com.grack.adventure.parser.MinorOpcodeType.ITERATOR;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * Minor opcodes for the {@link MajorOpcode}s that specify code.
 */
public enum MinorOpcode {
	// ********************************************************
	// Conditionals
	// ********************************************************

	BIT(CONDITIONAL),

	IFEQ(CONDITIONAL), IFLT(CONDITIONAL), IFGT(CONDITIONAL),

	IFAT(CONDITIONAL), IFLOC(CONDITIONAL), IFNEAR(CONDITIONAL), IFHAVE(CONDITIONAL), IFKEY(CONDITIONAL),

	CHANCE(CONDITIONAL),

	/**
	 * Prompts the user for a yes/no answer, displaying the given text.
	 * 
	 * QUERY text-name
	 */
	QUERY(CONDITIONAL),

	// ********************************************************
	// Iterators
	// ********************************************************

	/**
	 * Iterates over all objects.
	 * 
	 * ITOBJ variable-name
	 */
	ITOBJ(ITERATOR, "ITLIST"),

	/**
	 * Iterates over all places.
	 * 
	 * ITPLACE variable-name
	 */
	ITPLACE(ITERATOR),

	// ********************************************************
	// Block ends
	// ********************************************************

	/**
	 * Delimits the blocks that are executed when the conditionals succeed and
	 * fail.
	 */
	ELSE(ELSE_BLOCK),

	/**
	 * Ends an IF block.
	 */
	FIN(BLOCK_END),

	/**
	 * Ends an iterator block.
	 */
	EOI(BLOCK_END),

	/**
	 * Ends all outstanding blocks and loops.
	 */
	EOF(BLOCK_END),

	// ********************************************************
	// Quick tests
	// ********************************************************

	/**
	 * If all the given keywords appear in the input, continue, otherwise
	 * PROCEED.
	 * 
	 * KEYWORD entity-name, ...
	 */
	KEYWORD,

	/**
	 * If the given object is in the user's hand or nearby, continue, otherwise
	 * PROCEED.
	 * 
	 * NEAR object-name
	 */
	NEAR,

	/**
	 * If the user typed any of the given keywords, continue, otherwise PROCEED.
	 * Note: multiple ANYOF lines are merged into a single line.
	 * 
	 * ANYOF object-name, ...
	 */
	ANYOF,

	/**
	 * If the given object is in the user's hand, continue, otherwise PROCEED.
	 * 
	 * NEAR object-name
	 */
	HAVE,

	/**
	 * If the user at any of the given places, continue, otherwise PROCEED.
	 * 
	 * AT place-name, ...
	 */
	AT,

	// ********************************************************
	// Conditional chains
	// ********************************************************

	OR(CHAIN), AND(CHAIN), EOR(CHAIN), NOT(CHAIN),

	// ********************************************************
	// Flag manipulation
	// ********************************************************

	/**
	 * Clears the given flag on an entity.
	 * 
	 * BIC entity-name value
	 */
	BIC,

	/**
	 * Sets the given flag on an entity.
	 * 
	 * BIS entity-name value
	 */
	BIS,

	// ********************************************************
	// Variable/value manipulation
	// ********************************************************

	/**
	 * Sets the value of a variable to an entity reference, rather than to the
	 * value of the entity.
	 * 
	 * LDA variable entity
	 */
	LDA,

	/**
	 * Sets the value of a variable to a reference to the location of the object
	 * in the second argument.
	 * 
	 * LOCATE variable object-entity*
	 */
	LOCATE,

	/**
	 * Sets the value of a variable to the value of an object indirected to by
	 * the second argument.
	 * 
	 * EVAL variable entity
	 */
	EVAL,

	DEPOSIT,

	SET,

	ADD,

	SUB,

	MULT,

	DIVIDE,

	/**
	 * Sets the value of a variable to the value of an entity or value.
	 * 
	 * RANDOM variable entity|value
	 */
	RANDOM,

	// ********************************************************
	// Flow control
	// ********************************************************

	/**
	 * Calls a procedure by a given name. If entity is a PLACE, calls the
	 * appropriate AT block. If entity is a VERB, calls the appropriate ACTION
	 * block.
	 * 
	 * CALL entity
	 */
	CALL,

	/**
	 * Returns from the current procedure.
	 * 
	 * PROCEED
	 */
	PROCEED,

	/**
	 * Re-starts the REPEAT procedures.
	 * 
	 * QUIT
	 */
	QUIT,

	/**
	 * Stops the game.
	 * 
	 * STOP
	 */
	STOP,

	// ********************************************************
	// Text input/output
	// ********************************************************

	/**
	 * Displays the text for the given entity, optionally using the second
	 * argument as either a text switch lookup or parameter (replacing # with
	 * the name). Technically Dave Platt's FORTRAN source splits this into two
	 * opcodes, but we can effectively combine them.
	 * 
	 * Note that calling NAME with the special arg1 or arg2 variables will print
	 * out the contents of the user's token rather than what arg1 or arg2 points
	 * at.
	 * 
	 * SAY entity-name entity-name|value
	 */
	SAY("NAME"),

	/**
	 * Like SAY, but replaces # with the value of the entity instead of the
	 * name. If the second parameter is an object entity, outputs the value of
	 * the object, plus one.
	 * 
	 * VALUE entity-name entity|value
	 */
	VALUE,

	/**
	 * Reads input from the player, setting ARG1 and ARG2 to values read (and
	 * the bits of ARG1/ARG2 to the special OBJECT/PLACE/VERB/BADWORD bits).
	 * Sets the value of STATUS to the number of words read.
	 * 
	 * All words must either be VERBs, OBJECTs, or be DEFINEd.
	 * 
	 * INPUT
	 */
	INPUT,

	/**
	 * If no object was specified by the user (ie: only a single piece of input
	 * was provided), sets the object to the first object here matching the
	 * given flag.
	 * 
	 * If no object was found, sets ARG2 to -1. If more than one matching object
	 * is found, sets ARG2 to -2.
	 * 
	 * DEFAULT flag
	 */
	DEFAULT,

	// ********************************************************
	// Object/place manipulation
	// ********************************************************

	/**
	 * Moves the given object to the given place.
	 * 
	 * APPORT object* place*
	 */
	APPORT,

	/**
	 * Moves the given object INHAND. Sets the JUGGLED flag on STATUS.
	 * 
	 * GET object*
	 */
	GET,

	/**
	 * Moves the given object to HERE. Sets the JUGGLED flag on STATUS.
	 * 
	 * DROP object*
	 */
	DROP,

	/**
	 * Transports the user to the given place. Sets the MOVED flag on STATUS.
	 * 
	 * GOTO place*
	 */
	GOTO,

	/**
	 * If the user typed _word_ as the first word of the command, or typed a
	 * synonym of EXPLORE and _word_ as the second word of the command, GOTO the
	 * place, then QUIT.
	 * 
	 * MOVE word place*
	 */
	MOVE,

	/**
	 * If the user typed _word_ as the first word of the command, or typed a
	 * synonym of EXPLORE and _word_ as the second word of the command, SAY
	 * text, GOTO the place, then QUIT.
	 * 
	 * SMOVE word place* text*
	 */
	SMOVE,

	// ********************************************************
	// System commands
	// ********************************************************

	/**
	 * Sets the value of the variable to a given system parameter (4 = hours, 5
	 * = minutes).
	 * 
	 * SVAR value variable-name
	 */
	SVAR,

	/**
	 * Runs a system command. Current commands (from the FORTRAN source) are:
	 * 
	 * 1 - dump current state of game to disc.
	 * 
	 * 2 - restore dumped game.
	 * 
	 * 3 - delete dumped game. (This only happens after a restore)
	 * 
	 * 4 - display news.
	 * 
	 * 5 - check if starting in free time.
	 * 
	 * 6 - print hours. As supplied, the hours routine says 'open at all times'
	 * 
	 * 7 - save a variable.
	 * 
	 * 8 - restore saved variable.
	 * 
	 * 9 - check demo games allowed.
	 * 
	 * EXEC value variable-name
	 */
	EXEC("EXECUTE", "EXECUTIVE"),

	;

	private MinorOpcodeType type;

	/**
	 * This lives in a static inner class because we can't refer to an enum's
	 * static initializers in the constructor.
	 */
	private static class InternalMap {
		private static final Map<String, MinorOpcode> SYNONYM_MAP = Maps.newHashMap();
	}

	private MinorOpcode(MinorOpcodeType type, String... synonyms) {
		this.type = type;
		InternalMap.SYNONYM_MAP.put(this.name(), this);
		for (String synonym : synonyms)
			InternalMap.SYNONYM_MAP.put(synonym, this);
	}

	private MinorOpcode(String... synonyms) {
		this(MinorOpcodeType.NORMAL, synonyms);
	}

	private MinorOpcode(MinorOpcodeType type) {
		this(type, new String[] {});
	}

	public MinorOpcodeType getType() {
		return type;
	}

	public static MinorOpcode lookup(String name) throws ParseException {
		MinorOpcode opcode = InternalMap.SYNONYM_MAP.get(name);
		if (opcode == null)
			throw new ParseException("Unknown opcode: " + name);
		return opcode;
	}
}
