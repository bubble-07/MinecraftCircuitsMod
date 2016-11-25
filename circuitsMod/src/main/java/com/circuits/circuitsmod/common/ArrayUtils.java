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
	
	public static <T> T[] cat(T[] a, T[] b) {
		T[] result = ((T[]) new Object[a.length + b.length]);
		for (int i = 0; i < a.length; i++) {
			result[i] = a[i];
		}
		for (int i = 0; i < b.length; i++) {
			result[a.length + i] = b[i];
		}
		return result;
	}
}
