package com.circuits.circuitsmod.circuitimplementation;

public class ABBACircuit {
	
	boolean input1;
	boolean output1;
	boolean output2;
	double time1;
	double time2;
	double time3;
	
	//NEED TO UNDERSTAND DEFINITION OF TIME!
	public ABBACircuit() {
		input1 = false;
		output1 = false;
		output2 = false;
	}
	
	public void tick(boolean i1) {
		input1 = i1;
		output1 = value0();
		output2 = value1();
	}
	
	public boolean value0() {
		return input1;
	}
	
	public boolean value1() {
		return input1;
	}
	
	public boolean isSequential() {
		return true;
	}
	

}
