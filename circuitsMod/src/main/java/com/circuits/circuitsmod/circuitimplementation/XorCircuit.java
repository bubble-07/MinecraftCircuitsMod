package com.circuits.circuitsmod.circuitimplementation;

public class XorCircuit {

	long input1;
	long input2;
	
	public XorCircuit() {
		input1 = 0;
		input2 = 0;
	}
	
	 void tick(long i1, long i2) {
		 input1 = i1;
		 input2 = i2;
	 }
	 
	 long value0() {
		 return input1 ^ input2;
	 }
	 
	
}
