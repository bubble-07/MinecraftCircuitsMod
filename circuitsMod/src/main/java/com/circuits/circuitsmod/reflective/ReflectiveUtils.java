package com.circuits.circuitsmod.reflective;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;

import com.circuits.circuitsmod.common.Log;

public class ReflectiveUtils {
	
	public static Optional<Class<?>> loadClassFile(File classFile, File libDir, String classname) {
		try {	
			
			URL[] urls = new URL[]{classFile.getParentFile().toURI().toURL(), libDir.toURI().toURL(), new File(libDir.toPath() + "/Utils.class").toURI().toURL()};
			
			ClassLoader cl = new URLClassLoader(urls);
			
			return Optional.of(cl.loadClass(classname));
		}
		catch (MalformedURLException e) {
			Log.internalError("Malformed Directory Path in ChipInvoker " + classFile);
		}
		catch (ClassNotFoundException e) {
			Log.userError("Cannot find the class " + classname + " in " + classFile.toString());
		}
		return Optional.empty();
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
	public static Optional<Method> getMethodFromName(Class<?> implClass, String methodName) {
		for (Method m : implClass.getMethods()) {
			if (m.getName().equals(methodName)) {
				return Optional.of(m);
			}
		}
		return Optional.empty();
	}
}
