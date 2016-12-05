package com.circuits.circuitsmod.reflective;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import com.circuits.circuitsmod.common.BusData;

public interface TestGenerator {
	public Optional<List<BusData>> invoke(Serializable state);
	public boolean slowable();
	public int totalTests();
	public Serializable initState();
}
