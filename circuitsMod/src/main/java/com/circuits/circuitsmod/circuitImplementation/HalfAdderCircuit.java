package com.circuits.circuitsmod.circuitImplementation;

public class HalfAdderCircuit {

	boolean input1;
	boolean input2;
	int output;
	int carryOut;
	
	public HalfAdderCircuit() {
		input1 = false;
		input2 = false;
		output = 0;
		carryOut = 0;
	}
	
	public void tick() {
		output = toInt(value0());
		carryOut = toInt(value1());
	}
	
	public boolean value0() {
		return (input1 || input2) && !(input1 && input2);
	}
	
	public boolean value1() {
		return input1 && input2;
	}
	
	public int toInt(boolean input) {
		return ((input == true) ? 1 : 0);
	}
	
}
