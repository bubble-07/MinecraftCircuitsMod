package com.circuits.circuitsmod.reflective;

import java.io.File;
import java.util.Optional;

public class ChipImpl {
	private final ChipInvoker invoker;
	private final Optional<TestGeneratorInvoker> testGen;
	public ChipImpl(ChipInvoker invoker, Optional<TestGeneratorInvoker> testGen) {
		this.invoker = invoker;
		this.testGen = testGen;
	}
	public ChipInvoker getInvoker() {
		return this.invoker;
	}
	public Optional<TestGeneratorInvoker> getTestGenerator() {
		return this.testGen;
	}
	
	public static Optional<ChipImpl> fromCircuitDirectory(File dir) {
		File testsFile = new File(dir.toString() + "/Tests.class");
		File implFile = new File(dir.toString() + "/Implementation.class");
		
		Optional<ChipInvoker> invoker = ChipInvoker.getInvoker(implFile);
		Optional<TestGeneratorInvoker> testGen = TestGeneratorInvoker.getInvoker(testsFile);
		
		if (!invoker.isPresent()) {
			return Optional.empty();
		}
		
		return Optional.of(new ChipImpl(invoker.get(), testGen));
	}

}
