package com.grack.adventure.kernel;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.grack.adventure.kernel.entity.Entity;
import com.grack.adventure.kernel.entity.LabelEntity;
import com.grack.adventure.kernel.entity.ObjectEntity;
import com.grack.adventure.kernel.entity.PlaceEntity;
import com.grack.adventure.kernel.entity.TextEntity;
import com.grack.adventure.kernel.entity.VariableEntity;
import com.grack.adventure.kernel.entity.VerbEntity;
import com.grack.adventure.kernel.world.World;
import com.grack.adventure.parser.MinorOpcode;
import com.grack.adventure.parser.MinorOpcodeType;
import com.grack.adventure.parser.ParseException;
import com.grack.adventure.util.RestartableTypeFilteringListIterator;

/**
 * Interprets a list of parsed opcodes.
 */
public class Interpreter {
	private static final Splitter INPUT_SPLITTER = Splitter.on(' ').trimResults().omitEmptyStrings();
	
	private final World world;
	private final HostEnvironment hostEnvironment;

	private boolean allowRestart = true;

	private CompiledLine currentOpcode;

	private VariableEntity here, there, status, arg1, arg2;
	private Entity explore;

	private VirtualMachine vm;
	
	public Interpreter(HostEnvironment hostEnvironment, World world, VirtualMachine vm) {
		this.hostEnvironment = hostEnvironment;
		this.world = world;
		this.vm = vm;

		pushRepeat();
		pushInitial();

		// Grab the stock variables right away
		here = world.getVariableByName("HERE");
		there = world.getVariableByName("THERE");
		status = world.getVariableByName("STATUS");
		arg1 = world.getVariableByName("ARG1");
		arg2 = world.getVariableByName("ARG2");

		explore = world.getEntityByName("EXPLORE");
	}
	
	public VirtualMachine getVm() {
		return vm;
	}

	private void pushInitial() {
		for (int i = world.getInitialProcedures().size() - 1; i >= 0; i--)
			vm.getExecutionStack().add(new StackFrame(world.getInitialProcedures().get(i)));
	}

	private void pushRepeat() {
		for (int i = world.getRepeatProcedures().size() - 1; i >= 0; i--)
			vm.getExecutionStack().add(new StackFrame(world.getRepeatProcedures().get(i)));
	}

	public StackFrame currentFrame() {
		return vm.getExecutionStack().peek();
	}

	/**
	 * Pushes a call to a label on the stack.
	 */
	public void callLabel(String label) {
		LabelEntity e = (LabelEntity) world.getEntityByName(label);
		call(e);
	}
	
	public void setAllowRestart(boolean allowRestart) {
		this.allowRestart = allowRestart;
	}

	public boolean getAllowRestart() {
		return allowRestart;
	}

