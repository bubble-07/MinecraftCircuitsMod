package com.circuits.circuitsmod.reflective;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.circuits.circuitsmod.common.ArrayUtils;
import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.common.FileUtils;
import com.circuits.circuitsmod.common.Log;
import com.google.common.collect.Lists;

/**
 * Class used to reflectively invoke circuit implementations
 * 
 * A circuit implementation is a compiled Java class with a no-arg constructor
 * and the following method signatures:
 * 
 * void tick(I0, (, I1, I2));
 * T0 value0();
 * (T1 value1();)
 * (T2 value2();)
 * (int[] inputWidths();)
 * (int[] outputWidths();)
 * (boolean isSequential(););
 * 
 * Where T0, T1, T2, I0, I1, and I2 are each one of {boolean, byte, short, int, long},
 * and anything enclosed in parentheses is optional. 
 * 
 * This will be interpreted as follows:
 * "tick" is responsible for updating the instantiated circuit object's internal data
 * to deliver output with respect to the passed inputs, where each input (Ii) is interpreted
 * to be of the same bus width as the bit width of the type Ii, unless overridden by "inputWidths"
 * Overrides are intended __only__ for specifying 2-bit and 4-bit buses with an underlying
 * type of "byte" -- attempting to resize any other type will result in undefined behavior.
 * 
 * and valuek() is responsible for delivering the outputs of a circuit according
 * to the internal state computed in tick() following the same bus conventions as
 * the input (this time, with widths overridable by specifying an "outputWidths" method).
 * 
 * "isSequential()" returns "true" if the circuit is sequential, and "false" if the circuit is
 * combinational. The default (method doesn't exist) is combinational.
 * 
 * "inputWidths", "outputWidths", and "isSequential" are all required to be idempotent
 *
 * 
 * @author bubble-07
 *
 */
@SuppressWarnings("unused")
public class ChipInvoker extends Invoker {
	
	/**
	 * Stores the "tick" method
	 */
	private final Method tickMethod;
	
	/**
	 * List of output methods
	 */
	private final List<Method> outputMethods;
	
	private final boolean isSequential;
	private final int[] outputWidths;
	private final int[] inputWidths;
	
	
	
	private ChipInvoker(Class<?> implClass, Method tickMethod,
			List<Method> outputMethods, boolean isSequential,
			int[] outputWidths, int[] inputWidths) {
		super(implClass);
		this.tickMethod = tickMethod;
		this.outputMethods = outputMethods;
		this.isSequential = isSequential;
		this.outputWidths = outputWidths;
		this.inputWidths = inputWidths;
	}
	
	public static Optional<ChipInvoker> getInvoker(File implFile) {
		Optional<Class<?>> clazz = ReflectiveUtils.loadClassFile(implFile, FileUtils.getCircuitLibDir(), "Implementation");
		if (clazz.isPresent()) {
			return getInvoker(clazz.get());
		}
		return Optional.empty();
	}

