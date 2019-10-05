package com.grack.adventure.kernel;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.grack.adventure.kernel.entity.Entity;
import com.grack.adventure.kernel.world.World;

/**
 * Encompasses the entirety of the virtual machine state needed to re-create a
 * VM at any given time.
 * 
 * TODO: Move badword from {@link World} over here.
 */
public class VirtualMachine {
	private static final Splitter COMMA_SPLITTER = Splitter.on(',');

	private Stack<StackFrame> executionStack = new Stack<StackFrame>();
	private Stack<ActiveIterator> iteratorStack = new Stack<ActiveIterator>();
	private boolean lastWasConditional;
	private boolean conditionalSuccess;
	private InterpreterState interpreterState = InterpreterState.RUNNING;
	private Map<Integer, Integer> entityValues = Maps.newHashMap();
	private Map<Integer, Integer> entityFlags = Maps.newHashMap();
	private Map<Integer, Integer> entityLocations = Maps.newHashMap();

	private int executiveSavedValue;

	private List<Entity> input = Lists.newArrayList();
	private String arg1Token, arg2Token;

	public Stack<StackFrame> getExecutionStack() {
		return executionStack;
	}

	public Stack<ActiveIterator> getIteratorStack() {
		return iteratorStack;
	}

	public String getArg1Token() {
		return arg1Token;
	}

	public void setArg1Token(String arg1Token) {
		this.arg1Token = arg1Token;
	}

	public String getArg2Token() {
		return arg2Token;
	}

	public void setArg2Token(String arg2Token) {
		this.arg2Token = arg2Token;
	}

	public boolean isConditionalSuccess() {
		return conditionalSuccess;
	}

	public boolean isLastWasConditional() {
		return lastWasConditional;
	}

	public void setConditionalSuccess(boolean conditionalSuccess) {
		this.conditionalSuccess = conditionalSuccess;
	}

	public void setLastWasConditional(boolean lastWasConditional) {
		this.lastWasConditional = lastWasConditional;
	}

	public List<Entity> getInput() {
		return input;
	}

	public void setInterpreterState(InterpreterState interpreterState) {
		this.interpreterState = interpreterState;
	}

	public InterpreterState getInterpreterState() {
		return interpreterState;
	}

	public int getExecutiveSavedValue() {
		return executiveSavedValue;
	}

	public void setExecutiveSavedValue(int executiveSavedValue) {
		this.executiveSavedValue = executiveSavedValue;
	}

	public int getEntityValue(Entity e) {
		Integer value = entityValues.get(e.getId());
		return (value == null) ? 0 : value;
	}

	public void setEntityValue(Entity e, int value) {
		entityValues.put(e.getId(), value);
	}

	public int getEntityLocation(Entity e) {
		Integer value = entityLocations.get(e.getId());
		return (value == null) ? 0 : value;
	}

	public void setEntityLocation(Entity e, int value) {
		entityLocations.put(e.getId(), value);
	}

	public int getFlags(Entity e) {
		Integer flags = entityFlags.get(e.getId());
		return (flags == null) ? e.getDefaultFlags() : flags;
	}

	public void setFlags(Entity e, int flags) {
		entityFlags.put(e.getId(), flags);
	}

	public Map<String, String> saveInternal() {
		Map<String, String> internalState = Maps.newHashMap();
		internalState.put("0", saveStack(executionStack));
		internalState.put("1", saveStack(iteratorStack));
		internalState.put("2", "" + executiveSavedValue);
		internalState.put("3", "" + interpreterState.ordinal());
		internalState.put("4", conditionalSuccess ? "1" : "0");
		internalState.put("5", lastWasConditional ? "1" : "0");
		internalState.put("6", arg1Token);
		internalState.put("7", arg2Token);
		
		return internalState;
	}
	
	private String saveStack(Stack<? extends SaveAware> stack) {
		
		return null;
	}

	public Map<String, String> save() {
		Set<Integer> keys = Sets.newHashSet();
		keys.addAll(entityFlags.keySet());
		keys.addAll(entityValues.keySet());
		keys.addAll(entityLocations.keySet());
		// TODO: Sort?
//		List<Integer> sortedKeys = Lists.newArrayList(keys);
//		Collections.sort(sortedKeys);
		Map<String, String> save = Maps.newHashMap();
		for (int key : keys) {
			// Don't serialize this
			if (key == Constants.ARG_VALUE_BADWORD)
				continue;
			
			String value = saveInteger(entityFlags.get(key)) + "," + saveInteger(entityValues.get(key));
			if (entityLocations.containsKey(key))
				value += "," + saveInteger(entityLocations.get(key));
			// Hokey test for all zeros
			if (!value.equals("0,0") && !value.equals("0,0,0"))
				save.put("" + (key - Entity.ENTITY_ID_START), value);
		}
		return save;
	}

	private String saveInteger(Integer i) {
		if (i == null)
			return "0";

		// Try to save a shorter integer if it's negative (and would benefit
		// from a shorter string)
		if (i < 0 && i > -0x10000000)
			return "-" + Integer.toHexString(-i);

		return Integer.toHexString(i);
	}

	public void restore(Map<String, String> values) {
		entityFlags.clear();
		entityValues.clear();
		entityLocations.clear();
		
		for (String keyString : values.keySet()) {
			int key = Integer.parseInt(keyString) + Entity.ENTITY_ID_START;
			String vals = values.get(keyString);
			Iterator<String> it = (vals == null) ?  Collections.<String> emptyIterator() : COMMA_SPLITTER.split(vals).iterator();

			if (!it.hasNext())
				continue;
			entityFlags.put(key, (int) Long.parseLong(it.next(), 16));

			if (!it.hasNext())
				continue;
			entityValues.put(key, (int) Long.parseLong(it.next(), 16));

			if (!it.hasNext())
				continue;
			entityLocations.put(key, (int) Long.parseLong(it.next(), 16));
		}
	}

	public VirtualMachine clone() {
		VirtualMachine clone = new VirtualMachine();
		clone.arg1Token = arg1Token;
		clone.arg2Token = arg2Token;
		clone.conditionalSuccess = conditionalSuccess;
		clone.lastWasConditional = lastWasConditional;
		clone.entityFlags = Maps.newHashMap(entityFlags);
		clone.entityLocations = Maps.newHashMap(entityLocations);
		clone.entityValues = Maps.newHashMap(entityValues);
		clone.input = Lists.newArrayList(input);
		clone.interpreterState = interpreterState;
		clone.executiveSavedValue = executiveSavedValue;
		
		// TODO
		clone.executionStack = new Stack<StackFrame>();
		clone.iteratorStack = new Stack<ActiveIterator>();
		
		return clone;
	}

	public void clearExecutionStack() {
		lastWasConditional = conditionalSuccess = false;
		executiveSavedValue = 0;
		executionStack.clear();
		iteratorStack.clear();
		interpreterState = InterpreterState.RUNNING;
	}
}
