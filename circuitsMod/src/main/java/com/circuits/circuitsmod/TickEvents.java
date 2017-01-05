package com.circuits.circuitsmod;

import java.util.Collections;
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
	
	private boolean tickFlag = false;
	
	private Set<Runnable> actionsOne = Collections.newSetFromMap(new ConcurrentHashMap<>());
	private Set<Runnable> actionsTwo = Collections.newSetFromMap(new ConcurrentHashMap<>());

	private Set<Runnable> nextSet() {
		if (tickFlag) {
			return actionsTwo;
		}
		return actionsOne;
	}
	
	private Set<Runnable> currentSet() {
		if (tickFlag) {
			return actionsOne;
		}
		return actionsTwo;
	}
	
	public void addImmediateAction(Runnable action) {
		currentSet().add(action);
	}
	
	public void addDelayedAction(Runnable action) {
		nextSet().add(action);
	}
	
	
	@SubscribeEvent
	public void handleTick(WorldTickEvent e) {
		for (Runnable action : currentSet()) {
			action.run();
		}
		currentSet().clear();
		tickFlag = !tickFlag;
	}
}