	public static Optional<ChipInvoker> getInvoker(Class<?> implClass) {
		Consumer<String> error = (s) -> Log.userError("Class: " + implClass + " " + s);
		
		//Create an instance of the implementation class to be able to
		//determine the results of calling the idempotent functions in the API
		Optional<Object> instance = getInstance(implClass);
		if (!instance.isPresent()) {
			return Optional.empty();
		}
		
		Optional<Method> tickMethod = ReflectiveUtils.getMethodFromName(implClass, "tick");
		if (!tickMethod.isPresent()) {
			error.accept("has no tick method");
			return Optional.empty();
		}
		
		//Now, get a list of output methods.
		Optional<List<Method>> outputMethods = getSequentialMethods(implClass, "value");
		if (!outputMethods.isPresent()) {
			return Optional.empty();
		}
		
		if (ReflectiveUtils.getMethodFromName(implClass, "value3").isPresent()) {
			error.accept("has more than three output faces");
			return Optional.empty();
		}
		
		if (outputMethods.get().size() == 0) {
			error.accept("has no value methods");
			return Optional.empty();
		}
		
		if (tickMethod.get().getParameterCount() < 0) {
			error.accept("has no inputs");
			return Optional.empty();
		}
		if (tickMethod.get().getParameterCount() > 3) {
			error.accept("has too many inputs");
			return Optional.empty();
		}
		if (tickMethod.get().getParameterCount() + outputMethods.get().size() > 4) {
			error.accept("has too many wires");
			return Optional.empty();
		}
		
		//By default, set the input and output widths to the widths of the input/
		//output types
		int[] inputWidths = new int[tickMethod.get().getParameterCount()];
		int[] outputWidths = new int[outputMethods.get().size()];
		
		Class<?>[] parameterTypes = tickMethod.get().getParameterTypes();
		 
		//TODO: make this less verbose. Stream functions?
		for (int i = 0; i < parameterTypes.length; i++) {
			inputWidths[i] = getTypeWidth(parameterTypes[i]);
			if (inputWidths[i] == 0) {
				error.accept("has an undefined parameter type in tick");
				return Optional.empty();
			}
		}
		
		for (int i = 0; i < outputMethods.get().size(); i++) {
			outputWidths[i] = getTypeWidth(outputMethods.get().get(i).getReturnType());
			if (outputWidths[i] == 0) {
				error.accept("has an undefined return type in value" + i);
				return Optional.empty();
			}
		}
		
		//Great, now if overrides are present, modify the input and output widths
		//to reflect those instead.
		
		//TODO: abstract these two out with a lambda
		Optional<Method> outputWidthsMethod = ReflectiveUtils.getMethodFromName(implClass, "outputWidths");
		Optional<Method> inputWidthsMethod = ReflectiveUtils.getMethodFromName(implClass, "inputWidths");
		if (outputWidthsMethod.isPresent()) {
			try {
				outputWidths = (int[]) outputWidthsMethod.get().invoke(instance.get());
			}
			catch (Exception e) {
				error.accept("has an output width override, but the method is not formatted correctly");
				Log.info("Continuing with default output widths");
			}
		}
		if (inputWidthsMethod.isPresent()) {
			try {
				inputWidths = (int[]) inputWidthsMethod.get().invoke(instance.get());
			}
			catch (Exception e) {
				error.accept("has an input width override, but the method is not formatted correctly");
				Log.info("Continuing with default input widths");
			}
		}
		
		//Cool, now just determine whether or not it's a sequential circuit
		boolean isSequential = false;
		Optional<Method> isSequentialMethod = ReflectiveUtils.getMethodFromName(implClass, "isSequential");
		if (isSequentialMethod.isPresent()) {
			try {
				isSequential = (Boolean) isSequentialMethod.get().invoke(instance);
			}
			catch (Exception e) {
				error.accept("specifies isSequential, but the method is not formatted correctly");
				Log.info("Continuing, assuming the circuit is combinational");
			}
		}
		//Okay, so in theory, if we made it this far, we can just spit out a ChipInvoker.
		//So we do so.
		return Optional.of(new ChipInvoker(implClass, tickMethod.get(),
				                          outputMethods.get(), isSequential,
				                          outputWidths, inputWidths));
		
	}
	
	public int[] outputWidths() {
		return this.outputWidths;
	}
	
	public int[] inputWidths() {
		return this.inputWidths;
	}
	
	public int numInputs() {
		return this.inputWidths.length;
	}
	
	public int numOutputs() {
		return this.outputWidths.length;
	}
	
	public boolean isSequential() {
		return this.isSequential;
	}
	
	private static int[] indicesOfOnes(int[] orig) {
		int count = 0;
		for (int i = 0; i < orig.length; i++) {
			if (orig[i] == 1) {
				count++;
			}
		}
		int[] result = new int[count];
		int j = 0;
		for (int i = 0; i < orig.length; i++) {
			if (orig[i] == 1) {
				result[j] = i;
				j++;
			}
		}
		return result;
	}
	
	/**
	 * Returns an array of indices to those inputs
	 * which have a declared input width of 1
	 */
	public int[] getRedstoneInputs() {
		return indicesOfOnes(this.inputWidths);
	}
	
	public int[] getRedstoneOutputs() {
		return indicesOfOnes(this.outputWidths);
	}
	
	
	/**
	 * Actually invoke the circuit's dynamically-loaded code. 
	 * If the dynamically-loaded code throws exceptions (which it shouldn't)
	 * this will just force all outputs to zero, effectively disabling the circuit.
	 * @param state
	 * @param inputValues
	 * @return
	 */
	public List<BusData> invoke(Invoker.State state, List<BusData> inputValues) {
		Object[] tickArgs = new Object[inputValues.size()];
		for (int i = 0; i < tickArgs.length; i++) {
			tickArgs[i] = unBus(inputValues.get(i));
		}
		
		try {
			Object instance = state.getWrapped();
			this.tickMethod.invoke(instance, tickArgs);
			List<BusData> result = Lists.newArrayListWithCapacity(this.outputWidths.length);
			for (int i = 0; i < this.outputMethods.size(); i++) {
				Method valMethod = this.outputMethods.get(i);
				result.add(bus(valMethod.invoke(instance))
						   .truncate(this.outputWidths[i]));
			}
			return result;
		}
		catch (Exception e) {
			return Arrays.stream(this.outputWidths)
					      .mapToObj((width) -> new BusData(width, 0))
					      .collect(Collectors.toList());
		}

	}
}
