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
public class ChipInvoker {
	
	/**
	 * Stores the class of the implementation (loaded reflectively)
	 */
	protected final Class implClass;
	
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
	
	
	
	private ChipInvoker(Class implClass, Method tickMethod,
			List<Method> outputMethods, boolean isSequential,
			int[] outputWidths, int[] inputWidths) {
		super();
		this.implClass = implClass;
		this.tickMethod = tickMethod;
		this.outputMethods = outputMethods;
		this.isSequential = isSequential;
		this.outputWidths = outputWidths;
		this.inputWidths = inputWidths;
	}

	public static Optional<ChipInvoker> getInvoker(Class implClass) {
		Consumer<String> error = (s) -> Log.userError("Class: " + implClass + " " + s);
		
		//Create an instance of the implementation class to be able to
		//determine the results of calling the idempotent functions in the API
		Object instance = null;
		try {
			instance = implClass.getConstructors()[0].newInstance();
		}
		catch (Exception e) {
			error.accept("has no zero-argument public constructor");
			return Optional.empty();
		}
		
		
		Optional<Method> tickMethod = getMethodFromName(implClass, "tick");
		if (!tickMethod.isPresent()) {
			error.accept("has no tick method");
			return Optional.empty();
		}
		
		//Now, get a list of output methods.
		List<Method> outputMethods = Lists.newArrayList();
		for (int i = 0; i < 3; i++) {
			Optional<Method> method = getMethodFromName(implClass, "value" + i);
			method.ifPresent((m) -> outputMethods.add(m));
		}
		if (outputMethods.size() == 0) {
			error.accept("has no value methods");
			return Optional.empty();
		}
		
		//By default, set the input and output widths to the widths of the input/
		//output types
		int[] inputWidths = new int[tickMethod.get().getParameterCount()];
		int[] outputWidths = new int[outputMethods.size()];
		
		Class[] parameterTypes = tickMethod.get().getParameterTypes();
		
		//TODO: make this less verbose. Stream functions?
		for (int i = 0; i < parameterTypes.length; i++) {
			inputWidths[i] = getTypeWidth(parameterTypes[i]);
			if (inputWidths[i] == 0) {
				error.accept("has an undefined parameter type in tick");
				return Optional.empty();
			}
		}
		
		for (int i = 0; i < outputMethods.size(); i++) {
			outputWidths[i] = getTypeWidth(outputMethods.get(i).getReturnType());
			if (outputWidths[i] == 0) {
				error.accept("has an undefined return type in value" + i);
				return Optional.empty();
			}
		}
		
		//Great, now if overrides are present, modify the input and output widths
		//to reflect those instead.
		
		//TODO: abstract these two out with a lambda
		Optional<Method> outputWidthsMethod = getMethodFromName(implClass, "outputWidths");
		Optional<Method> inputWidthsMethod = getMethodFromName(implClass, "inputWidths");
		if (outputWidthsMethod.isPresent()) {
			try {
				Integer[] out = (Integer[]) outputWidthsMethod.get().invoke(instance);
				outputWidths = ArrayUtils.unbox(out);
			}
			catch (Exception e) {
				error.accept("has an output width override, but the method is not formatted correctly");
				Log.info("Continuing with default output widths");
			}
		}
		if (inputWidthsMethod.isPresent()) {
			try {
				Integer[] out = (Integer[]) inputWidthsMethod.get().invoke(instance);
				inputWidths = ArrayUtils.unbox(out);
			}
			catch (Exception e) {
				error.accept("has an input width override, but the method is not formatted correctly");
				Log.info("Continuing with default input widths");
			}
		}
		
		//Cool, now just determine whether or not it's a sequential circuit
		boolean isSequential = false;
		Optional<Method> isSequentialMethod = getMethodFromName(implClass, "isSequential");
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
				                          outputMethods, isSequential,
				                          outputWidths, inputWidths));
		
	}
	
	/**
	 * Given a class and a method's name, return the first method corresponding
	 * to that name (if any), otherwise, return Optional.empty. 
	 * Exists because Class.getMethod() requires specifying the argument types
	 * but we don't always want that.
	 * 
	 * @param implClass
	 * @param methodName
	 * @return
	 */
	private static Optional<Method> getMethodFromName(Class implClass, String methodName) {
		for (Method m : implClass.getMethods()) {
			if (m.getName().equals(methodName)) {
				return Optional.of(m);
			}
		}
		return Optional.empty();
	}
	
	
	
	public class ChipState {
		/**
		 * Stores an instantiated chip object.
		 */
		private Object instance;
		
		/**
		 * Given a ChipInvoker, initialize a chip state object.
		 * Used for all circuits, but only really useful for sequential ones.
		 * @param parent
		 */
		public ChipState(ChipInvoker parent) {
				try {
					instance = parent.implClass.getConstructors()[0].newInstance();
				} catch (InstantiationException | IllegalAccessException
						| IllegalArgumentException | InvocationTargetException
						| SecurityException e) {
					assert(true == false);
				}
		}
		public Object getWrapped() {
			return this.instance;
		}
	}
	
	
	/**
	 * Gets the bus width of a given class
	 * @param clazz
	 * @return
	 */
	private static int getTypeWidth(Class<?> clazz) {
		if (int.class.isAssignableFrom(clazz) || Integer.class.isAssignableFrom(clazz)) {
			return 32;
		}
		else if (long.class.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz)) {
			return 64;
		}
		else if (short.class.isAssignableFrom(clazz) || Short.class.isAssignableFrom(clazz)) {
			return 16;
		}
		else if (byte.class.isAssignableFrom(clazz) || Byte.class.isAssignableFrom(clazz)) {
			return 8;
		}
		else if (boolean.class.isAssignableFrom(clazz) || Boolean.class.isAssignableFrom(clazz)) {
			return 1;
		}
		Log.internalError("Cannot determine the width of type: " + clazz);
		return 0;
	}
	
	/**
	 * Given BusData, return the (boxed) Java object representation.
	 * @return
	 */
	private static Object unBus(BusData data) {
		switch (data.getWidth()) {
			case 64:
				return ((long) data.getData());
			case 32:
				return ((int) data.getData());
			case 16:
				return ((short) data.getData());
			case 1:
				return ((boolean) (data.getData() > 0));
			default:
				return ((byte) data.getData());
		}
	}
	
	/**
	 * Given a (boxed) Java object, return BusData
	 */
	private static BusData bus(Object obj) {
		if (obj instanceof Long) {
			return new BusData(64, (Long) obj);
		}
		else if (obj instanceof Integer) {
			return new BusData(32, (Integer) obj);
		}
		else if (obj instanceof Short) {
			return new BusData(16, (Short) obj);
		}
		else if (obj instanceof Byte) {
			return new BusData(8, (Byte) obj);
		}
		else if (obj instanceof Boolean) {
			return new BusData(1, ((Boolean) obj) ? 1 : 0);
		}
		return null;
	}
	
	
	/**
	 * Actually invoke the circuit's dynamically-loaded code. 
	 * If the dynamically-loaded code throws exceptions (which it shouldn't)
	 * this will just force all outputs to zero, effectively disabling the circuit.
	 * @param state
	 * @param inputValues
	 * @return
	 */
	public List<BusData> invoke(ChipState state, List<BusData> inputValues) {
		Object[] tickArgs = new Object[inputValues.size()];
		for (int i = 0; i < tickArgs.length; i++) {
			tickArgs[i] = unBus(inputValues.get(i));
		}
		
		try {
			Object instance = state.getWrapped();
			this.tickMethod.invoke(instance, tickArgs);
			List<BusData> result = Lists.newArrayListWithCapacity(this.outputWidths.length);
			for (Method valMethod : this.outputMethods) {
				result.add(bus(valMethod.invoke(instance)));
			}
			return result;
		}
		catch (Exception e) {
			return Arrays.stream(this.outputWidths)
					      .mapToObj((width) -> new BusData(width, 0))
					      .collect(Collectors.toList());
		}

	}
	
	
	
	private static Object invoke(Method m, Object target, Object... args) {
		try {
			return m.invoke(target, args);
		}
		catch (Exception e) {
			System.err.println(e);
		}
		return null;
	}
	
	private static Optional<Class> loadClassFile(File classFile, File libDir, String classname) {
		try {	
			
			URL[] urls = new URL[]{classFile.getParentFile().toURI().toURL(), libDir.toURI().toURL()};
			
			ClassLoader cl = new URLClassLoader(urls);
			
			return Optional.of(cl.loadClass(classname));
		}
		catch (MalformedURLException e) {
			Log.internalError("Malformed Directory Path in ChipInvoker " + classFile);
		}
		catch (ClassNotFoundException e) {
			Log.userError("Cannot find the class " + classname + " in " + classFile.toString());
		}
		return null;
	}
}
