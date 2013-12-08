package com.grack.adventure.kernel;

import java.util.List;

import com.google.common.collect.Lists;
import com.grack.adventure.kernel.entity.Entity;
import com.grack.adventure.parser.MinorOpcode;
import com.grack.adventure.util.RestartableTypeFilteringListIterator;

/**
 * Tracks an active {@link MinorOpcode#ITOBJ} or {@link MinorOpcode#ITPLACE}
 * iterator.
 */
public class ActiveIterator implements SaveAware {
	private int opcode;
	private final int variableId;
	private RestartableTypeFilteringListIterator<Entity, ? extends Entity> iterator;

	public ActiveIterator(int variableId, int opcode, RestartableTypeFilteringListIterator<Entity, ? extends Entity> entities) {
		this.variableId = variableId;
		this.opcode = opcode;
		this.iterator = entities;
	}

	public RestartableTypeFilteringListIterator<Entity, ? extends Entity> getIterator() {
		return iterator;
	}

	public int getOpcode() {
		return opcode;
	}

	public int getVariableId() {
		return variableId;
	}
	
	public List<String> save() {
		List<String> list = Lists.newArrayList();
		list.add("" + opcode);
		list.add("" + variableId);
		list.add("" + iterator.getNextIndex());
		return list;
	}
}
