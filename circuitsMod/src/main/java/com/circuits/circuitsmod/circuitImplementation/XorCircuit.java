package com.circuits.circuitsmod.circuitImplementation;

public class XorCircuit {

	boolean input1;
	boolean input2;
	
	public XorCircuit() {
		input1 = false;
		input2 = false;
	}
	
	 void tick(boolean i1, boolean i2) {
		 input1 = i1;
		 input2 = i2;
	 }
	 
	 boolean value0() {
		 return input1 ^ input2;
	 }
	 
	
}
