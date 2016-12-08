package com.circuits.circuitsmod.circuitimplementation;

public class ABBACircuit {
	
	boolean input1;
	boolean output1;
	boolean output2;
	
	int countA = 0;
	int countB = 0;
	
	public static final int timeA = 30;
	public static final int timeB = 15;
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
		if (countA <= timeA) {
			countA++;
			return true;
		}
		else {
			countA = 0;
			return false;
		}
	}
	
	public boolean value1() {
		if (output1 && countB <= timeB) {
			countB++;
			return true;
		} else {
			countB = 0;
			return false;
		}
	}
	
	public boolean isSequential() {
		return true;
	}
	
	public int[] outputWidths() {
		int[] returnArray = {64, 64};
		return returnArray;
	}
	
	public int[] inputWidths() {
		int[] returnArray = {64};
		return returnArray;
	}

}
