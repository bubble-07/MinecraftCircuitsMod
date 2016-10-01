package com.circuits.circuitsmod.common;

public class ArrayUtils {
	public static boolean inArray(int val, int[] array) {
		for (int i = 0; i < array.length; i++) {
			if (array[i] == val) {
				return true;
			}
		}
		return false;
	}
	public static int[] unbox(Integer[] input) { 
		int[] result = new int[input.length];
		for (int i = 0; i < input.length; i++) {
			result[i] = input[i];
		}
		return result;
	}
}
