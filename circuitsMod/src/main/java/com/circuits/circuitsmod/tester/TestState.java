package com.circuits.circuitsmod.tester;

import java.util.List;

import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.common.BusData;

//Testing state, as seen by the tile entity
public class TestState extends SequenceReaderState {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private SpecializedCircuitUID circuitUID;
	
	public TestState(SpecializedCircuitUID circuitUID, int testindex, int numTests, boolean finished, boolean success, TestConfig config,
			         List<BusData> inputCase, String failureReason) {
		super(testindex, numTests, finished, success, config, inputCase, failureReason);
		this.circuitUID = circuitUID;
	}
	public SpecializedCircuitUID getUID() {
		return this.circuitUID;
	}
}
