package com.circuits.circuitsmod;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

@Mod.EventBusSubscriber
public class TickEvents {
	private static TickEvents instance;
	public static TickEvents instance() {
		if (TickEvents.instance == null) {
			TickEvents.instance = new TickEvents();
		}
		return TickEvents.instance;
	}
	
	private Set<Runnable> actions = Collections.newSetFromMap(new ConcurrentHashMap<>());
	
	public void addAction(Runnable action) {
		actions.add(action);
	}
	
	
	@SubscribeEvent
	public void handleTick(WorldTickEvent e) {
		for (Runnable action : actions) {
			action.run();
		}
		actions.clear();
	}
}
