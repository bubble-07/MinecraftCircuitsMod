package com.circuits.circuitsmod;

import com.circuits.circuitsmod.circuit.CircuitInfoProvider;

import net.minecraft.client.Minecraft;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber
public class WorldEvents { 
    @SubscribeEvent
    public void onEvent(WorldEvent.Unload e) {
    	if (e.getWorld() instanceof WorldServer) {
    		if (((WorldServer) e.getWorld()).provider.getDimensionType().equals(DimensionType.OVERWORLD)) {
    			CircuitInfoProvider.clearState();
    		}
    	}
    }
}
