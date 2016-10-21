package com.circuits.circuitsmod.circuitImplementation;

public class ClockCircuit {
	int output;
	double time;
	
	public ClockCircuit() {
		output = 0;
		time = 0;
	}
	
	public void tick() {
		time = time + 0.10;
		output = value0();
	}
	
	public int value0() {
		if (time % 2 > 0)
			return 1;
		 else
			return 0;
	}
}
