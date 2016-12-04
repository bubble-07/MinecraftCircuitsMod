package com.circuits.circuitsmod.controlblock.tester;

import java.io.Serializable;
import java.util.List;

import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.common.BusData;

//Testing state, as seen by the tile entity
public class TestState implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public int testindex;
	public int numTests;
	public SpecializedCircuitUID circuitUID;
	public TestConfig config;
	public boolean finished;
	public boolean success;
	List<BusData> inputCase;
	
	Serializable internalState;
	
	public TestState(SpecializedCircuitUID circuitUID, int testindex, int numTests, boolean finished, boolean success, TestConfig config,
			         Serializable internalState, List<BusData> inputCase) {
		this.circuitUID = circuitUID;
		this.testindex = testindex;
		this.numTests = numTests;
		this.config = config;
		this.finished = finished;
		this.success = success;
		this.internalState = internalState;
		this.inputCase = inputCase;
	}
}
