package com.grack.adventure.kernel;

import java.util.Map;

import com.grack.adventure.kernel.entity.Entity;
import com.grack.adventure.kernel.world.World;

public class ScoreUtil {
	private static final class NoHostEnvironment implements HostEnvironment {
		public void save(Map<String, String> game) {
			throw new UnsupportedOperationException();
		}

		public Map<String, String> restore() {
			throw new UnsupportedOperationException();
		}

		public int randomInt(int n) {
			throw new UnsupportedOperationException();
		}

		public Boolean query() {
			throw new UnsupportedOperationException();
		}

		public void printString(String text) {
			throw new UnsupportedOperationException();
		}

		public String input() {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Reads the score by cloning the existing vm.
	 */
	public static Score getScore(World world, VirtualMachine vm) {
		vm = vm.clone();

		Entity quitting = world.getEntityByName("QUITTING");
		Entity scorex = world.getEntityByName("SCOREX");
		Entity maxScore = world.getEntityByName("MAXSCORE");

		quitting.setIntValue(vm, 1);

		Interpreter i2 = new Interpreter(new NoHostEnvironment(), world, vm);
		vm.clearExecutionStack();
		i2.setAllowRestart(false);
		i2.callLabel("GETSCORE");
		while (!i2.isComplete())
			i2.step();
		
		return new Score(scorex.getIntValue(vm), maxScore.getIntValue(vm));
	}
}
