package com.circuits.circuitsmod.reflective;

import java.io.Serializable;
import java.util.List;
import com.circuits.circuitsmod.common.BusData;

public interface TestGenerator {
	
	/**
	 * Generate circuit inputs for a new test case.
	 * @return
	 */
	public List<BusData> generate(Serializable state);
	/**
	 * Given a list of outputs, check them against the current test case's state
	 * @param state
	 * @param outputs
	 * @return
	 */
	public boolean test(Serializable state, List<BusData> outputs);
	
	public boolean slowable();
	public int totalTests();
	public Serializable initState();
}
