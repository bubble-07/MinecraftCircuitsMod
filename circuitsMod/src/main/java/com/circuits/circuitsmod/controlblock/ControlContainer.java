package com.circuits.circuitsmod.controlblock;


import java.util.Optional;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.circuitblock.CircuitItem;
import com.circuits.circuitsmod.controlblock.tester.net.CraftingRequest;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ControlContainer extends Container {
	protected ControlTileEntity tileEntity;
	
	//adapted from http://www.minecraftforge.net/wiki/Containers_and_GUIs. Boilerplate is pain.
	
	public ControlContainer(InventoryPlayer inventoryPlayer, ControlTileEntity te) {
		tileEntity = te;
		
		for (int i=0; i < 2; i++) {
			for (int j = 0; j < 3; j++) {
				addSlotToContainer(new Slot(tileEntity, j + i * 3, 117 + j * 18, 42 + i * 18));
			}
		}
		
		addSlotToContainer(new Slot(tileEntity, 6, 117, 14));
		
		addSlotToContainer(new Slot(tileEntity, 7, 117 + 30, 8));
		
		bindPlayerInventory(inventoryPlayer);
		
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return tileEntity.isUseableByPlayer(player);
	}
	
	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
			}
		}
		for (int i =  0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 142));
		}
	}
	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slot) {
		ItemStack stack = null;
		Slot slotObject = (Slot) inventorySlots.get(slot);
		
		//null checks and checks if the item can be stacked (maxStackSize > 1)
		if (slotObject != null && slotObject.getHasStack()) {
			ItemStack stackInSlot = slotObject.getStack();
			stack = stackInSlot.copy(); 
			if (slot < tileEntity.getSizeInventory()) {
				if (!this.mergeItemStack(stackInSlot, tileEntity.getSizeInventory(), 
						36+tileEntity.getSizeInventory(), true)) {
					return null;
				}
			}
			else if (!this.mergeItemStack(stackInSlot, 0, tileEntity.getSizeInventory(), false)) {
				return null;
			}
			if (stackInSlot.stackSize == 0) {
				slotObject.putStack(null);
			}
			else {
				slotObject.onSlotChanged();
			}
			
			if (stackInSlot.stackSize == stack.stackSize) {
				return null;
			}
			slotObject.onPickupFromSlot(player, stackInSlot);
			if (slot == 7) {
				//Special handling! Need to remove from the other slots
				Optional<SpecializedCircuitUID> craftingCell = CircuitItem.getUIDFromStack(stack);
				if (craftingCell.isPresent()) {
					CircuitsMod.network.sendToServer(new CraftingRequest.Message(player.getUniqueID(), tileEntity.getPos(), stack.stackSize, 
							craftingCell.get()));
					//tileEntity.craftingSlotPickedUp(stack.stackSize);
					slotObject.onSlotChanged();
				}
			}
		}
		return stack;
	}
}
