package com.circuits.circuitsmod.circuitimplementation;

public class MultiplexerCircuit {
	
	long input1;
	long input2;
	long switchIn;
	long output1;
	long output2; 
	
	public MultiplexerCircuit() {
		input1 = 0;
		input2 = 0;
		switchIn = 0;
		output1 = 0;
		output2 = 0;
	}
	
	public void tick(long i1, long i2, long s1) {
		input1 = i1;
		input2 = i2;
		switchIn = s1;
		
		output1 = value0();
		output2 = value1();
	}
	
	public long value0() {
		return input1 & switchIn;
	}
	
	public long value1() {
		return input2 & ~switchIn;
	}
}