	public InterpreterState step() throws ParseException {
		// If we have a pending input() or query(), process it now
		switch (vm.getInterpreterState()) {
		case WAITING_FOR_INPUT:
			input();
			break;
		case WAITING_FOR_QUERY:
			query(null);
			break;
		default:
			break;
		}

		if (vm.getInterpreterState() != InterpreterState.RUNNING)
			return vm.getInterpreterState();

		while (currentFrame().getInstructionPointer() >= currentFrame().getEnd()) {
			trace("End of function");
			vm.getExecutionStack().pop();

			if (vm.getExecutionStack().isEmpty()) {
				if (!restartRepeat()) {
					vm.setInterpreterState(InterpreterState.COMPLETED);
					return vm.getInterpreterState();
				}
			}
		}

		currentOpcode = getCurrentParsedOpcode();

		MinorOpcode op = currentOpcode.getOpcode();

		switch (op.getType()) {
		case CONDITIONAL:
			if (currentOpcode.getBooleanOp() != null) {
				// chained
				break;
			}
			//$FALL-THROUGH$
		case ELSE_BLOCK:
		case ITERATOR:
		case NORMAL: {
			if (vm.isLastWasConditional()) {
				if (vm.isConditionalSuccess()) {
					// Was a success
				} else {
					// Was a failure
					skipBlock();
					vm.setLastWasConditional(false);
					return InterpreterState.RUNNING;
				}
			}
			break;
		}
		case BLOCK_END:
			break;
		case CHAIN:
			// These are merged into the ParsedOpcode
			error("Unexpected opcode type: " + op.getType());
			break;
		}

		// Step the current frame
		currentFrame().step();

		trace("" + currentOpcode + " (" + op.getType() + ")");

		vm.setLastWasConditional(false);
		switch (op) {
		case BIC:
			bic(resolveEntityIndirect(currentOpcode.getArg(0)), resolveValue(currentOpcode.getArg(1)));
			break;
		case BIS:
			bis(resolveEntityIndirect(currentOpcode.getArg(0)), resolveValue(currentOpcode.getArg(1)));
			break;
		case BIT:
			bit(resolveEntityIndirect(currentOpcode.getArg(0)), resolveValue(currentOpcode.getArg(1)));
			break;
		case SAY:
			String name = null;
			int value = 0;
			if (currentOpcode.getArgs().size() > 1) {
				/**
				 * The FORTRAN interpreter special-cases arg1 and arg2 - they
				 * always print out the token typed by the user, rather than the
				 * object they point to.
				 */
				Entity entity = resolveEntityIndirect(currentOpcode.getArg(1));
				value = entity.getIntValue(vm);
				if (currentOpcode.getArg(1) == arg1) {
					name = vm.getArg1Token();
				} else if (currentOpcode.getArg(1) == arg2) {
					name = vm.getArg2Token();
				} else {
					name = entity.getName();
				}
			}
			say(resolveEntityIndirect(currentOpcode.getArg(0)), name, value);
			break;
		case VALUE:
			value(resolveEntityIndirect(currentOpcode.getArg(0)), currentOpcode.getArgs().get(1));
			break;
		case ITPLACE:
			itplace(resolveVariable(currentOpcode.getArg(0)));
			break;
		case ITOBJ:
			itobj(resolveVariable(currentOpcode.getArg(0)));
			break;
		case APPORT:
			apport(resolveObject(currentOpcode.getArg(0)), resolvePlace(currentOpcode.getArg(1)));
			break;
		case DROP:
			drop(resolveObject(currentOpcode.getArg(0)));
			break;
		case EOI:
			eoi();
			break;
		case RANDOM:
			random(resolveEntity(currentOpcode.getArg(0)), resolveValue(currentOpcode.getArg(1)));
			break;
		case SET:
			set(resolveEntity(currentOpcode.getArg(0)), resolveValue(currentOpcode.getArg(1)));
			break;
		case ADD:
			add(resolveEntity(currentOpcode.getArg(0)), resolveValue(currentOpcode.getArg(1)));
			break;
		case SUB:
			sub(resolveEntity(currentOpcode.getArg(0)), resolveValue(currentOpcode.getArg(1)));
			break;
		case MULT:
			mult(resolveEntity(currentOpcode.getArg(0)), resolveValue(currentOpcode.getArg(1)));
			break;
		case DIVIDE:
			divide(resolveEntity(currentOpcode.getArg(0)), resolveValue(currentOpcode.getArg(1)));
			break;
		case GOTO:
			goto_(resolvePlace(currentOpcode.getArg(0)));
			break;
		case GET:
			get(resolveObject(currentOpcode.getArg(0)));
			break;
		case LDA:
			lda(resolveVariable(currentOpcode.getArg(0)), resolveEntityIdIndirect(currentOpcode.getArg(1)));
			break;
		case CHANCE:
			chance(resolveValue(currentOpcode.getArg(0)));
			break;
		case IFLOC:
			ifloc(resolveObject(currentOpcode.getArg(0)), resolvePlace(currentOpcode.getArg(1)));
			break;
		case IFNEAR:
			ifnear(resolveObject(currentOpcode.getArg(0)));
			break;
		case IFHAVE:
			ifhave(resolveObject(currentOpcode.getArg(0)));
			break;
		case IFAT:
			ifat(resolvePlace(currentOpcode.getArg(0)));
			break;
		case ELSE:
			else_();
			break;
		case FIN:
			// no-op
			break;
		case EXEC:
			exec(resolveValue(currentOpcode.getArg(0)), resolveVariable(currentOpcode.getArg(1)));
			break;
		case IFGT:
		case IFLT:
		case IFEQ:
			if_(resolveValue(currentOpcode.getArg(0)), resolveValue(currentOpcode.getArg(1)));
			break;
		case IFKEY:
			ifkey(resolveEntityIndirect(currentOpcode.getArg(0)));
			break;
		case QUIT:
			quit();
			break;
		case STOP:
			stop();
			break;
		case PROCEED:
			proceed();
			break;
		case QUERY:
			query(resolveText(currentOpcode.getArg(0)));
			break;
		case INPUT:
			input();
			break;
		case CALL:
			call(resolveEntityIndirect(currentOpcode.getArg(0)));
			break;
		case MOVE:
		case SMOVE:
			move(resolveEntity(currentOpcode.getArg(0)), resolvePlace(currentOpcode.getArg(1)),
					currentOpcode.getArgs().size() > 2 ? resolveText(currentOpcode.getArg(2)) : null);
			break;
		case DEFAULT:
			default_(resolveValue(currentOpcode.getArg(0)));
			break;
		case DEPOSIT:
			set(resolveEntityIndirect(currentOpcode.getArg(0)), resolveValue(currentOpcode.getArg(1)));
			break;
		case EVAL:
			eval(resolveEntity(currentOpcode.getArg(0)), resolveEntityIndirect(currentOpcode.getArg(1)));
			break;
		case LOCATE:
			locate(resolveVariable(currentOpcode.getArg(0)), resolveObject(currentOpcode.getArg(1)));
			break;
		case SVAR:
			svar(resolveValue(currentOpcode.getArg(0)), resolveVariable(currentOpcode.getArg(1)));
			break;
		case ANYOF:
		case KEYWORD:
		case AT:
		case NEAR:
		case HAVE:
			// These opcodes are converted to IFxxx by Procedure
			throw new RuntimeException("Unexpected opcode: " + currentOpcode.getOpcode() + " (" + currentOpcode + ")");
		case AND:
		case OR:
		case NOT:
		case EOR:
			// These opcodes are converted into flags by Procedure
			throw new RuntimeException("Unexpected opcode: " + currentOpcode.getOpcode() + " (" + currentOpcode + ")");
		case EOF:
			// This should be converted into appropriate FIN/EOI in Procedure
			throw new RuntimeException("Unexpected opcode: " + currentOpcode.getOpcode() + " (" + currentOpcode + ")");
		}

		return vm.getInterpreterState();
	}

