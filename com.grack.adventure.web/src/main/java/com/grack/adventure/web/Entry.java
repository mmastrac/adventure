package com.grack.adventure.web;

import com.google.gwt.core.client.EntryPoint;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Entry implements EntryPoint {

	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		attachToDocument();
	}

	private native void attachToDocument() /*-{
		var obj = this;
		$wnd.ACode = function(file, callbacks) {
			this._acode = @com.grack.adventure.web.ACodeInterface::new(Ljava/lang/String;Lcom/google/gwt/core/client/JavaScriptObject;)(file, callbacks);
		};

		$wnd.ACode.prototype.input = function(input) {
			this._acode.@com.grack.adventure.web.ACodeInterface::input(Ljava/lang/String;)(input);
		}

		$wnd.ACode.prototype.pause = function() {
			this._acode.@com.grack.adventure.web.ACodeInterface::pause()();
		}

		$wnd.ACode.prototype.resume = function() {
			this._acode.@com.grack.adventure.web.ACodeInterface::resume()();
		}

		$wnd.ACode.prototype.step = function() {
			this._acode.@com.grack.adventure.web.ACodeInterface::step()();
		}

		if ($wnd.ACodeReady)
			$wnd.ACodeReady();
	}-*/;
}
