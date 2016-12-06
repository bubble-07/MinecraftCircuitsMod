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
	private TestGenerator testGen;
	private ChipImpl oldImpl;
	
	public ChipInvoker getInvoker() {
		return this.invoker;
	}
	public TestGenerator getTestGenerator() {
		return this.testGen;
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
		if (testInvoker.isPresent()) {
			this.testGen = testInvoker.get();
		}
		else {
			this.testGen = new DefaultTestGenerator(invoker.inputWidths());
		}
		this.oldImpl = oldImpl;
	}
}