	private void query(TextEntity text) {
		// Only speak once
		if (vm.getInterpreterState() == InterpreterState.RUNNING)
			say(text, null, 0);

		Boolean b = hostEnvironment.query();
		if (b == null) {
			vm.setInterpreterState(InterpreterState.WAITING_FOR_QUERY);
			return;
		}

		conditional(b);
		vm.setInterpreterState(InterpreterState.RUNNING);
	}

	private void locate(VariableEntity variable, ObjectEntity object) {
		variable.setIntValue(vm, object.getLocation(vm));
	}

	private void eval(Entity entity, Entity entity2) {
		entity.setIntValue(vm, entity2.getIntValue(vm));
	}

	@SuppressWarnings("deprecation")
	private void svar(int value, VariableEntity variable) {
		switch (value) {
		case 4:
			variable.setIntValue(vm, new Date().getHours());
			break;
		case 5:
			variable.setIntValue(vm, new Date().getMinutes());
			break;
		default:
			glitch("Unknown SVAR: " + value);
			break;
		}
	}

	private void drop(ObjectEntity object) {
		object.setLocation(vm, here.getIntValue(vm));
		status.setFlag(vm, Constants.STATUS_JUGGLED);
	}

	private void default_(int flag) {
		if (vm.getInput().size() != 1)
			return;

		int location = here.getIntValue(vm);
		int found = -1;
		for (ObjectEntity object : world.getObjectEntities()) {
			if (object.getLocation(vm) == location && object.hasFlag(vm, flag)) {
				if (found == -1)
					found = object.getId();
				else
					found = -2;
			}
		}
		trace("DEFAULT -> " + found);
		arg2.setIntValue(vm, found);
		Entity entity = world.getEntityById(found);
		if (entity != null) {
			vm.getInput().add(entity);
		}
	}

