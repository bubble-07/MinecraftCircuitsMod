package com.circuits.circuitsmod.network;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Consumer;

import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.reflective.ReflectiveUtils;

import net.minecraft.world.IBlockAccess;

public class ClientHandlers {
	public static void dispatch(TypedMessage msg) {
		Consumer<Class<?>> handleCase = (clazz) -> {
			if (clazz.isAssignableFrom(msg.getWrappedClass())) {
				Optional<Method> handleMethod = ReflectiveUtils.getMethodFromName(clazz, "handle");
				if (!handleMethod.isPresent()) {
					Log.internalError("Client dispatch: Handler not present for " + clazz);
					return;
				}
				try {
					handleMethod.get().invoke(null, clazz.cast(msg.getWrappedObject()));
				}
				catch (Exception e) {
					Log.internalError("Client dispatch: Failed to dispatch handler method for " + clazz);
				}
			}
		};
	}
}
