package com.grack.adventure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Resources;
import com.grack.adventure.kernel.Interpreter;
import com.grack.adventure.kernel.InterpreterState;
import com.grack.adventure.kernel.Score;
import com.grack.adventure.kernel.ScoreUtil;
import com.grack.adventure.kernel.VirtualMachine;
import com.grack.adventure.kernel.world.World;
import com.grack.adventure.kernel.world.WorldBuilder;
import com.grack.adventure.parser.ACodeParser;
import com.grack.adventure.parser.ParseException;
import com.grack.adventure.parser.program.ACodeProgram;

public class ParserTest {
	private VirtualMachine vm;
	private World state;

	@Test
	public void testSave1() throws IOException {
		run("iftest.acode");
		Map<String, String> saved = vm.save();
		assertEquals("0,1", saved.get("5"));
		assertEquals(1, saved.size());
	}

	@Test
	public void testSave2() throws IOException {
		run("saytest.acode");
		Map<String, String> saved = vm.save();
		assertEquals("2000,80001017", saved.get("0"));
		assertEquals("25,0", saved.get("2"));
		assertEquals("0,2", saved.get("5"));
		assertEquals("0,2,-1", saved.get("18"));
		assertEquals("0,0,-1", saved.get("19"));
		assertEquals("2000,0", saved.get("23"));
		assertEquals(6, saved.size());
	}

	@Test
	public void testIf() throws ParseException, IOException {
		String output = run("iftest.acode");
		assertEquals(Resources.toString(getClass().getResource("iftest.txt"), Charsets.UTF_8), output);
	}
	
	@Test
	public void testIf2() throws ParseException, IOException {
		String output = run("iftest2.acode");
		assertEquals("Pass!\nPass!\nPass!\nPass!\nPass!\n", output);
	}

	@Test
	public void testCall() throws ParseException, IOException {
		String output = run("calltest.acode", "n");
		assertEquals("Pass! 1\nPass! 2\n> n\nPass! 3\nPass! 4\nPass! 5\n", output);
	}

	@Test
	public void testObjectAndPlaceIterator() throws ParseException, IOException {
		String output = run("iteratortest.acode");
		assertEquals(Resources.toString(getClass().getResource("iteratortest.txt"), Charsets.UTF_8), output);
	}

	@Test
	public void testSay() throws ParseException, IOException {
		String output = run("saytest.acode");
		assertEquals(Resources.toString(getClass().getResource("saytest.txt"), Charsets.UTF_8), output);
	}

	@Test
	public void testConstants() throws ParseException, IOException {
		String output = run("constantstest.acode");
		assertEquals(Resources.toString(getClass().getResource("constantstest.txt"), Charsets.UTF_8), output);
	}

	@Test
	public void testInput0() throws ParseException, IOException {
		String outputs = Resources.toString(getClass().getResource("inputtest.txt"), Charsets.UTF_8);
		testInput(outputs, 0);
	}

	@Test
	public void testInput1() throws ParseException, IOException {
		String outputs = Resources.toString(getClass().getResource("inputtest.txt"), Charsets.UTF_8);
		testInput(outputs, 1);
	}

	@Test
	public void testInput2() throws ParseException, IOException {
		String outputs = Resources.toString(getClass().getResource("inputtest.txt"), Charsets.UTF_8);
		testInput(outputs, 2);
	}

	@Test
	public void testInput3() throws ParseException, IOException {
		String outputs = Resources.toString(getClass().getResource("inputtest.txt"), Charsets.UTF_8);
		testInput(outputs, 3);
	}

	@Test
	public void testInput4() throws ParseException, IOException {
		String outputs = Resources.toString(getClass().getResource("inputtest.txt"), Charsets.UTF_8);
		testInput(outputs, 4);
	}

	@Test
	public void testInput5() throws ParseException, IOException {
		String outputs = Resources.toString(getClass().getResource("inputtest.txt"), Charsets.UTF_8);
		testInput(outputs, 5);
	}

	@Test
	public void testInput6() throws ParseException, IOException {
		String outputs = Resources.toString(getClass().getResource("inputtest.txt"), Charsets.UTF_8);
		testInput(outputs, 6);
	}

	@Test
	public void testInput7() throws ParseException, IOException {
		String outputs = Resources.toString(getClass().getResource("inputtest.txt"), Charsets.UTF_8);
		testInput(outputs, 7);
	}

	@Test
	public void testInput8() throws ParseException, IOException {
		String outputs = Resources.toString(getClass().getResource("inputtest.txt"), Charsets.UTF_8);
		testInput(outputs, 8);
	}

	@Test
	public void testInput9() throws ParseException, IOException {
		String outputs = Resources.toString(getClass().getResource("inputtest.txt"), Charsets.UTF_8);
		testInput(outputs, 9);
	}

