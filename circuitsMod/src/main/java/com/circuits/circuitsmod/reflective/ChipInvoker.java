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
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.circuits.circuitsmod.circuit.CircuitConfigOptions;
import com.circuits.circuitsmod.common.ArrayUtils;
import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.common.FileUtils;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.Pair;
import com.circuits.circuitsmod.common.StreamUtils;
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
 * (boolean[] analogInputs(););
 * (boolean[] analogOutputs(););
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
 * For each input/output, it's also possible to declare whether or not the input/output
 * is an analog input/output (analogInputs()/analogOutputs()). If analog(Inputs/Outputs)()[i]
 * is true, then the input/output at position i will be a redstone input/output where the power
 * level of the connected wire is identified with the corresponding input/output value to the circuit
 * implementation. Any input/output position declared to be analog must have a 4-bit width (byte datatype)
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
	
	private final boolean[] analogInputs;
	private final boolean[] analogOutputs;
	
	private final int[] redstoneInputs;
	private final int[] redstoneOutputs;
	
	/**
	 * Something that provides ChipInvokers given a list of config options
	 * @author bubble-07
	 *
	 */
	public static class Provider {
		Class<?> implClass;
		private Provider(Class<?> implClass) {
			this.implClass = implClass;
		}
		public static Optional<Provider> getProvider(File implFile) {
			Optional<Class<?>> clazz = ReflectiveUtils.loadClassFile(implFile, FileUtils.getCircuitLibDir(), "Implementation");
			if (clazz.isPresent()) {
				return Optional.of(new Provider(clazz.get()));
			}
			return Optional.empty();
		}
		public Optional<ChipInvoker> getInvoker(CircuitConfigOptions configs) {
			return ChipInvoker.getInvoker(this.implClass, configs);
		}
	}
	
	
	private ChipInvoker(Class<?> implClass, Method tickMethod,
			List<Method> outputMethods, boolean isSequential,
			int[] outputWidths, int[] inputWidths, boolean[] analogInputs, boolean[] analogOutputs,
			CircuitConfigOptions configOpts, String configName) {
		super(implClass, configOpts, configName);
		this.tickMethod = tickMethod;
		this.outputMethods = outputMethods;
		this.isSequential = isSequential;
		this.outputWidths = outputWidths;
		this.inputWidths = inputWidths;
		this.analogInputs = analogInputs;
		this.analogOutputs = analogOutputs;
		
		BiFunction<int[], boolean[], int[]> redstoneIndices = (widths, analog) -> {
			return ArrayUtils.unbox(
					Stream.concat(indicesOfOnes(widths), indicesOfTrue(analog))
					      .distinct().toArray(Integer[]::new));
		};
		
		this.redstoneInputs = redstoneIndices.apply(inputWidths, analogInputs);
		this.redstoneOutputs = redstoneIndices.apply(outputWidths, analogOutputs);
	}

	public static Optional<ChipInvoker> getInvoker(Class<?> implClass, CircuitConfigOptions configs) {
		Consumer<String> error = (s) -> Log.userError("Class: " + implClass + " " + s);
		
		//Create an instance of the implementation class to be able to
		//determine the results of calling the idempotent functions in the API
		Optional<Object> instance = getInstance(implClass);
		if (!instance.isPresent()) {
			return Optional.empty();
		} 
		Optional<String> configName = initConfigs(instance.get(), configs);
		if (!configName.isPresent()) {
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
		
		BiFunction<String, int[], int[]> widthOverride = (name, old) -> {
			Optional<Method> widthsMethod = ReflectiveUtils.getMethodFromName(implClass, name);
			if (widthsMethod.isPresent()) {
				try {
					int[] result = (int[]) widthsMethod.get().invoke(instance.get());
					
					if (result.length != old.length) {
						error.accept("has an " + name + " override, but the method returns an array of the wrong size");
						Log.info("Continuing with default widths");
						return old;
					}
					
					return result;
				}
				catch (Exception e) {
					error.accept("has an " + name + " override, but the method is not formatted correctly");
					Log.info("Continuing with default widths");
				}
			}
			return old;
		};
		
		outputWidths = widthOverride.apply("outputWidths", outputWidths);
		inputWidths = widthOverride.apply("inputWidths", inputWidths);
		
		//Cool, now determine whether or not any of the input/outputs are analog
		
		BiFunction<String, int[], boolean[]> analogOverride = (name, corrWidths) -> {
			Optional<Method> analogMethod = ReflectiveUtils.getMethodFromName(implClass, name);
			if (analogMethod.isPresent()) {
				try {
					boolean[] result = (boolean[]) analogMethod.get().invoke(instance.get());
					if (result.length != corrWidths.length) {
						error.accept("has an " + name + " override, but the method returns an array of the wrong size");
						Log.info("Continuing, assuming everything is digital");
						return new boolean[corrWidths.length];
					}
					//Okay, cool. Now check to make sure that every analog position has a width of 4
					for (int i = 0; i < result.length; i++) {
						if (result[i] && corrWidths[i] != 4) {
							error.accept("has an " + name + " override, but position " + i + " is not 4-bit!");
							Log.info("Continuing, assuming everything is digital");
							return new boolean[corrWidths.length];
						}
					}
					return result;
				}
				catch (Exception e) {
					error.accept("has an " + name + " override, but the method is not formatted correctly");
					Log.info("Continuing, assuming everything is digital");
				}
			}
			return new boolean[corrWidths.length];
		};
		boolean[] analogInputs = analogOverride.apply("analogInputs", inputWidths);
		boolean[] analogOutputs = analogOverride.apply("analogOutputs", outputWidths);
		
		//Cool, now just determine whether or not it's a sequential circuit
		boolean isSequential = false;
		Optional<Method> isSequentialMethod = ReflectiveUtils.getMethodFromName(implClass, "isSequential");
		if (isSequentialMethod.isPresent()) {
			try {
				isSequential = (Boolean) isSequentialMethod.get().invoke(instance.get());
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
				                          outputWidths, inputWidths, analogInputs, analogOutputs,
				                          configs, configName.get()));
		
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
	
	public boolean[] analogInputs() {
		return this.analogInputs;
	}
	public boolean[] analogOutputs() {
		return this.analogOutputs;
	}
	
	private static Stream<Integer> indicesOfOnes(int[] orig) {
		return StreamUtils.indexStream(Arrays.stream(orig).mapToObj((i) -> i))
		           .filter((p) -> p.second().intValue() == 1)
		           .map(Pair::first);
	}
	
	private static Stream<Integer> indicesOfTrue(boolean[] orig) {
		return StreamUtils.indexStream(StreamUtils.fromArray(orig))
		           .filter((p) -> p.second())
		           .map(Pair::first);
	}
	
	/**
	 * Returns an array of indices to those inputs
	 * which have a declared input width of 1 or are analog
	 */
	public int[] getRedstoneInputs() {
		return this.redstoneInputs;
	}
	
	public int[] getRedstoneOutputs() {
		return this.redstoneOutputs;
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
			int formalWidth = Invoker.getWidthOf(this.tickMethod.getParameterTypes()[i]);
			BusData oldBus = inputValues.get(i);
			BusData modBus = new BusData(Math.max(formalWidth, oldBus.getWidth()), oldBus.getData());
			tickArgs[i] = unBus(modBus);
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
