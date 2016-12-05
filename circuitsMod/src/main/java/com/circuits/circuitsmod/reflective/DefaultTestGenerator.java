package com.circuits.circuitsmod.reflective;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.circuits.circuitsmod.common.BusData;
import com.google.common.collect.Lists;

/**
 * Default test generator for combinational circuits for which
 * a Tests.class is not defined -- this will do exhaustive or randomized
 * tests, depending on the formally-declared number of inputs/outputs
 * for a given circuit.
 * @author bubble-07
 *
 */
//TODO: this __probably__ doesn't belong in the reflective package, but where does it belong?
public class DefaultTestGenerator implements TestGenerator {
	//Maximum total number of tests we're willing to perform in any given testing
	//sequence. This determines the cutoff between exhaustive and randomized test
	private static int MAX_TESTS = 32;
	//TODO: Make this configurable!
	
	int totalTests;
	int[] inputWidths;
	
	public DefaultTestGenerator(int[] inputWidths) {
		this.totalTests = (int) Math.min(DefaultTestGenerator.getProductSize(inputWidths), MAX_TESTS);
		this.inputWidths = inputWidths;
	}
	
	public DefaultTestGenerator.State initState() {
		return new State(inputWidths);
	}
	
	public static class State implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private int[] inputWidths;
		boolean randomized = false;
		long testNum = 0;
		
		public State(int[] inputWidths) {
			this.inputWidths = inputWidths;
			long numExhaustive = getProductSize(inputWidths);
			if (numExhaustive > MAX_TESTS) {
				this.randomized = true;
			}
		}
		
		/**
		 * Partitions the given integer along the input widths
		 * @param val
		 * @return
		 */
		private long[] partitionLong(long val) {
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
		private long[] randomizeLong() {
			long[] result = new long[inputWidths.length];
			for (int i = 0; i < result.length; i++) {
				long maxValForCurrent = (long) Math.pow(2, inputWidths[i]);
				long val = (long) (Math.random() * (maxValForCurrent - 1));
				result[i] = val;
 			}
			return result;
		}
		
		public List<BusData> getTestForIndex(long ind) {
			long[] toDeliver;
			if (this.randomized) {
				toDeliver = randomizeLong();
			}
			else {
				toDeliver = partitionLong(ind);
			}
			List<BusData> result = Lists.newArrayList();
			for (int i = 0; i < toDeliver.length; i++) {
				result.add(new BusData(inputWidths[i], toDeliver[i]));
			}
			return result;
		}
	}
	
	private static long getProductSize(int[] widthArr) {
		int totalWidth = Arrays.stream(widthArr).sum();
		return (long) Math.pow(2, totalWidth);
	}
	
	public Optional<List<BusData>> invoke(State state) {
		if (state.testNum > MAX_TESTS) {
			//Done testing
			return Optional.empty();
		}
		List<BusData> datas = state.getTestForIndex(state.testNum);
		
		//Move the state along to the next test
		state.testNum++;
		
		return Optional.of(datas);
	}

	@Override
	public boolean slowable() {
		return true;
	}

	@Override
	public int totalTests() {
		return this.totalTests;
	}

	@Override
	public Optional<List<BusData>> invoke(Serializable state) {
		//TODO: plz help, static typing gods.
		return invoke((State) state);
	}
}
