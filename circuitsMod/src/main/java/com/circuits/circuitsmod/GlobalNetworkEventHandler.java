package com.circuits.circuitsmod;

import com.circuits.circuitsmod.circuit.CircuitInfoProvider;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

@Mod.EventBusSubscriber
public class GlobalNetworkEventHandler {
	@SubscribeEvent
	public void onEvent(FMLNetworkEvent.ClientDisconnectionFromServerEvent e) {
		CircuitInfoProvider.clearState();
	}
}