	private void quit() {
		restartRepeat();
	}

	private void move(Entity entity, PlaceEntity place, TextEntity text) {
		// TODO: This doesn't match the FORTRAN
		// (word1 != lineword(1) && (linelen < 2 || word1 != lineword(2) ||
		// (lineword1 != explore && lineword1 != sayxx))

		// The FORTRAN code also tests to see if the player specified "SAY",
		// which doesn't make much sense
		if (vm.getInput().get(0).getId() == entity.getId()
				|| (vm.getInput().size() > 1 && vm.getInput().get(0).getId() == explore.getId() && vm.getInput().get(1).getId() == entity.getId())) {
			goto_(place);
			if (text != null)
				say(text, null, 0);
			quit();
		}
	}

	private void ifkey(Entity entity) {
		for (Entity e : vm.getInput()) {
			if (entity.getId() == e.getId()) {
				trace("IFKEY: Found");
				conditional(true);
				return;
			}
		}

		trace("IFKEY: Not found");
		conditional(false);
	}

	private void proceed() {
		vm.getExecutionStack().pop();
		if (vm.getExecutionStack().isEmpty()) {
			if (!restartRepeat()) {
				vm.setInterpreterState(InterpreterState.COMPLETED);
			}
		}
	}

	private void call(Entity entity) {
		if (entity == null) {
			trace("No entity to call");
			return;
		}

		if (entity instanceof LabelEntity || entity instanceof VerbEntity || entity instanceof PlaceEntity
				|| entity instanceof ObjectEntity) {
			trace("Calling " + entity);
		} else {
			// Ignore calls to BADWORD or empty variables
			if (entity.getId() == Constants.ARG_VALUE_BADWORD || entity instanceof VariableEntity) {
				trace("Ignoring CALL to " + entity);
				return;
			}

			glitch("Bad CALL: " + entity);
			return;
		}

		String name = entity.getName();
		Collection<Procedure> procedures = world.getLabelProcedures().get(name);

		if (procedures.size() > 0) {
			for (Procedure proc : Lists.reverse(Lists.newArrayList(procedures)))
				vm.getExecutionStack().push(new StackFrame(proc));
		}
	}

