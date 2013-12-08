package com.grack.adventure.parser;

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

public class PrintVisitor implements ACodeVisitor {

	public void visitAction(ActionDirective action) {
		System.out.println(action);
	}

	public void visitAt(AtDirective at) {
		System.out.println(at);
	}

	public void visitDefine(DefineDirective define) {
		System.out.println(define);
	}

	public void visitInitial(InitialDirective initial) {
		System.out.println(initial);
	}

	public void visitLabel(LabelDirective label) {
		System.out.println(label);
	}

	public void visitNullWord(NullDirective null_) {
		System.out.println(null_);
	}

	public void visitObject(ObjectDirective object) {
		System.out.println(object);
	}

	public void visitPlace(PlaceDirective place) {
		System.out.println(place);
	}

	public void visitRepeat(RepeatDirective repeat) {
		System.out.println(repeat);
	}

	public void visitSynonym(SynonymDirective synonym) {
		System.out.println(synonym);
	}

	public void visitText(TextDirective text) {
		System.out.println(text);
	}

	public void visitVariable(VariableDirective variable) {
		System.out.println(variable);
	}

	public void visitVerb(VerbDirective verb) {
		System.out.println(verb);
	}
}
