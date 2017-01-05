package com.circuits.circuitsmod.tester;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import com.circuits.circuitsmod.common.BusData;

public abstract class SequenceReaderState implements Serializable {
	private static final long serialVersionUID = 1L;
	public int testindex;
	public int numTests;
	public TestConfig config;
	public boolean finished;
	public boolean success;
	private List<BusData> inputCase;
	private String failureReason;
	
	public SequenceReaderState(int testindex, int numTests, boolean finished, boolean success, TestConfig config,
			         List<BusData> inputCase, String failureReason) {
		this.testindex = testindex;
		this.numTests = numTests;
		this.config = config;
		this.finished = finished;
		this.success = success;
		this.inputCase = inputCase;
		this.failureReason = failureReason;
	}
	
	public Optional<String> getFailureReason() {
		return Optional.ofNullable(failureReason);
	}
	
	public List<BusData> getInputCase() {
		return this.inputCase;
	}
	public int getTick() {
		return this.testindex;
	}
}