	private void input() {
		// TODO: PLS.CLARIFY
		// TODO: null words

		String input = hostEnvironment.input();
		if (input == null) {
			trace("Waiting for input");
			vm.setInterpreterState(InterpreterState.WAITING_FOR_INPUT);
			return;
		}

		vm.setInterpreterState(InterpreterState.RUNNING);
		vm.getInput().clear();

		vm.setArg1Token("");
		vm.setArg2Token("");

		status.setIntValue(vm, 0);
		arg1.setIntValue(vm, Constants.ARG_BADWORD);
		arg1.setFlags(vm, 0);
		arg1.setFlag(vm, Constants.ARG_BADWORD);
		arg2.setIntValue(vm, Constants.ARG_BADWORD);
		arg2.setFlags(vm, 0);
		arg2.setFlag(vm, Constants.ARG_BADWORD);

		Iterator<String> words = INPUT_SPLITTER.split(input).iterator();
		if (!words.hasNext()) {
			status.setIntValue(vm, 0);
			arg1.setEntity(vm, world.getBadWord(vm, ""));
			return;
		}

		// TODO: Should we be clearing some of the status bits here?
		int statusFlags;

		status.setIntValue(vm, 1);
		String word1 = words.next().toLowerCase();
		Entity e1 = world.getVocabulary().get(word1);
		if (e1 == null) {
			// set bad word
			trace("Bad word: " + word1);
			statusFlags = 1 << Constants.ARG_BADWORD;
			arg1.setEntity(vm, world.getBadWord(vm, word1));
			vm.setArg1Token(word1);

			// Abort input parse after first bad word
			return;
		} else {
			vm.getInput().add(e1);
			vm.setArg1Token(word1);
			arg1.setEntity(vm, e1);
			statusFlags = e1.getDefaultFlags();
		}

		if (words.hasNext()) {
			status.setIntValue(vm, 2);

			String word2 = words.next().toLowerCase();
			Entity e2 = world.getVocabulary().get(word2);
			if (e2 == null) {
				// set bad word
				trace("Bad word: " + word2);
				arg2.setEntity(vm, world.getBadWord(vm, word2));
				vm.setArg2Token(word2);
			} else {
				vm.getInput().add(e2);
				arg2.setEntity(vm, e2);
				vm.setArg2Token(word2);
			}

			// Swap
			if (e2 instanceof VerbEntity && (e1 instanceof ObjectEntity || e1 instanceof PlaceEntity)) {
				arg1.setEntity(vm, e2);
				vm.setArg1Token(word2);
				arg2.setEntity(vm, e1);
				vm.setArg2Token(word1);

				statusFlags = e2.getDefaultFlags();
			}
		}

		status.setFlags(vm, statusFlags);
	}

	private void get(ObjectEntity obj) {
		obj.setLocation(vm, Constants.OBJECT_LOCATION_INHAND);
		status.setFlag(vm, Constants.STATUS_JUGGLED);
	}

	private void eoi() {
		ActiveIterator iterator = vm.getIteratorStack().peek();

		if (!iterator.getIterator().hasNext()) {
			trace("Done iteration");
			vm.getIteratorStack().pop();
			return;
		}

		Entity entity = iterator.getIterator().next();
		VariableEntity variable = (VariableEntity) world.getEntityById(iterator.getVariableId());
		variable.setEntity(vm, entity);
		currentFrame().setOpcode(iterator.getOpcode());
	}

	private void stop() {
		vm.setInterpreterState(InterpreterState.COMPLETED);
		vm.getExecutionStack().clear();
	}

	private void itobj(VariableEntity variable) {
		RestartableTypeFilteringListIterator<Entity, ObjectEntity> objects = world.getObjectEntities().iterator();
		if (objects.hasNext()) {
			variable.setEntity(vm, objects.next());
			vm.getIteratorStack().add(new ActiveIterator(variable.getId(), currentFrame().getInstructionPointer(), objects));
		} else {
			trace("No objects found");
			skipBlock();
		}
	}

	private void itplace(VariableEntity variable) {
		RestartableTypeFilteringListIterator<Entity, PlaceEntity> places = world.getPlaceEntities().iterator();
		if (places.hasNext()) {
			variable.setEntity(vm, places.next());
			vm.getIteratorStack().add(new ActiveIterator(variable.getId(), currentFrame().getInstructionPointer(), places));
		} else {
			trace("No objects found");
			skipBlock();
		}
	}

	private void ifat(PlaceEntity place) {
		if (place == null)
			conditional(false);
		else {
			trace("If at " + place + "?");
			conditional(here.getIntValue(vm) == place.getId());
		}
	}

	private void ifhave(ObjectEntity object) {
		boolean have = object.getLocation(vm) == Constants.OBJECT_LOCATION_INHAND;
		trace("Have " + object + "?");
		conditional(have);
	}

