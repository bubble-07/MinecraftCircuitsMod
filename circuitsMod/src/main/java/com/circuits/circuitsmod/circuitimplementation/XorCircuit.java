package com.circuits.circuitsmod.circuitimplementation;

public class XorCircuit {

	long input1;
	long input2;
	long output;
	
	public XorCircuit() {
		input1 = 0;
		input2 = 0;
	}
	
	 void tick(long i1, long i2) {
		 input1 = i1;
		 input2 = i2;
		 output = value0();
	 }
	 
	 long value0() {
		 return input1 ^ input2;
	 }
	 
	 public int[] inputWidths() { 
		 String inputValue1 = Long.toBinaryString(input1);
		 int width1 = 0;
		 String inputValue2 = Long.toBinaryString(input2);
		 int width2 = 0;
		 for (int i = 0; i < inputValue1.length(); i++) {
			 width1 = width1 + 1;
		 }
		 for (int j = 0; j < inputValue2.length(); j++) {
			 width2 = width2 + 1;
		 }
		 
		 int[] inputWidths = {width1, width2};
		 return inputWidths; 
	 }
	 
	 public int[] outputWidths() {
		 String outputValue1 = Long.toBinaryString(output);
		 int width1 = 0;
		 for (int i = 0; i < outputValue1.length(); i++) {
			 width1 = width1 + 1;
		 }
		 
		 int[] outputWidths = {width1};
		 return outputWidths; 
	 }
	
}
