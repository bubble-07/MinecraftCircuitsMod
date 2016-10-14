package com.circuits.circuitsmod.circuitImplementation;

import com.circuits.circuitsmod.reflective.*;
public class AndCircuit {
	
	boolean input1;
	boolean input2;
	boolean output;
	
	public AndCircuit() {
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
		 return input1 && input2;
	 }
	 
	
}