	private void ifnear(ObjectEntity object) {
		boolean have = object.getLocation(vm) == Constants.OBJECT_LOCATION_INHAND;
		boolean near = object.getLocation(vm) == here.getIntValue(vm);

		if (object.hasFlag(vm, Constants.OBJECT_DUAL))
			near |= object.getLocation(vm) == here.getIntValue(vm) - 1;

		trace("Have? " + have + " Near? " + near);

		conditional(have || near);
	}

	private void ifloc(ObjectEntity object, PlaceEntity place) {
		if (place == null)
			conditional(false);
		else
			conditional(object.getLocation(vm) == place.getId());
	}

	private void bit(Entity entity, int value) {
		conditional(entity.hasFlag(vm, value));
	}

	private boolean restartRepeat() {
		if (!allowRestart)
			return false;

		// If we've run off the end of all the REPEAT procedures, put them
		// back
		trace("Restoring REPEAT procedures");
		if (world.getRepeatProcedures().size() == 0) {
			trace("No REPEAT: done");
			vm.setInterpreterState(InterpreterState.COMPLETED);
			return false;
		}

		pushRepeat();

		return true;
	}

	/**
	 * Skips a block. Assumes that IP is positioned immediately after the start
	 * of the block.
	 */
	private void skipBlock() {
		trace("Skipping");
		int blockCount = 1;
		loop: while (true) {
			// Skip over this block
			CompiledLine next = getCurrentParsedOpcode();
			trace("  SKIP: " + next + " " + blockCount);
			if (next == null) {
				trace("Fell off procedure while skipping block");
				return;
			}
			MinorOpcodeType type = next.getOpcode().getType();
			switch (type) {
			case ELSE_BLOCK:
				if (blockCount == 1)
					break loop;
				break;
			case BLOCK_END:
				blockCount--;

				if (blockCount == 0)
					break loop;
				break;
			case CONDITIONAL:
			case ITERATOR:
				// Increment the block count if it's not a chained conditional
				if (next.getBooleanOp() == null)
					blockCount++;
				break;
			case NORMAL:
				// Do nothing
				break;
			case CHAIN:
				error("Unexpected opcode type: " + type);
				break;
			}
			currentFrame().step();
		}
		currentFrame().step();
	}

	public CompiledLine getCurrentParsedOpcode() {
		StackFrame currentFrame = currentFrame();
		int ip = currentFrame.getInstructionPointer();
		if (ip > currentFrame.getEnd())
			return null;
		
		return world.getProgram().getCode().get(ip);
	}

	private void if_(int v1, int v2) {
		switch (currentOpcode.getOpcode()) {
		case IFEQ:
			conditional(v1 == v2);
			break;
		case IFGT:
			conditional(v1 > v2);
			break;
		case IFLT:
			conditional(v1 < v2);
			break;
		default:
			error("Unexpected opcode: " + currentOpcode.getOpcode());
			break;
		}
	}

	private void exec(int value, VariableEntity variable) {
		switch (value) {
		case 1:
			// Dump current state of game
			hostEnvironment.save(vm.save());
			variable.setIntValue(vm, 0);
			break;
		case 2:
			// Restore dumped game
			Map<String, String> restore = hostEnvironment.restore();
			if (restore == null) {
				// No game
				variable.setIntValue(vm, 1);
			} else {
				vm.restore(restore);
				variable.setIntValue(vm, 0);
			}
			break;
		case 3:
			// Delete dumped game
			hostEnvironment.save(null);
			variable.setIntValue(vm, 0);
			break;
		case 4:
			// Display news: no-op (why does it display news after restore?)
			break;
		case 5:
			// Is the cave open ("prime time" flag)? (puts 0 in the variable if
			// so)
			variable.setIntValue(vm, 0);
			break;
		case 6:
			// Print hours
			hostEnvironment.printString("All day.\n");
			variable.setIntValue(vm, 0);
			break;
		case 7:
			// Save a value over RESTORE
			vm.setExecutiveSavedValue(variable.getIntValue(vm));
			break;
		case 8:
			// Restore a value over RESTORE
			variable.setIntValue(vm, vm.getExecutiveSavedValue());
			break;
		case 9:
			// Exit if demo games are not allowed (no implementation)
			break;
		default:
			glitch("Unexpected EXEC: " + value);
		}
		trace("EXEC " + value + " -> " + variable.getName());
	}

