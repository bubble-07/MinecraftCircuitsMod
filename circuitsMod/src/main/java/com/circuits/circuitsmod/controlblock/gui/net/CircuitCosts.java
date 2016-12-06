package com.circuits.circuitsmod.controlblock.gui.net;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

import net.minecraft.item.ItemStack;

import com.circuits.circuitsmod.common.SerializableItemStack;

public class CircuitCosts implements Serializable {
	private static final long serialVersionUID = 1L;
	
	List<SerializableItemStack> stacks;
	public CircuitCosts(List<SerializableItemStack> stacks) {
		this.stacks = stacks;
	}
	public boolean isUnlocked() {
		return this.stacks != null;
	}
	public Optional<List<ItemStack>> getCost() {
		if (stacks == null) {
			return Optional.empty();
		}
		return Optional.of(SerializableItemStack.deserializeItemStacks(stacks));
	}
}
