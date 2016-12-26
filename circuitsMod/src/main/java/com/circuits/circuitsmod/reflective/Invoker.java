package com.circuits.circuitsmod.reflective;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.circuits.circuitsmod.circuit.CircuitConfigOptions;
import com.circuits.circuitsmod.common.ArrayUtils;
import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.common.Log;
import com.google.common.collect.Lists;

/**
 * Common base class for "invokers" -- objects
 * responsible for invoking methods from a reflectively-loaded
 * class based on the state contained in some object,
 * where the class is assumed to have a zero-argument constructor.
 * In the context of the mod, this means ChipInvoker and TestGeneratorInvoker.
 * 
 * @author bubble-07
 *
 */
public abstract class Invoker {
	
	/**
	 * Stores the class of the implementation (loaded reflectively)
	 */
	protected final Class<?> implClass;
	
	/**
	 * Stores the configuration options for the circuit being invoked. 
	 */
	protected final CircuitConfigOptions circuitConfigs;
	
	/**
	 * Stores the name of the particular configuration of this invoker.
	 */
	protected final String configName;
	
	public CircuitConfigOptions getConfigOptions() {
		return circuitConfigs;
	}
	
	public String getConfigName() {
		return configName;
	}
	
	
	protected Invoker(Class<?> implClass, CircuitConfigOptions configOpts, String configName) {
		this.implClass = implClass;
		this.circuitConfigs = configOpts;
		this.configName = configName;
	}
	
	public static class State implements Serializable {
		private static final long serialVersionUID = 1L;
		
		/**
		 * Stores an instantiated chip object.
		 */
		private Object instance;
		
		/**
		 * Given an Invoker initialize a state object.
		 * @param parent
		 */
		public State(Invoker parent) {
			instance = getInstance(parent.implClass).get();
			Invoker.initConfigs(instance, parent.circuitConfigs);
		}
		public Object getWrapped() {
			return this.instance;
		}
	}
	
	
	/**
	 * Convenience method to get a new state from this Invoker.
	 * @return
	 */
	public State initState() {
		return new State(this);
	}
	
	protected static int getNumConfigSlots(Class<?> clazz) {
		Optional<Method> configMethod = ReflectiveUtils.getMethodFromName(clazz, "config");
		if (!configMethod.isPresent()) {
			return 0;
		}
		return configMethod.get().getParameterCount();
	}
	
	/**
	 * Initializes the configuration of a given instance and returns the
	 * configuration name for that instance
	 */
	protected static Optional<String> initConfigs(Object instance, CircuitConfigOptions configs) {
		Optional<Method> configMethod = ReflectiveUtils.getMethodFromName(instance.getClass(), "config");
		if (!configMethod.isPresent()) {
			if (configs.asInts().length != 0) {
				Log.userError("Config method not specified for " + instance + " but we passed a non-empty configs list!");
				return Optional.empty();
			}
			//Otherwise, there must not be any configuration options for the circuit, after all!
			return Optional.of("");
		}
		Integer[] configArr = ArrayUtils.box(configs.asInts());
		String resultString = null;
		try {
			resultString = (String) configMethod.get().invoke(instance, (Object[])configArr);
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | ClassCastException e) {
			Log.userError("Unable to invoke config method for " + instance);
			return Optional.empty();
		}
		if (resultString == null) {
			return Optional.empty();
		}
		return Optional.of(resultString);
		
	}
	
	protected static Optional<Object> getInstance(Class<?> implClass) {
		Consumer<String> error = (s) -> Log.userError("Class: " + implClass + " " + s);
		
		try {
			Object instance = implClass.getConstructors()[0].newInstance();
			//TODO: Implement your own serialization routines/force the user to!
			//We can't just cast to Serializable, because we're dealing with a different classloader!
			//return Optional.of((Serializable) instance);
			return Optional.of(instance);
		}
		catch (ClassCastException e) {
			error.accept("is not serializable!");
		}
		catch (Exception e) {
			error.accept("has no zero-argument public constructor");
		}
		return Optional.empty();
	}
	
	/**
	 * If possible, get a list of sequential (starting from zero) methods named
	 * prefix + number, and return such a list. If they're not sequential, throw
	 * up an error and return Optional.empty();
	 * @param implClass
	 * @param prefix
	 * @return
	 */
	protected static Optional<List<Method>> getSequentialMethods(Class<?> implClass, String prefix) {
		Consumer<String> error = (s) -> Log.userError("Class: " + implClass + " " + s);
		
		List<Method> outputMethods = Lists.newArrayList();
		boolean hasMissing = false; //Becomes true when an output is missing
		boolean nonSequential = false; //Becomes true when outputs are non-sequential
		for (int i = 0; i < 3; i++) {
			Optional<Method> method = ReflectiveUtils.getMethodFromName(implClass, prefix + i);
			method.ifPresent((m) -> outputMethods.add(m));
			if (hasMissing && method.isPresent()) {
				nonSequential = true;
			}
			if (!method.isPresent()) {
				hasMissing = true;
			}
		}
		
		if (nonSequential) {
			error.accept("has a non-sequential " + prefix + " indices!");
			return Optional.empty();
		}
		return Optional.of(outputMethods);
	}
	
	
	/**
	 * Gets the bus width of a given class
	 * @param clazz
	 * @return
	 */
	protected static int getTypeWidth(Class<?> clazz) {
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
	public static Object unBus(BusData data) {
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
	
	protected static int getWidthOf(Class<?> type) {
		if (long.class.isAssignableFrom(type)) {
			return 64;
		}
		if (int.class.isAssignableFrom(type)) {
			return 32;
		}
		if (short.class.isAssignableFrom(type)) {
			return 16;
		}
		if (byte.class.isAssignableFrom(type)) {
			return 8;
		}
		if (boolean.class.isAssignableFrom(type)) {
			return 1;
		}
		return 0;
	}
	
	/**
	 * Given a (boxed) Java object, return BusData
	 */
	public static BusData bus(Object obj) {
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
}
