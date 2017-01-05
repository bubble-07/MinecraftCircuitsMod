package com.circuits.circuitsmod.generators;

import java.util.Arrays;
import java.util.List;

import com.circuits.circuitsmod.common.BusData;
import com.google.common.collect.Lists;

public class GeneratorUtils {

	public static long getProductSize(int[] widthArr) {
		int totalWidth = Arrays.stream(widthArr).sum();
		return (long) Math.pow(2, totalWidth);
	}

	/**
	 * Partitions the given integer along the input widths
	 * @param val
	 * @return
	 */
	public static long[] partitionLong(long val, int[] inputWidths) {
		long[] result = new long[inputWidths.length];
		for (int i = 0 ; i < result.length; i++) {
			long old = val;
			val = val >> inputWidths[i];
			long lowBits = old - (val << inputWidths[i]);
			result[i] = lowBits;
		}
		return result;
	}

	/**
	 * Returns an array of random longs, each of which
	 * fits within the widths specified by inputWidths
	 * @param ind
	 * @return
	 */
	public static long[] randomizeLong(int[] inputWidths) {
		long[] result = new long[inputWidths.length];
		for (int i = 0; i < result.length; i++) {
			long maxValForCurrent = (long) Math.pow(2, inputWidths[i]);
			long val = (long) (Math.random() * (maxValForCurrent - 1));
			result[i] = val;
		}
		return result;
	}

	public static List<BusData> busLongs(long[] toDeliver, int[] inputWidths) {
		List<BusData> result = Lists.newArrayList();
		for (int i = 0; i < toDeliver.length; i++) {
			result.add(new BusData(inputWidths[i], toDeliver[i]));
		}
		return result;
	}

}
