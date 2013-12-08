package com.grack.adventure.web;

import java.util.Map;

import com.google.common.collect.Maps;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONException;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Timer;
import com.grack.adventure.kernel.Interpreter;
import com.grack.adventure.kernel.InterpreterState;
import com.grack.adventure.kernel.HostEnvironment;
import com.grack.adventure.kernel.Score;
import com.grack.adventure.kernel.ScoreUtil;
import com.grack.adventure.kernel.VirtualMachine;
import com.grack.adventure.kernel.entity.Entity;
import com.grack.adventure.kernel.world.World;
import com.grack.adventure.kernel.world.WorldBuilder;
import com.grack.adventure.parser.ACodeParser;
import com.grack.adventure.parser.ParseException;
import com.grack.adventure.parser.program.ACodeProgram;

public class ACodeInterface {
	private class WebEnvironment implements HostEnvironment {
		private String queuedInput;

		public String input() {
			String input = queuedInput;
			queuedInput = null;
			return input;
		}

		public void printString(String text) {
			print0(text);
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

		public Map<String, String> restore() {
			return ACodeInterface.this.restore();
		}

		public void save(Map<String, String> game) {
			ACodeInterface.this.save(game);
		}

		public void setQueuedInput(String queuedInput) {
			this.queuedInput = queuedInput;
		}

		@Override
		public int randomInt(int n) {
			return (int) (Math.random() * n);
		}
	}

	private final String file;
	private JavaScriptObject printFunction;
	private JavaScriptObject stateChangeFunction;
	private JavaScriptObject saveFunction;
	private JavaScriptObject restoreFunction;
	private JavaScriptObject logFunction;
	private JavaScriptObject setScoreFunction;
	private JavaScriptObject traceFunction;
	protected ACodeProgram program;
	protected Interpreter interpreter;
	private Timer timer;
	private boolean paused;

	protected WebEnvironment environment;
	protected World world;

	public ACodeInterface(String file, JavaScriptObject callbacks) {
		this.file = file;
		init0(callbacks);

		stateChange0("LOADING");

		timer = new Timer() {
			@Override
			public void run() {
				if (!paused) {
					try {
						trace0("Running code...");
						// Run only if the interpreter is running
						for (int i = 0; i < 100; i++) {
							InterpreterState state = interpreter.step();
							if (state == InterpreterState.COMPLETED) {
								timer.cancel();
								trace0("Done.");
								refreshScore();
								stateChange0("COMPLETED");
							} else if (state == InterpreterState.WAITING_FOR_INPUT || state == InterpreterState.WAITING_FOR_QUERY) {
								paused = true;
								trace0("Need input: " + state);
								refreshScore();
								stateChange0("INPUT");
							}

							if (state != InterpreterState.RUNNING) {
								trace0("Exiting loop: " + state);
								return;
							}
						}
					} catch (Throwable t) {
						trace0("Uh-oh, internal error: " + t.getClass() + " " + t.getMessage());
					}
				}
			}
		};

		RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, "ADVENTURE.ACODE");
		builder.setCallback(new RequestCallback() {
			public void onError(Request request, Throwable exception) {
				stateChange0("ERROR");
				logError("Unable to fetch source: " + exception.getMessage());
			}

			public void onResponseReceived(Request request, Response response) {
				try {
					program = ACodeParser.parseProgram(response.getText());
					environment = new WebEnvironment();
					WorldBuilder kernelStateBuilder = new WorldBuilder(program);
					world = kernelStateBuilder.getState();
					interpreter = new Interpreter(environment, world, new VirtualMachine());

					timer.scheduleRepeating(1);
					stateChange0("RUNNING");
				} catch (ParseException e) {
					stateChange0("ERROR");
					logError("Parse exception: " + e.getMessage());
				}
			}
		});

		try {
			builder.send();
		} catch (RequestException e) {
			stateChange0("ERROR");
			logError("Unable to send request to fetch source: " + e.getMessage());
		}
	}

	public void input(String input) {
		trace0("Got input: " + input);
		environment.setQueuedInput(input);
		paused = false;
	}

	private Map<String, String> restore() {
		Map<String, String> toRestores = Maps.newHashMap();
		String restored = restore0();
		if (restored == null)
			return null;

		try {
			JSONObject json = (JSONObject) JSONParser.parseStrict(restored);
			for (String key : json.keySet()) {
				toRestores.put(key, ((JSONString) json.get(key)).stringValue());
			}
			return toRestores;
		} catch (JSONException e) {
			logError("Malformed saved data JSON: " + e.getMessage());
			return null;
		} catch (ClassCastException e) {
			logError("Malformed saved data JSON: " + e.getMessage());
			return null;
		}
	}

	private void save(Map<String, String> values) {
		if (values == null) {
			save0(null);
			return;
		}

		JSONObject json = new JSONObject();
		for (String key : values.keySet())
			json.put(key, new JSONString(values.get(key)));

		save0(json.toString());
	}

	private void logError(String string) {
		log0("ERROR", string);
	}

	/**
	 * Refreshes the score.
	 */
	private void refreshScore() {
		trace0("Refresh score");

		Score score = ScoreUtil.getScore(world, interpreter.getVm());
		setScore0(score.getScore(), score.getMaxScore());
	}

	private native void init0(JavaScriptObject callbacks) /*-{
		this.@com.grack.adventure.web.ACodeInterface::printFunction = callbacks['print'];
		this.@com.grack.adventure.web.ACodeInterface::stateChangeFunction = callbacks['stateChange'];
		this.@com.grack.adventure.web.ACodeInterface::saveFunction = callbacks['save'];
		this.@com.grack.adventure.web.ACodeInterface::restoreFunction = callbacks['restore'];
		this.@com.grack.adventure.web.ACodeInterface::logFunction = callbacks['log'];
		this.@com.grack.adventure.web.ACodeInterface::setScoreFunction = callbacks['setScore'];
		this.@com.grack.adventure.web.ACodeInterface::traceFunction = callbacks['trace'];
	}-*/;

	private native void log0(String type, String message) /*-{
		(this.@com.grack.adventure.web.ACodeInterface::logFunction)(type, message);
	}-*/;

	private native void print0(String s) /*-{
		(this.@com.grack.adventure.web.ACodeInterface::printFunction)(s);
	}-*/;

	private native String restore0() /*-{
		return (this.@com.grack.adventure.web.ACodeInterface::restoreFunction)();
	}-*/;

	private native void save0(String string) /*-{
		(this.@com.grack.adventure.web.ACodeInterface::saveFunction)(string);
	}-*/;

	private native void setScore0(int score, int total) /*-{
		(this.@com.grack.adventure.web.ACodeInterface::setScoreFunction)(score, total);
	}-*/;

	private native void stateChange0(String state) /*-{
		(this.@com.grack.adventure.web.ACodeInterface::stateChangeFunction)(state);
	}-*/;

	private native void trace0(String msg) /*-{
		(this.@com.grack.adventure.web.ACodeInterface::traceFunction)(msg);
	}-*/;

	public void pause() {
		this.paused = true;
	}

	public void resume() {
		this.paused = false;
	}
}
