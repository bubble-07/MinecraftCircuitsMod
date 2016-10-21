package com.circuits.circuitsmod.circuitImplementation;

public class PulseLengthenerCircuit {
	long input;
	long output;
	long multiplier;
	
	public PulseLengthenerCircuit() {
		input = 0;
		output = 0;
		multiplier = 2;
	}
	
	public void tick() {
		output = value0();
	}
	
	public long value0() {
		return input*multiplier;
	}
}