	private void else_() {
		skipBlock();
	}

	private void chance(int chance) {
		if (hostEnvironment.randomInt(100) < chance) {
			conditional(true);
		} else {
			conditional(false);
		}
	}

	private void conditional(boolean b) {
		if (currentOpcode.isNot()) {
			trace("Conditional: inverted");
			b = !b;
		}

		vm.setLastWasConditional(true);

		boolean conditionalSuccess = vm.isConditionalSuccess();
		if (currentOpcode.getBooleanOp() != null) {
			trace("Conditional: " + conditionalSuccess + " " + currentOpcode.getBooleanOp() + " " + b);
			switch (currentOpcode.getBooleanOp()) {
			case AND:
				conditionalSuccess &= b;
				break;
			case OR:
				conditionalSuccess |= b;
				break;
			case EOR:
				conditionalSuccess ^= b;
				break;
			}
		} else {
			trace("Conditional: " + b);
			conditionalSuccess = b;
		}
		
		vm.setConditionalSuccess(conditionalSuccess);
	}

	private void lda(VariableEntity variable, int value) {
		variable.setEntity(vm, value);

		trace(variable.getName() + " -> " + world.getEntityById(value));
	}

	private void goto_(PlaceEntity place) {
		there.setFrom(vm, here);
		here.setEntity(vm, place);
		here.setFlags(vm, place.getFlags(vm));

		trace("HERE => " + world.getEntityById(here.getIntValue(vm)) + ", THERE => " + world.getEntityById(there.getIntValue(vm)));

		status.setFlag(vm, Constants.STATUS_MOVED);
	}

	private void apport(ObjectEntity object, PlaceEntity place) {
		trace("APPORT " + object + " -> " + place);
		object.setLocation(vm, place.getId());
	}

	private Entity resolveEntity(KernelValue val) {
		return (Entity) val;
	}

	private Entity resolveEntityIndirect(KernelValue val) {
		Entity entity = resolveEntity(val);
		if (entity instanceof VariableEntity) {
			VariableEntity variable = (VariableEntity) entity;
			int id = variable.getIntValue(vm);
			if (variable.isEntity(vm)) {
				Entity indirect = world.getEntityById(id);
				if (indirect == null)
					glitch("No entity with ID " + id);
				return indirect;
			}
			return variable;
		}

		return entity;
	}

	private void say(Entity entity, String name, int value) {
		String output = "";

		if (entity instanceof ObjectEntity) {
			ObjectEntity object = (ObjectEntity) entity;
			if (object.getLocation(vm) == Constants.OBJECT_LOCATION_INHAND) {
				output = processAlternatives(object.getDesc(), object.getIntValue(vm));
			} else {
				if (entity.getIntValue(vm) < 0 || entity.getIntValue(vm) >= object.getStates().size())
					// Print nothing
					return;
				else
					output = object.getStates().get(entity.getIntValue(vm));
			}

		} else if (entity instanceof PlaceEntity) {
			PlaceEntity place = (PlaceEntity) entity;

			boolean brief = status.hasFlag(vm, Constants.STATUS_BRIEF);
			boolean fast = status.hasFlag(vm, Constants.STATUS_FAST);
			boolean been = place.hasFlag(vm, Constants.PLACE_BEEN);

			output = (fast || (brief && been)) ? place.getShortDesc() : place.getLongDesc();
		} else if (entity instanceof TextEntity) {
			TextEntity text = (TextEntity) entity;
			if (name != null)
				output = processAlternatives(text.getTextValue(), value);
			else
				output = text.getTextValue();
		} else {
			glitch("Bad say: " + entity);
			return;
		}

		if (name != null)
			output = output.replaceAll("#", name.toLowerCase());

		if (output.startsWith("!`"))
			hostEnvironment.printString(output.substring(2));
		else
			hostEnvironment.printString(output + "\n");
	}

