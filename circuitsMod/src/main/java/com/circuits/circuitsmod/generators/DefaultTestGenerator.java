package com.circuits.circuitsmod.generators;

import java.io.Serializable;
import java.util.List;
import com.circuits.circuitsmod.Config;
import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.reflective.ChipInvoker;
import com.circuits.circuitsmod.reflective.TestGenerator;

/**
 * Default test generator for combinational circuits for which
 * a Tests.class is not defined -- this will do exhaustive or randomized
 * tests, depending on the formally-declared number of inputs/outputs
 * for a given circuit.
 * @author bubble-07
 *
 */
public class DefaultTestGenerator implements TestGenerator {
	
	int totalTests;
	int[] inputWidths;
	ChipInvoker invoker;
	
	public DefaultTestGenerator(ChipInvoker invoker) {
		int[] inputWidths = invoker.inputWidths();
		this.totalTests = (int) Math.min(GeneratorUtils.getProductSize(inputWidths), Config.maxAutoGenTests);
		this.inputWidths = inputWidths;
		this.invoker = invoker;
	}
	
	public DefaultTestGenerator.State initState() {
		return new State(inputWidths);
	}
	
	public static class State implements Serializable {
		private static final long serialVersionUID = 1L;
		
		private int[] inputWidths;
		boolean randomized = false;
		long testNum = 0;
		List<BusData> currentInputCase;
		
		public State(int[] inputWidths) {
			this.inputWidths = inputWidths;
			long numExhaustive = GeneratorUtils.getProductSize(inputWidths);
			if (numExhaustive > Config.maxAutoGenTests) {
				this.randomized = true;
			}
		}
		
		public List<BusData> getTestForIndex(long ind) {
			long[] toDeliver;
			if (this.randomized) {
				toDeliver = GeneratorUtils.randomizeLong(inputWidths);
			}
			else {
				toDeliver = GeneratorUtils.partitionLong(ind, inputWidths);
			}
			return GeneratorUtils.busLongs(toDeliver, inputWidths);
		}
	}
	
	public List<BusData> generate(State state) {
		
		state.currentInputCase = state.getTestForIndex(state.testNum);
		
		//Move the state along to the next test
		state.testNum++;
		
		return state.currentInputCase;
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
	public List<BusData> generate(Serializable state) {
		//TODO: plz help, static typing gods.
		return generate((State) state);
	}

	@Override
	public boolean test(Serializable state, List<BusData> outputs) {
		
		//We can't hope for this to work at all on sequential circuits.
		List<BusData> expectedOutputs = invoker.invoke(invoker.initState(), ((State) state).currentInputCase);
		
		return outputs.equals(expectedOutputs);
	}
}