	@Test
	public void testInput10() throws ParseException, IOException {
		String outputs = Resources.toString(getClass().getResource("inputtest.txt"), Charsets.UTF_8);
		testInput(outputs, 10);
	}

	@Test
	public void testInput11() throws ParseException, IOException {
		String outputs = Resources.toString(getClass().getResource("inputtest.txt"), Charsets.UTF_8);
		testInput(outputs, 11);
	}

	@Test
	public void testInput12() throws ParseException, IOException {
		String outputs = Resources.toString(getClass().getResource("inputtest.txt"), Charsets.UTF_8);
		testInput(outputs, 12);
	}

	@Test
	public void testInput13() throws ParseException, IOException {
		String outputs = Resources.toString(getClass().getResource("inputtest.txt"), Charsets.UTF_8);
		testInput(outputs, 13);
	}

	@Test
	public void testInput14() throws ParseException, IOException {
		String outputs = Resources.toString(getClass().getResource("inputtest.txt"), Charsets.UTF_8);
		testInput(outputs, 14);
	}

	@Test
	public void sampleRun() throws IOException {
		ACodeProgram program = new ACodeProgram();
		ACodeParser parser = new ACodeParser(program);
		parser.parse(Resources.toString(ParserTest.class.getResource("ADVENTURE.ACODE"), Charsets.UTF_8));
		String sampleRun = Resources.toString(getClass().getResource("samplerun.txt"), Charsets.UTF_8);
		TestUserInterfaceImpl uiImpl = new TestUserInterfaceImpl(true);
		for (String line : Splitter.on('\n').split(sampleRun)) {
			if (line.startsWith("> "))
				uiImpl.addInput(line.substring(2));
		}

		WorldBuilder gameStateBuilder = new WorldBuilder(program);
		World world = gameStateBuilder.getState();
		Interpreter interpreter = new Interpreter(uiImpl, world, new VirtualMachine());

		while (!interpreter.isComplete()) {
			if (interpreter.step() == InterpreterState.WAITING_FOR_INPUT) {
				break;
			}
		}

		Score score = ScoreUtil.getScore(world, interpreter.getVm());
		assertEquals(24, score.getScore());
		assertEquals(550, score.getMaxScore());
		
		assertEquals(uiImpl.toString(), sampleRun);
	}

	private void testInput(String outputs, int which) throws IOException {
		String[] tests = outputs.split("---\n");
		// Note: add new test methods if adding new tests!
		assertEquals(15, tests.length);
		String output = tests[which];
		String[] bits = output.split("\n", 2);
//		System.out.println(Arrays.toString(bits));
		String input = bits[0].substring(1);
		output = bits[1];
		assertEquals("> " + input + "\n" + output.trim(), run("inputtest.acode", input).trim());
	}

	private String run(String file, String... inputs) throws IOException {
		ACodeProgram program = new ACodeProgram();
		ACodeParser parser = new ACodeParser(program);
		parser.parse(Resources.toString(ParserTest.class.getResource(file), Charsets.UTF_8));
		TestUserInterfaceImpl uiImpl = new TestUserInterfaceImpl(true);
		WorldBuilder gameStateBuilder = new WorldBuilder(program);

		for (String input : inputs)
			uiImpl.addInput(input);

		state = gameStateBuilder.getState();

		Interpreter interpreter = new Interpreter(uiImpl, state, new VirtualMachine());
		vm = interpreter.getVm();

		while (!interpreter.isComplete()) {
			if (interpreter.step() == InterpreterState.WAITING_FOR_INPUT) {
				fail("Waiting for input");
			}
		}

		String output = uiImpl.toString();
		return output;
	}

	public static void main(String[] args) throws IOException, ParseException {
		// ACodeParser majorOpcodeParser = new ACodeParser(kernelStateBuilder);
		ACodeProgram program = new ACodeProgram();
		ACodeParser parser = new ACodeParser(program);
		parser.parse(Resources.toString(ParserTest.class.getResource("ADVENTURE.ACODE"), Charsets.UTF_8));
		TestUserInterfaceImpl ui = new TestUserInterfaceImpl(false) {
			@Override
			public void printString(String text) {
				System.out.print(text);
				super.printString(text);
			}
		};
		WorldBuilder kernelStateBuilder = new WorldBuilder(program);

		World world = kernelStateBuilder.getState();

		Interpreter interpreter = new Interpreter(ui, world, new VirtualMachine());
		BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

		while (!interpreter.isComplete()) {
			InterpreterState interpreterState = interpreter.step();
			if (interpreterState == InterpreterState.WAITING_FOR_INPUT || interpreterState == InterpreterState.WAITING_FOR_QUERY) {
				Score score = ScoreUtil.getScore(world, interpreter.getVm());
				System.out.print("(" + score.getScore() + "/" + score.getMaxScore() + ")> ");
				
				ui.addInput(stdin.readLine());
			}
		}
	}
}
