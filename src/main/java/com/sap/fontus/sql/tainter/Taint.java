package com.sap.fontus.sql.tainter;

public class Taint {
	
	private String name;
	private String taintBits;
	
	public Taint(String name, String taintBits){
		this.name = name;
		this.taintBits = taintBits;
	}

	String getName() {
		return name;
	}

	String getTaintBits() {
		return taintBits;
	}
}