	private String processAlternatives(String text, int value) {
		StringBuilder output = new StringBuilder();
		for (int i = 0; i < text.length(); i++) {
			int start = i + 1;
			if (text.charAt(i) == '[') {
				for (; i < text.length() && text.charAt(i) != ']'; i++) {
					// Find the end
				}

				String group = text.substring(start, i);
				String[] alternatives = group.split("/", 2);
				output.append(alternatives[value == 1 ? 0 : 1]);
			} else {
				output.append(text.charAt(i));
			}
		}

		return output.toString();
	}

	private void value(Entity text, KernelValue value) {
		int v;
		if (value instanceof ImmediateValue) {
			v = value.getIntValue(vm);
		} else {
			Entity e = resolveEntityIndirect(value);
			if (e instanceof ObjectEntity)
				v = e.getIntValue(vm) + 1;
			else
				v = e.getIntValue(vm);
		}

		String output = processAlternatives(text.getTextValue(), v);
		output = output.replaceAll("#", "" + v);
		if (output.startsWith("!`"))
			hostEnvironment.printString(output.substring(2));
		else
			hostEnvironment.printString(output + "\n");
	}

	private TextEntity resolveText(KernelValue val) {
		return (TextEntity) resolveEntityIndirect(val);
	}

	private ObjectEntity resolveObject(KernelValue val) {
		return (ObjectEntity) resolveEntityIndirect(val);
	}

	private PlaceEntity resolvePlace(KernelValue val) {
		Entity entity = resolveEntityIndirect(val);

		// Silently fail
		if (entity instanceof VariableEntity && entity.getIntValue(vm) == 0)
			return null;

		if (!(entity instanceof PlaceEntity)) {
			glitch("Not a place entity: " + entity);
			return null;
		}
		return (PlaceEntity) entity;
	}

	private int resolveValue(KernelValue value) throws ParseException {
		return value.getIntValue(vm);
	}

	private int resolveEntityIdIndirect(KernelValue value) throws ParseException {
		if (value instanceof Entity)
			return ((Entity) value).getId();
		return value.getIntValue(vm);
	}

	private void set(Entity entity, int value) {
		trace(entity.getName() + " -> " + value);
		entity.setIntValue(vm, value);
	}

	private void add(Entity entity, int value) {
		trace(entity.getName() + " += " + value);
		entity.setIntValue(vm, entity.getIntValue(vm) + value);
	}

	private void sub(Entity entity, int value) {
		trace(entity.getName() + " -= " + value);
		entity.setIntValue(vm, entity.getIntValue(vm) - value);
	}

	private void mult(Entity entity, int value) {
		trace(entity.getName() + " *= " + value);
		entity.setIntValue(vm, entity.getIntValue(vm) * value);
	}

	private void divide(Entity entity, int value) {
		trace(entity.getName() + " /= " + value);
		entity.setIntValue(vm, entity.getIntValue(vm) / value);
	}

	private void random(Entity entity, int value) {
		if (value < 0) {
			glitch("Invalid argument to RANDOM: " + value);
			return;
		}
		value = value == 0 ? 0 : hostEnvironment.randomInt(value);
		set(entity, value);
	}

	private void bis(Entity entity, int value) {
		entity.setFlag(vm, value);
	}

	private void bic(Entity entity, int value) {
		entity.clearFlag(vm, value);
	}

	private VariableEntity resolveVariable(KernelValue val) {
		if (val instanceof VariableEntity)
			return (VariableEntity) val;

		glitch("Not a variable");
		return null;
	}

	public boolean isComplete() {
		return vm.getInterpreterState() == InterpreterState.COMPLETED;
	}

	private void trace(String string) {
//		 System.out.println("TRACE: " + string);
	}

	private void glitch(String string) {
		System.out.println("*** GLITCH: " + string);
	}

	private void error(String string) {
		System.out.println("*** ERROR: " + string);
	}

}
