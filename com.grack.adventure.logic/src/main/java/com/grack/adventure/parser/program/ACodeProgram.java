package com.grack.adventure.parser.program;

import java.util.List;

import com.google.common.collect.Lists;
import com.grack.adventure.parser.ACodeVisitor;

public class ACodeProgram implements ACodeVisitor {
	private List<ActionDirective> actions = Lists.newArrayList();
	private List<AtDirective> ats = Lists.newArrayList();
	private List<DefineDirective> defines = Lists.newArrayList();
	private List<InitialDirective> initials = Lists.newArrayList();
	private List<LabelDirective> labels = Lists.newArrayList();
	private List<NullDirective> nulls = Lists.newArrayList();
	private List<ObjectDirective> objects = Lists.newArrayList();
	private List<PlaceDirective> places = Lists.newArrayList();
	private List<RepeatDirective> repeats = Lists.newArrayList();
	private List<SynonymDirective> synonyms = Lists.newArrayList();
	private List<TextDirective> texts = Lists.newArrayList();
	private List<VariableDirective> variables = Lists.newArrayList();
	private List<VerbDirective> verbs = Lists.newArrayList();

	public void visitAction(ActionDirective action) {
		actions.add(action);
	}

	public List<ActionDirective> getActions() {
		return actions;
	}

	public void visitAt(AtDirective at) {
		ats.add(at);
	}

	public void visitDefine(DefineDirective define) {
		defines.add(define);
	}

	public void visitInitial(InitialDirective initial) {
		initials.add(initial);
	}

	public void visitLabel(LabelDirective label) {
		labels.add(label);
	}

	public void visitNullWord(NullDirective null_) {
		nulls.add(null_);
	}

	public void visitObject(ObjectDirective object) {
		objects.add(object);
	}

	public void visitPlace(PlaceDirective place) {
		places.add(place);
	}

	public void visitRepeat(RepeatDirective repeat) {
		repeats.add(repeat);
	}

	public void visitSynonym(SynonymDirective synonym) {
		synonyms.add(synonym);
	}

	public void visitText(TextDirective text) {
		texts.add(text);
	}

	public void visitVariable(VariableDirective variable) {
		variables.add(variable);
	}

	public void visitVerb(VerbDirective verb) {
		verbs.add(verb);
	}

	public List<AtDirective> getAts() {
		return ats;
	}

	public List<DefineDirective> getDefines() {
		return defines;
	}

	public List<InitialDirective> getInitials() {
		return initials;
	}

	public List<LabelDirective> getLabels() {
		return labels;
	}

	public List<NullDirective> getNulls() {
		return nulls;
	}

	public List<ObjectDirective> getObjects() {
		return objects;
	}

	public List<PlaceDirective> getPlaces() {
		return places;
	}

	public List<RepeatDirective> getRepeats() {
		return repeats;
	}

	public List<SynonymDirective> getSynonyms() {
		return synonyms;
	}

	public List<TextDirective> getTexts() {
		return texts;
	}

	public List<VariableDirective> getVariables() {
		return variables;
	}

	public List<VerbDirective> getVerbs() {
		return verbs;
	}
}
