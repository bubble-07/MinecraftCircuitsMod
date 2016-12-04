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
 * boolean tick() : update the state of the Test Case Generator for
 * a new redstone tick. Return true if there are more ticks left in the testing sequence.
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
 * [boolean slowable()] : Optional method [default: assume returns true] which specifies whether
 * or not the ticks as used in the testing method can be taken to occur
 * slower -- that is, some integer multiple of the in-game redstone ticks, for 
 * the purposes of the test. 
 * 
 * [int numTests()] : method which returns the total number of tests that will be ran on the circuit,
 * or -1 if the number of runnable tests cannot be determined a priori. Default return value is -1.
 * Defining this method is necessary to yield a progress bar in the control tile entity tests
 * 
 * 
 * @author bubble-07
 *
 */
public class TestGeneratorInvoker extends Invoker implements TestGenerator {
	
	/**
	 * Stores the "tick" method
	 */
	private final Method tickMethod;
	
	/**
	 * List of input methods
	 */
	private final List<Method> inputMethods;
	
	boolean slowable;
	
	int numTests;

	private TestGeneratorInvoker(Class<?> implClass, Method tickMethod,
			List<Method> inputMethods, boolean slowable, int numTests, CircuitConfigOptions configOpts, String configName) {
		super(implClass, configOpts, configName);
		this.tickMethod = tickMethod;
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
		
		Optional<Serializable> instance = getInstance(implClass);
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
				slowable = (boolean) slowableMethod.get().invoke(instance);
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
				numTests = (int) numTestsMethod.get().invoke(instance);
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
		Optional<Method> tickMethod = ReflectiveUtils.getMethodFromName(implClass, "tick");
		if (!tickMethod.isPresent()) {
			error.accept("has no tick method!");
			return Optional.empty();
		}
		return Optional.of(new TestGeneratorInvoker(implClass, tickMethod.get(), 
				                                    inputMethods.get(), slowable, numTests,
				                                    configOpts, configName.get()));
	}
	
	/**
	 * Given the state of a test generator, return a list of BusData inputs for the current 
	 * logical tick. Otherwise, if there are no ticks remaining, return Optional.empty().
	 * Flawed tests will just return Optional.empty(), but a message will be added to the log
	 * -- then the test's dev has to deal with it!
	 *
	 * @param state
	 * @return
	 */
	public Optional<List<BusData>> invoke(Invoker.State state) {
		try {
			Object instance = state.getWrapped();
			boolean moreTicks = (boolean) this.tickMethod.invoke(instance);
			if (!moreTicks) {
				return Optional.empty(); //Done testing
			}
			List<BusData> result = Lists.newArrayList();
			for (int i = 0; i < this.inputMethods.size(); i++) {
				Method valMethod = this.inputMethods.get(i);
				//No need to truncate. Why? We won't care, because we'll
				//just be feeding it to a native Java version anyway.
				result.add(bus(valMethod.invoke(instance)));
			}
			return Optional.of(result);
		}
		catch (Exception e) {
			Log.userError("Class: " + implClass + " does not have well-defined testing methods");
			return Optional.empty();
		}
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
	public Optional<List<BusData>> invoke(Serializable state) {
		return invoke((State) state);
	}

}
