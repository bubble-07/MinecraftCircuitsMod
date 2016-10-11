package com.circuits.circuitsmod.reflective;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.common.Log;
import com.google.common.collect.Lists;

/**
 * Common base class for "invokers" -- objects
 * responsible for invoking methods from a reflectively-loaded
 * class based on the state contained in some object,
 * where the class is assumed to have a zero-argument constructor.
 * In the context of the mod, this means ChipInvoker and TestGeneratorInvoker
 * 
 * @author bubble-07
 *
 */
public abstract class Invoker {
	
	/**
	 * Stores the class of the implementation (loaded reflectively)
	 */
	protected final Class<?> implClass;
	
	protected Invoker(Class<?> implClass) {
		this.implClass = implClass;
	}
	
	public static class State {
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
	
	protected static Optional<Object> getInstance(Class<?> implClass) {
		Consumer<String> error = (s) -> Log.userError("Class: " + implClass + " " + s);
		
		try {
			return Optional.of(implClass.getConstructors()[0].newInstance());
		}
		catch (Exception e) {
			error.accept("has no zero-argument public constructor");
			return Optional.empty();
		}
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
	protected static Object unBus(BusData data) {
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
	protected static BusData bus(Object obj) {
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
