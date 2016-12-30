package com.circuits.circuitsmod.reflective;

import java.io.File;
import java.util.Optional;

public class ChipImpl {
	private final ChipInvoker.Provider invoker;
	private final Optional<TestGeneratorInvoker.Provider> testGen;
	public ChipImpl(ChipInvoker.Provider invoker, Optional<TestGeneratorInvoker.Provider> testGen) {
		this.invoker = invoker;
		this.testGen = testGen;
	}
	public ChipInvoker.Provider getInvoker() {
		return this.invoker;
	}
	public Optional<TestGeneratorInvoker.Provider> getTestGenerator() {
		return this.testGen;
	}
	
	public static Optional<ChipImpl> fromCircuitDirectory(File dir) {
		File testsFile = new File(dir.toString() + "/Tests.class");
		File implFile = new File(dir.toString() + "/Implementation.class");
		
		Optional<ChipInvoker.Provider> invoker = ChipInvoker.Provider.getProvider(implFile);
		Optional<TestGeneratorInvoker.Provider> testGen = Optional.empty();
		
		if (testsFile.exists()) {
			testGen = TestGeneratorInvoker.Provider.getProvider(testsFile);
		}
		
		if (!invoker.isPresent()) {
			return Optional.empty();
		}
		
		return Optional.of(new ChipImpl(invoker.get(), testGen));
	}

}
