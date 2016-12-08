package com.circuits.circuitsmod.circuitimplementation;

public class AnalogToDigitalCircuit {
	
	int input1;
	int output;
	
	public AnalogToDigitalCircuit() {
		input1 = 0;
		output = 0;
	}
	
	public void tick(byte i1) {
		input1 = i1;
		output = value0();
	}
	
	public int value0() {
		return (int)input1;
	}
	
}
