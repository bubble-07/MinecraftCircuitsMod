package com.circuits.circuitsmod.network;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import net.minecraft.world.IBlockAccess;

import com.circuits.circuitsmod.TickEvents;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.reflective.ReflectiveUtils;

public abstract class Handlers {
	public void dispatch(TypedMessage msg, IBlockAccess worldIn) {
		Consumer<Class<?>> handleCase = (clazz) -> {
			if (msg.asTaggedObject() != null && msg.getWrappedObject() != null && msg.getWrappedClass() != null) {
			if (clazz.isAssignableFrom(msg.getWrappedClass())) {
				Optional<Method> handleMethod = ReflectiveUtils.getMethodFromName(clazz, "handle");
				if (!handleMethod.isPresent()) {
					Log.internalError(getHandlerName() + " dispatch: Handler not present for " + clazz);
					return;
				}
				TickEvents.instance().addImmediateAction(() -> {
					try {

						handleMethod.get().invoke(null, clazz.cast(msg.getWrappedObject()), worldIn);
					}
					catch (Exception e) {
						Log.internalError(getHandlerName() + " dispatch: Failed to dispatch handler method for " + clazz);
					}
				});

			}
			}
		};
		
		for (Class<?> clazz : getRequestKinds()) {
			handleCase.accept(clazz);
		}
	}
	
	public abstract String getHandlerName();
	
	public abstract List<Class<?>> getRequestKinds();
}
