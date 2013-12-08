package com.grack.adventure.kernel;

import java.util.Map;

public interface HostEnvironment {
	/**
	 * Prints a string to the console. Lines are terminated with \n.
	 */
	public void printString(String text);

	/**
	 * Asks the user for input. May return NULL if no input is available.
	 */
	public String input();
	
	/**
	 * Prompts the user for a yes/no question. May return NULL if no input is available.
	 */
	public Boolean query();
	
	/**
	 * Saves a game.
	 */
	public void save(Map<String, String> game);
	
	/**
	 * Restores a game.
	 */
	public Map<String, String> restore();
	
	/**
	 * Gets a random integer.
	 */
	public int randomInt(int n);
}
