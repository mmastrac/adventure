package com.grack.adventure.kernel.entity;

public class TextEntity extends Entity {
	private final String textValue;

	public TextEntity(String name, String textValue) {
		super(name);
		this.textValue = textValue;
	}

	public String getTextValue() {
		return textValue;
	}

	@Override
	public String toString() {
		return "{TEXT: " + name + " (" + trim(10) + ")}";
	}

	private String trim(int maxLength) {
		if (textValue.length() > maxLength)
			return textValue.replace("\n", "\\n").substring(0, 10) + "...";
		return textValue.replace("\n", "\\n");
	}
	
	@Override
	public TextEntity clone() {
		return clone(new TextEntity(name, textValue));
	}
}
