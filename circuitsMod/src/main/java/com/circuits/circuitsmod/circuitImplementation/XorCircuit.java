package com.circuits.circuitsmod.circuitImplementation;

public class XorCircuit {

	boolean input1;
	boolean input2;
	boolean output;
	
	public XorCircuit() {
		input1 = false;
		input2 = false;
		output = false;
	}
	
	 void tick(boolean i1, boolean i2) {
		 input1 = i1;
		 input2 = i2;
		 output = value0();
	 }
	 
	 boolean value0() {
		 return input1 ^ input2;
	 }
	 
	
}