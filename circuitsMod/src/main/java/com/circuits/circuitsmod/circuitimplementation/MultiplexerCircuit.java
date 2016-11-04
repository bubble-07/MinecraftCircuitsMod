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
		 String outputValue1 = Long.toBinaryString(output1);
		 int width1 = 0;
		 String outputValue2 = Long.toBinaryString(output2);
		 int width2 = 0;
		 for (int i = 0; i < outputValue1.length(); i++) {
			 width1 = width1 + 1;
		 }
		 for (int j = 0; j < outputValue2.length(); j++) {
			 width2 = width2 + 1;
		 }
		 
		 int[] outputWidths = {width1, width2};
		 return outputWidths; 
	 }
}
