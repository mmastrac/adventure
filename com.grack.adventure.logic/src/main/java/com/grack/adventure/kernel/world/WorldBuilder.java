package com.grack.adventure.kernel.world;

import java.util.List;

import com.grack.adventure.kernel.IdResolver;
import com.grack.adventure.kernel.ProcedureBuilder;
import com.grack.adventure.kernel.entity.Entity;
import com.grack.adventure.kernel.entity.LabelEntity;
import com.grack.adventure.kernel.entity.ObjectEntity;
import com.grack.adventure.kernel.entity.PlaceEntity;
import com.grack.adventure.kernel.entity.TextEntity;
import com.grack.adventure.kernel.entity.VariableEntity;
import com.grack.adventure.kernel.entity.VerbEntity;
import com.grack.adventure.parser.SourceLine;
import com.grack.adventure.parser.MinorOpcode;
import com.grack.adventure.parser.ParseException;
import com.grack.adventure.parser.program.ACodeProgram;
import com.grack.adventure.parser.program.ActionDirective;
import com.grack.adventure.parser.program.AtDirective;
import com.grack.adventure.parser.program.DefineDirective;
import com.grack.adventure.parser.program.InitialDirective;
import com.grack.adventure.parser.program.LabelDirective;
import com.grack.adventure.parser.program.NullDirective;
import com.grack.adventure.parser.program.ObjectDirective;
import com.grack.adventure.parser.program.PlaceDirective;
import com.grack.adventure.parser.program.RepeatDirective;
import com.grack.adventure.parser.program.SynonymDirective;
import com.grack.adventure.parser.program.TextDirective;
import com.grack.adventure.parser.program.VariableDirective;
import com.grack.adventure.parser.program.VerbDirective;

/**
 * Builds a {@link World} from an {@link ACodeProgram}.
 */
public class WorldBuilder {
	private World state;

	private IdResolver resolver;

	public WorldBuilder(ACodeProgram program) throws ParseException {
		resolver = new IdResolver();
		state = new World(resolver);

		// Allocate ids for the various entities
		
		// Variables
		for (VariableDirective variable : program.getVariables())
			allocate(variable.getName(), new VariableEntity(variable.getName()));
		
		// Text
		for (TextDirective text : program.getTexts())
			allocate(text.getId(), new TextEntity(text.getId(), text.getText()));
		
		// Objects
		for (ObjectDirective object : program.getObjects()) {
			ObjectEntity e = new ObjectEntity(object.getName(), object.getDesc(), object.getStates());
			allocate(object.getName(), e);
			state.getVocabulary().put(object.getName().toLowerCase(), e);
		}
		
		// Places
		for (PlaceDirective place : program.getPlaces())
			allocate(place.getName(), new PlaceEntity(place.getName(), place.getShortDesc(), place.getStates()));
		
		// Verbs
		for (VerbDirective verb : program.getVerbs()) {
			VerbEntity e = new VerbEntity(verb.getName());
			allocate(verb.getName(), e);
			for (String synonym : verb.getSynonyms()) {
				resolver.addSynonym(synonym, verb.getName());
				state.getVocabulary().put(synonym.toLowerCase(), e);
			}
			state.getVocabulary().put(verb.getName().toLowerCase(), e);
		}

		// Synonyms
		for (SynonymDirective synonym : program.getSynonyms()) {
			if (synonym.getWord().matches("[0-9]+")) {
				for (String synonymName : synonym.getSynonyms())
					resolver.addConstant(synonymName, Integer.parseInt(synonym.getWord()));
			} else {
				for (String synonymName : synonym.getSynonyms()) {
					resolver.addSynonym(synonymName, synonym.getWord());
					state.getVocabulary().put(synonymName.toLowerCase(), state.getEntityByName(synonym.getWord()));
				}
			}
		}

		// Add the code blocks
		
		// Labels
		for (LabelDirective label : program.getLabels()) {
			state.getLabelProcedures().put(label.getName(), ProcedureBuilder.build(state, resolver, label.getCode()));
			allocate(label.getName(), new LabelEntity(label.getName()));
		}
		
		// "AT"
		for (AtDirective at : program.getAts())
			state.getLabelProcedures().put(at.getPlace(), ProcedureBuilder.build(state, resolver, at.getCode()));
		
		// "ACTION"
		for (ActionDirective action : program.getActions()) {
			String actionName = action.getName();
			List<SourceLine> code = action.getCode();

			// If this is a complex action, convert it to a regular action with
			// ANYOF
			if (action.getKeywords().size() > 0) {
				code.add(0, new SourceLine(MinorOpcode.ANYOF, action.getKeywords()));
			}

			// Make sure we look up the root verb/object
			Entity entity = (Entity) state.getEntityById(resolver.getEntityIndex(actionName));
			state.getLabelProcedures().put(entity.getName(), ProcedureBuilder.build(state, resolver, code));
		}
		
		// "INITIAL"
		for (InitialDirective initial : program.getInitials())
			state.getInitialProcedures().add(ProcedureBuilder.build(state, resolver, initial.getCode()));
		
		// "REPEAT"
		for (RepeatDirective repeat : program.getRepeats())
			state.getRepeatProcedures().add(ProcedureBuilder.build(state, resolver, repeat.getCode()));

		// And finally, the declarations for the parser

		// Parse the null words
		for (NullDirective nullWord : program.getNulls())
			state.addNullWord(nullWord.getWord());

		// Vocabulary definitions 
		for (DefineDirective define : program.getDefines())
			state.getVocabulary().put(define.getName().toLowerCase(), state.getEntityByName(define.getName()));
	}

	public World getState() {
		return state;
	}

	private void allocate(String name, Entity entity) {
		int index = state.addEntity(entity);
		entity.setId(index + Entity.ENTITY_ID_START);

		if (name != null) {
			resolver.addEntity(name, entity.getStateCount(), index + Entity.ENTITY_ID_START);
		}
	}
}
