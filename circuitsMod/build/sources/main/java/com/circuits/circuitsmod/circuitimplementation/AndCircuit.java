package com.circuits.circuitsmod.circuitimplementation;

public class AndCircuit {
	
	long input1;
	long input2;
	long output;
	
	public AndCircuit() {
		input1 = 0;
		input2 = 0;
		output = 0;
	}
	
	 void tick(long i1, long i2) {
		 input1 = i1;
		 input2 = i2;
		 output = value0();
	 }
	 
	 long value0() {
		 return input1 & input2;
	 }
	 
	 public int[] inputWidths() { 
		 int[] inputWidths = {64, 64};
		 return inputWidths;
	 }
	 
	 public int[] outputWidths() {
		 int[] outputWidths = {64};
		 return outputWidths;
	 }
	 
	
}
