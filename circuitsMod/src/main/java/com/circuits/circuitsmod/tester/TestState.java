package com.circuits.circuitsmod.tester;

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
	
	public TestState(SpecializedCircuitUID circuitUID, int testindex, int numTests, boolean finished, boolean success, TestConfig config,
			         List<BusData> inputCase) {
		this.circuitUID = circuitUID;
		this.testindex = testindex;
		this.numTests = numTests;
		this.config = config;
		this.finished = finished;
		this.success = success;
		this.inputCase = inputCase;
	}
	
	public List<BusData> getInputCase() {
		return this.inputCase;
	}
	public int getTick() {
		return this.testindex;
	}
}
