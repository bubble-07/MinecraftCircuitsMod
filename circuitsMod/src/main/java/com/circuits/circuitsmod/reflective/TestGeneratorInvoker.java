package com.circuits.circuitsmod.reflective;

import java.io.File;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.circuits.circuitsmod.circuit.CircuitConfigOptions;
import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.common.FileUtils;
import com.circuits.circuitsmod.common.Log;
import com.google.common.collect.Lists;

/**
 * Similar to ChipInvoker, but used to reflectively invoke compiled
 * test case generator classes. Test case generators are only truly
 * necessary for sequential circuits, since combinational circuit tests
 * will be automatically generated for circuits in the circuit testing
 * code of the control block tile entity.
 * 
 * A Test Case Generator is a class with a no-argument constructor
 * (used to initialize the test case generator for a newly-initialized circuit state)
 * with the following methods:
 * 
 * boolean test(long o0, long o1, ... long oN) : update the state of the Test Case Generator for
 * a new redstone tick, and return true if the test is still passing at the current tick,
 * given that the tick yielded outputs o0 ... oN (all cast to long)
 * 
 * T0 input0();
 * [T1 input1()];
 * ...
 * TN inputN(): return the input corresponding to the index N in the order defined
 * by the corresponding Circuits API implementation file for the test case at the current tick.
 * The output of the circuit (in-game) will be tested against the result of running
 * the corresponding ChipInvoker on the list of inputs for consistency.
 * 
 * In the above, each TN is one of the primitive Java types accepted in the Circuits API
 * 
 * 
 * int numTests() : method which returns the total number of tests (test ticks) that will be ran on the circuit.
 * 
 * [boolean slowable()] : Optional method [default: assume returns true] which specifies whether
 * or not the ticks as used in the testing method can be taken to occur
 * slower -- that is, some integer multiple of the in-game redstone ticks, for 
 * the purposes of the test. 
 * 
 * 
 * @author bubble-07
 *
 */
public class TestGeneratorInvoker extends Invoker implements TestGenerator {
	
	/**
	 * Stores the "test" method
	 */
	private final Method testMethod;
	
	/**
	 * List of input methods
	 */
	private final List<Method> inputMethods;
	
	boolean slowable;
	
	int numTests;

	private TestGeneratorInvoker(Class<?> implClass, Method testMethod,
			List<Method> inputMethods, boolean slowable, int numTests, CircuitConfigOptions configOpts, String configName) {
		super(implClass, configOpts, configName);
		this.testMethod = testMethod;
		this.inputMethods = inputMethods;
		this.numTests = numTests;
		this.slowable = slowable;
	}
	
	/**
	 * Something that provides TestGeneratorInvokers given a list of config options
	 * @author bubble-07
	 *
	 */
	//TODO: Find a way to merge this with ChipInvoker#Provider?
	public static class Provider {
		Class<?> implClass;
		private Provider(Class<?> implClass) {
			this.implClass = implClass;
		}
		public static Optional<Provider> getProvider(File implFile) {
			Optional<Class<?>> clazz = ReflectiveUtils.loadClassFile(implFile, FileUtils.getCircuitLibDir(), "Tests");
			if (clazz.isPresent()) {
				return Optional.of(new Provider(clazz.get()));
			}
			return Optional.empty();
		}
		public Optional<TestGeneratorInvoker> getInvoker(CircuitConfigOptions configs) {
			return TestGeneratorInvoker.getInvoker(this.implClass, configs);
		}
	}
	
	public static Optional<TestGeneratorInvoker> getInvoker(Class<?> implClass, CircuitConfigOptions configOpts) {
		Consumer<String> error = (s) -> Log.userError("Class: " + implClass + " " + s);
		
		Optional<Object> instance = getInstance(implClass);
		if (!instance.isPresent()) {
			return Optional.empty();
		}
		
		Optional<String> configName = Invoker.initConfigs(instance.get(), configOpts);
		if (!configName.isPresent()) {
			return Optional.empty();
		}
		
		Optional<Method> slowableMethod = ReflectiveUtils.getMethodFromName(implClass, "slowable");
		boolean slowable = true;
		if (slowableMethod.isPresent()) {
			try {
				slowable = (boolean) slowableMethod.get().invoke(instance.get());
			}
			catch (Exception e) {
				error.accept("has an override to the slowable attribute, but the method is not formatted correctly");
				Log.info("Continuing with slowable=true");
			}
		}
		
		Optional<Method> numTestsMethod = ReflectiveUtils.getMethodFromName(implClass, "numTests");
		int numTests = -1;
		if (numTestsMethod.isPresent()) {
			try {
				numTests = (int) numTestsMethod.get().invoke(instance.get());
			}
			catch (Exception e) {
				error.accept("has an override to the numTests attribute, but the method is not formatted correctly");
				Log.info("Continuing assuming the number of tests isn't pre-determined");
			}
		}
		
		
		Optional<List<Method>> inputMethods = getSequentialMethods(implClass, "input");
		if (!inputMethods.isPresent()) {
			return Optional.empty();
		}
		Optional<Method> testMethod = ReflectiveUtils.getMethodFromName(implClass, "test");
		if (!testMethod.isPresent()) {
			error.accept("has no test method!");
			return Optional.empty();
		}
		return Optional.of(new TestGeneratorInvoker(implClass, testMethod.get(), 
				                                    inputMethods.get(), slowable, numTests,
				                                    configOpts, configName.get()));
	}

	@Override
	public boolean slowable() {
		return this.slowable;
	}

	@Override
	public int totalTests() {
		return this.numTests;
	}

	@Override
	public List<BusData> generate(Serializable state) {
		Object instance = ((Invoker.State)state).getWrapped();
		try {

			List<BusData> result = Lists.newArrayList();
			for (int i = 0; i < this.inputMethods.size(); i++) {
				Method valMethod = this.inputMethods.get(i);
				//No need to truncate. Why? We won't care, because we'll
				//just be feeding it to a native Java version anyway.
				result.add(bus(valMethod.invoke(instance)));
			}
			return result;
		}
		catch (Exception e) {
			Log.userError("Class: " + implClass + " does not have well-defined input-generating methods");
		}
		return Lists.newArrayList();
	}

	@Override
	public boolean test(Serializable state, List<BusData> outputs) {
		Object instance = ((Invoker.State)state).getWrapped();
		try {
			Object[] unBused = outputs.stream().map((b) -> b.getData()).toArray();
			
			return (boolean) this.testMethod.invoke(instance, unBused);
		}
		catch (Exception e) {
			Log.userError("Class: " + implClass + " does not have a well-defined test method");
			return false;
		}
	}

}
