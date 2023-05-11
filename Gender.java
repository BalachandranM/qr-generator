package com;

public enum Gender {
	M("Male"), F("Female");

	private String name;

	Gender(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}
