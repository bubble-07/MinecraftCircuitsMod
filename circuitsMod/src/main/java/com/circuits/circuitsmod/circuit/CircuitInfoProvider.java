package com.circuits.circuitsmod.circuit;

import com.circuits.circuitsmod.circuitblock.WireDirectionMapper;
import com.circuits.circuitsmod.reflective.ChipInvoker;

import net.minecraft.util.ResourceLocation;

public class CircuitInfoProvider {
	public static void ensureServerModelInit() { 
		//TODO: Implement me!
	}
	
	public static boolean isServerModelInit() {
		return false;
	}
	
	public static ResourceLocation getTexture(CircuitUID uid) {
		return null;
	}
	
	public static ChipInvoker getInvoker(CircuitUID uid) {
		return null;
	}
	public static WireDirectionMapper getWireMapper(CircuitUID uid) {
		return null;
	}
}
