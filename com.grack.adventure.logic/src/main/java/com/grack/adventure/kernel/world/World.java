package com.grack.adventure.kernel.world;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Predicate;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.grack.adventure.kernel.Constants;
import com.grack.adventure.kernel.IdResolver;
import com.grack.adventure.kernel.Procedure;
import com.grack.adventure.kernel.Program;
import com.grack.adventure.kernel.VirtualMachine;
import com.grack.adventure.kernel.entity.Entity;
import com.grack.adventure.kernel.entity.ObjectEntity;
import com.grack.adventure.kernel.entity.PlaceEntity;
import com.grack.adventure.kernel.entity.TextEntity;
import com.grack.adventure.kernel.entity.VariableEntity;
import com.grack.adventure.util.RestartableTypeFilteringListIterable;

/**
 * Manages the current states of the game by keeping track of the set of
 * {@link Entity} objects, {@link Procedure}s, and vocabulary.
 */
public class World {
	private Set<String> nullWords = Sets.newHashSet();

	private List<Entity> entities = Lists.newArrayList();

	private List<Procedure> initialProcedures = Lists.newArrayList();
	private List<Procedure> repeatProcedures = Lists.newArrayList();
	private Multimap<String, Procedure> labelProcedures = ArrayListMultimap.create();
	private Map<String, Entity> vocabulary = Maps.newHashMap();

	private final IdResolver resolver;

	private TextEntity badword;

	private Program program = new Program();
	
	public World(IdResolver resolver) {
		this.resolver = resolver;
	}
	
	public Program getProgram() {
		return program;
	}

	/**
	 * TODO: Move this to VirtualMachine.
	 */
	public TextEntity getBadWord(VirtualMachine vm, String text) {
		badword = new TextEntity(text, text);
		badword.setId(Constants.ARG_VALUE_BADWORD);
		badword.setFlags(vm, 1 << Constants.ARG_BADWORD);
		return badword;
	}

	public Entity getEntityById(int index) {
		if (index == Constants.ARG_VALUE_BADWORD)
			return badword;
		if (!Entity.isEntityId(index))
			return null;
		index = index - Entity.ENTITY_ID_START;
		if (index > entities.size() - 1)
			return null;
		return entities.get(index);
	}

	public Map<String, Entity> getVocabulary() {
		return vocabulary;
	}

	public Multimap<String, Procedure> getLabelProcedures() {
		return labelProcedures;
	}

	public List<Procedure> getInitialProcedures() {
		return initialProcedures;
	}

	public List<Procedure> getRepeatProcedures() {
		return repeatProcedures;
	}

	public VariableEntity getVariableByName(String name) {
		return (VariableEntity) getEntityById(resolver.getEntityIndex(name));
	}

	public Entity getEntityByName(String name) {
		return getEntityById(resolver.getEntityIndex(name));
	}

	public RestartableTypeFilteringListIterable<Entity, ObjectEntity> getObjectEntities() {
		return new RestartableTypeFilteringListIterable<Entity, ObjectEntity>(entities, new Predicate<Entity>() {
			public boolean apply(Entity input) {
				return input instanceof ObjectEntity;
			}
		});
	}

	public RestartableTypeFilteringListIterable<Entity, PlaceEntity> getPlaceEntities() {
		return new RestartableTypeFilteringListIterable<Entity, PlaceEntity>(entities, new Predicate<Entity>() {
			public boolean apply(Entity input) {
				return input instanceof PlaceEntity;
			}
		});
	}

	int addEntity(Entity entity) {
		entities.add(entity);
		return entities.size() - 1;
	}

	void addNullWord(String word) {
		nullWords.add(word);
	}
}
