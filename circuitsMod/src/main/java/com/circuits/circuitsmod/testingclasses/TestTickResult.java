package com.circuits.circuitsmod.testingclasses;

public class TestTickResult {
	private boolean atEndOfTest;
	private boolean currentlySucceeding;
	
	
	public boolean getAtEndOfTest() {
		return atEndOfTest;
	}
	
	public boolean getCurrentlySucceeding() {
		return currentlySucceeding;
	}
	
	public void setAtEndOfTest(boolean end) {
		atEndOfTest = end;
	}
	
	public void setCurrentlySucceeding(boolean success) {
		currentlySucceeding = success;
	}
	
}
