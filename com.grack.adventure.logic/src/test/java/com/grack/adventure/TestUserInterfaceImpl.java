package com.grack.adventure;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import com.grack.adventure.kernel.HostEnvironment;

class TestUserInterfaceImpl implements HostEnvironment {
	private StringBuilder say = new StringBuilder();
	private Deque<String> input = new LinkedList<String>();
	private Map<String, String> saved;
	private Random r = new Random(0);
	private final boolean echo;

	public TestUserInterfaceImpl(boolean echo) {
		this.echo = echo;
	}

	public void addInput(String input) {
		this.input.add(input);
	}

	public void printString(String text) {
		say.append(text);
	}

	@Override
	public String toString() {
		return say.toString();
	}

	public String input() {
		if (input.size() == 0)
			return null;
		String line = input.removeFirst();
		if (echo)
			printString("> " + line + "\n");
		return line;
	}

	public Boolean query() {
		String input = input();
		if (input == null)
			return null;

		if (input.equalsIgnoreCase("yes") || input.equalsIgnoreCase("y"))
			return true;
		if (input.equalsIgnoreCase("no") || input.equalsIgnoreCase("n"))
			return false;

		printString("Please answer yes or no\n");
		return null;
	}

	public void save(Map<String, String> game) {
		this.saved = game;
		System.out.println("SAVE: " + game);
	}

	public Map<String, String> restore() {
		return this.saved;
	}

	public int randomInt(int n) {
		return r.nextInt(n);
	}
}