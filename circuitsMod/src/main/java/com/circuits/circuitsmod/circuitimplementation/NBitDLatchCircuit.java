package com.circuits.circuitsmod.circuitimplementation;

public class NBitDLatchCircuit {
	int latchIn;
	long input1;
	long output;
	
	public NBitDLatchCircuit() {
		latchIn = 0;
		input1 = 0;
		output = 0;
	}
	
	public void tick(long i1, int l1) {
		input1 = i1;
		latchIn = l1;
		output = value0();
	}
	
	public long value0() {
		if (latchIn == 0) {
			return output;
		}
		else {
			return input1;
		}
	}
}
