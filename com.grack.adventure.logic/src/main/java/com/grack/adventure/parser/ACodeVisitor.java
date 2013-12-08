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

public interface ACodeVisitor {
	void visitAction(ActionDirective action);

	void visitAt(AtDirective at);

	void visitDefine(DefineDirective define);

	void visitInitial(InitialDirective initial);

	void visitLabel(LabelDirective label);

	void visitNullWord(NullDirective null_);

	void visitObject(ObjectDirective object);

	void visitPlace(PlaceDirective place);

	void visitRepeat(RepeatDirective repeat);

	void visitSynonym(SynonymDirective synonym);

	void visitText(TextDirective text);

	void visitVariable(VariableDirective variable);

	void visitVerb(VerbDirective verb);
}
