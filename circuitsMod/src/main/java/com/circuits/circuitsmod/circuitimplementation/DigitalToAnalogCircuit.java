package com.circuits.circuitsmod.circuitimplementation;

public class DigitalToAnalogCircuit {
	int input1;
	byte output;
	
	public DigitalToAnalogCircuit() {
		input1 = 0;
		output = 0;
	}
	
	public void tick(byte i1) {
		input1 = i1;
		output = value0();
	}
	
	public byte value0() {
		return (byte) Math.abs(input1);
	}
}
