package com.sap.fontus.sql.tainter;

public class Taint {
	
	private final String name;
	private final String taintBits;
	
	public Taint(String name, String taintBits){
		this.name = name;
		this.taintBits = taintBits;
	}

	String getName() {
		return this.name;
	}

	String getTaintBits() {
		return this.taintBits;
	}
}
