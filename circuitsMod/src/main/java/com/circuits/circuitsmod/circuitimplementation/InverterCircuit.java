package com.circuits.circuitsmod.circuitimplementation;

public class InverterCircuit {
	
	long input;
	long output;
	
	public InverterCircuit() {
		input = 0;
		output = 0;
	}
	
	public void tick(long i1) {
		input = i1;
		output = value0();
	}
	
	long value0() {
		return ~input;
	}
	
}
