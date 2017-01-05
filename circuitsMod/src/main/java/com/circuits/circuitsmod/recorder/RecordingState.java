package com.circuits.circuitsmod.recorder;

import java.util.List;

import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.tester.SequenceReaderState;
import com.circuits.circuitsmod.tester.TestConfig;

public class RecordingState extends SequenceReaderState {
	private static final long serialVersionUID = 1L;
	
	private final String circuitName;

	public RecordingState(String circuitName, int testindex, int numTests, boolean finished,
			boolean success, TestConfig config, List<BusData> inputCase, String failureReason) {
		super(testindex, numTests, finished, success, config, inputCase, failureReason);
		this.circuitName = circuitName;
	}
	
	public String getCircuitName() {
		return this.circuitName;
	}

}
