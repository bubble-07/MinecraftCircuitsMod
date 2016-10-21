package com.circuits.circuitsmod.circuitImplementation;

public class InverterCircuit {
	
	long input;
	long output;
	
	public InverterCircuit() {
		input = 0;
		output = 0;
	}
	
	public void tick() {
		output = value0(input);
	}
	
	long value0(long input) {
		return ~input;
	}
	
}
