package com.circuits.circuitsmod.controlblock;


import java.util.Optional;

import javax.annotation.Nullable;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.circuitblock.CircuitItem;
import com.circuits.circuitsmod.controlblock.gui.net.CraftingRequest;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ControlContainer extends Container {
	protected ControlTileEntity tileEntity;
	
	//adapted from http://www.minecraftforge.net/wiki/Containers_and_GUIs. Boilerplate is pain.
	
	public ControlContainer(InventoryPlayer inventoryPlayer, ControlTileEntity te) {
		tileEntity = te;
		
		for (int i=0; i < 3; i++) {
			for (int j = 0; j < 2; j++) {
				addSlotToContainer(new Slot(tileEntity, i + j * 3, 136 + j * 18, 42 + i * 18));
			}
		}
		
		addSlotToContainer(new Slot(tileEntity, 6, 110, 14) {
			@Override
			public boolean canBeHovered() {
				//I'm too lazy to remove this slot lol
				return false;
			}
		});
		
		addSlotToContainer(new Slot(tileEntity, 7, 145, 10) {
			
		    public boolean isItemValid(@Nullable ItemStack stack)
		    {
		        return false;
		    }
			
			@Override
		    public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack) {
				//Special handling! Need to remove from the other slots
				if (playerIn.worldObj.isRemote) {
					Optional<SpecializedCircuitUID> craftingCell = CircuitItem.getUIDFromStack(stack);
					if (craftingCell.isPresent()) {
						CircuitsMod.network.sendToServer(new CraftingRequest.Message(playerIn.getUniqueID(), tileEntity.getPos(), stack.stackSize, 
								craftingCell.get()));
					}
				}
			}
		});
		
		bindPlayerInventory(inventoryPlayer);
		
	}
	
	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return tileEntity.isUseableByPlayer(player);
	}
	
	protected void bindPlayerInventory(InventoryPlayer inventoryPlayer) {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventoryPlayer, j + i * 9 + 9, 8 + j * 18, 102 + i * 18));
			}
		}
		for (int i =  0; i < 9; i++) {
			addSlotToContainer(new Slot(inventoryPlayer, i, 8 + i * 18, 160));
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

		}
		return stack;
	}
}
