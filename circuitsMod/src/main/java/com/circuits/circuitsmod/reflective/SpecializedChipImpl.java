package com.circuits.circuitsmod.reflective;

import java.util.Optional;

import com.circuits.circuitsmod.circuit.CircuitConfigOptions;

/**
 * A ChipImpl after both the test generator and the implementation have been specialized by
 * a list of CircuitConfigOptions
 * @author bubble-07
 *
 */
public class SpecializedChipImpl {
	
	private ChipInvoker invoker;
	private Optional<TestGeneratorInvoker> testInvoker;
	private ChipImpl oldImpl;
	
	public ChipInvoker getInvoker() {
		return this.invoker;
	}
	public Optional<TestGeneratorInvoker> getTestGenerator() {
		return this.testInvoker;
	}
	
	public static Optional<SpecializedChipImpl> of(ChipImpl impl, CircuitConfigOptions configs) {
		Optional<TestGeneratorInvoker> testInvoker = impl.getTestGenerator().flatMap(p -> p.getInvoker(configs));
		Optional<ChipInvoker> chipInvoker = impl.getInvoker().getInvoker(configs);
		if (!chipInvoker.isPresent()) {
			return Optional.empty();
		}
		return Optional.of(new SpecializedChipImpl(chipInvoker.get(), testInvoker, impl));
	}
	
	private SpecializedChipImpl(ChipInvoker invoker, Optional<TestGeneratorInvoker> testInvoker, ChipImpl oldImpl) {
		this.invoker = invoker;
		this.testInvoker = testInvoker;
		this.oldImpl = oldImpl;
	}
}
