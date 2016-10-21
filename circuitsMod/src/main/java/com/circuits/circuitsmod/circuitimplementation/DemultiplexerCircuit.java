package com.circuits.circuitsmod.circuitimplementation;

public class DemultiplexerCircuit {
	 long input1;
	 long switchIn;
	 long output1;
	 long output2;
	
	public DemultiplexerCircuit() {
		input1 = 0;
		switchIn = 0;
		output1 = 0;
		output2 = 0;
	}
	
	public void tick(long i1, long s1) {
		input1 = i1;
		switchIn = s1;
		output1 = value0();
		output2 = value1();
	}
	
	public long value0() {
		return switchIn & input1;
	}
	
	public long value1() {
		return ~switchIn & input1;
	}
}
