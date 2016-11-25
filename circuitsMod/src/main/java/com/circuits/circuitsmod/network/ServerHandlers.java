package com.circuits.circuitsmod.network;

import java.util.Optional;
import java.util.function.Consumer;
import java.lang.reflect.Method;

import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.reflective.ReflectiveUtils;

import net.minecraft.world.IBlockAccess;

public class ServerHandlers {
	public static void dispatch(TypedMessage msg, IBlockAccess worldIn) {
		Consumer<Class<?>> handleCase = (clazz) -> {
			if (clazz.isAssignableFrom(msg.getWrappedClass())) {
				Optional<Method> handleMethod = ReflectiveUtils.getMethodFromName(clazz, "handle");
				if (!handleMethod.isPresent()) {
					Log.internalError("Server dispatch: Handler not present for " + clazz);
					return;
				}
				try {
					handleMethod.get().invoke(null, clazz.cast(msg.getWrappedObject()), worldIn);
				}
				catch (Exception e) {
					Log.internalError("Server dispatch: Failed to dispatch handler method for " + clazz);
				}
			}
		};
		
		handleCase.accept(CircuitInfoProvider.ModelRequestFromClient.class);
	}
}
