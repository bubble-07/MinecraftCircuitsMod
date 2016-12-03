package com.circuits.circuitsmod.controlblock.frompoc;


import java.util.List;
import java.util.Optional;

import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.SpecializedCircuitInfo;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.circuitblock.CircuitItem;
import com.circuits.circuitsmod.common.ItemUtils;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.SerialUtils;
import com.circuits.circuitsmod.controlblock.tester.TestConfig;
import com.circuits.circuitsmod.controlblock.tester.TestState;
import com.circuits.circuitsmod.controlblock.tester.Tester;
import com.circuits.circuitsmod.frameblock.StartupCommonFrame;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ControlTileEntity extends TileEntity implements IInventory, ITickable {
	public ItemStack[] inv;
	
	Tester tester = null;
	TestState state = null;
	
	private SpecializedCircuitInfo craftingCell = null;
	
	private final String name = "controltileentity";
	
	public ControlTileEntity() {
		inv = new ItemStack[8];
	}
	
	public Tester getTester() {
		return tester;
	}
	
	public TestState getState() {
		return this.state;
	}
	
	public boolean testInProgress() {
		if (tester != null) {
			return tester.testInProgress();
		}
		return false;
	}
	public void stopTest() {
		tester = null;
		state = null;
	}
	
	public void setCraftingCell(SpecializedCircuitInfo craftingCell) {
		this.craftingCell = craftingCell;
	}
	public SpecializedCircuitInfo getCraftingCell() {
		return this.craftingCell;
	}
	
	private void updateCraftingGrid() {
		if (craftingCell != null) {
			int numCraftable = numCraftable();
			//TODO: Set this to work with chips
			if (numCraftable != 0 && (inv[7] == null || inv[7].getItem() == Item.getItemFromBlock(StartupCommonFrame.frameBlock))) {
				inv[7] = getCircuitStack(craftingCell.getUID(), numCraftable);
			}
			this.markDirty();
		}
	}
	
	private int getNumTimesIngredient(ItemStack stack) {
		float accum = 0;
		for (int i = 0; i < 5; i++) {
			if (inv[i] != null && inv[i].getItem() == stack.getItem()) {
				accum += ((float)inv[i].stackSize) / ((float)stack.stackSize);
			}
		}
		return (int)Math.floor(accum);
	}
	
	public int numCraftable() {
		int numCraftable = 9999;
		Optional<List<ItemStack>> cost = craftingCell.getInfo().getCost();
		if (!cost.isPresent()) {
			return 0;
		}
		for (ItemStack stack : cost.get()) {
			numCraftable = Math.min(numCraftable, getNumTimesIngredient(stack));
		}
		return numCraftable;
	}
	
	public void craftingSlotPickedUp(int numCrafted) {
		//TODO: Make this __also__ use the item metadata, not just the item
		
		inv[7] = null;
		if (craftingCell != null) {
			List<ItemStack> totalCost = ItemUtils.mapOverQty(craftingCell.getInfo().getCost().get(), (qty) -> (qty * numCrafted));
			for (ItemStack cost : totalCost) {
				for (int i = 0; i < 5; i++) {
					if (inv[i] != null && cost != null && cost.getItem() == inv[i].getItem()) {
						int sub = Math.min(inv[i].stackSize, cost.stackSize);
						cost.stackSize -= sub;
						inv[i].stackSize -= sub;
						if (inv[i].stackSize == 0) {
							inv[i] = null;
						}
					}
				}
			}
			
			//TODO: Get rid of this stupid, ugly hack. I don't want to deal with overriding slot, tho
			//TODO: Maybe a better way would be to send the player requesting?
			
			List<EntityPlayer> playersInRange = getWorld().getPlayers(EntityPlayer.class, (Object p) -> (
					((EntityPlayer) p).getDistanceSq(getPos()) < 25));
			playersInRange.sort((one, two) ->
				(int)((one).getDistanceSq(getPos()) - ((two).getDistanceSq(getPos()))));
			EntityPlayer player = playersInRange.get(0);
			
			
			player.inventory.addItemStackToInventory(getCircuitStack(craftingCell.getUID(), numCrafted));
		}
	}
	
	private static ItemStack getCircuitStack(SpecializedCircuitUID uid, int numCrafted) {
		ItemStack result = CircuitItem.getStackFromUID(uid);
		result.stackSize = numCrafted;
		return result;
	}
	
	public void updateState(TestState newState) {
		this.state = newState;
	}
	
	public World getWorld() {
		return this.worldObj;
	}
	
	//Server-only
	public void startTest(SpecializedCircuitUID circuitUID, TestConfig config) {
		
		Optional<SpecializedCircuitInfo> circuit = CircuitInfoProvider.getSpecializedInfoFor(circuitUID);
		if (!circuit.isPresent()) {
			Log.internalError("Circuit entry not present! " + circuitUID);
			return;
		}
		this.tester = new Tester(this, circuit.get(), config);
		this.state = tester.getState();
	}
	
	public float getTestProgress() {
		if (state == null) {
			return 0;
		}
		return (float) state.testindex / (float) state.numTests;
	}
	
	@Override
	public void update() {
		if (tester != null) {
			tester.update();
		}
		updateCraftingGrid();
	}

	@Override
	public int getSizeInventory() {
		return inv.length;
	}
	@Override
	public ItemStack getStackInSlot(int slot) {
		return inv[slot];
	}
	
	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inv[slot] = stack;
		if (stack != null && stack.stackSize > getInventoryStackLimit()) {
			stack.stackSize = getInventoryStackLimit();
		}
	}
	
	@Override
	public ItemStack decrStackSize(int slot, int amt) {
		ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			if (stack.stackSize < amt) {
				setInventorySlotContents(slot, null);
			}
			else {
				stack = stack.splitStack(amt);
				if (stack.stackSize == 0) {
					setInventorySlotContents(slot, null);
				}
			}
		}
		return stack;
	}
	
	@Override
	public int getInventoryStackLimit() {
		return 64;
	}
	
	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
		//Don't get why it'd be anything else
		return true;
	}
	
	@Override
	public void openInventory(EntityPlayer player) {
	}
	
	@Override
	public void closeInventory(EntityPlayer player) {
	}
	
	@Override
	public void readFromNBT(NBTTagCompound tagCompound) {
		super.readFromNBT(tagCompound);
		
		this.state = (TestState) SerialUtils.fromByteArray(getTileData().getByteArray("TestState"));
		
		NBTTagList tagList = tagCompound.getTagList("Inventory", 10);
		for (int i = 0; i < tagList.tagCount(); i++) {
			NBTTagCompound tag = (NBTTagCompound) tagList.getCompoundTagAt(i);
			byte slot = tag.getByte("Slot");
			if (slot >= 0 && slot < inv.length) {
				inv[slot] = ItemStack.loadItemStackFromNBT(tag);
			}
		}
	}
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
		
		this.getTileData().setByteArray("TestState", SerialUtils.toByteArray(this.state));
		
		super.writeToNBT(tagCompound);
		NBTTagList itemList = new NBTTagList();
		for (int i = 0; i < inv.length; i++) {
			ItemStack stack = inv[i];
			if (stack != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setByte("Slot", (byte) i);
				stack.writeToNBT(tag);
				itemList.appendTag(tag);
			}
		}
		tagCompound.setTag("Inventory", itemList);
		return tagCompound;
	}
	@Override
	public boolean isItemValidForSlot(int slot, ItemStack itemStack) {
		return true;
	}
	@Override
	public void clear() {
		//NO-OP
	}
	@Override
	public int getField(int id) {
		//NO-OP
		return 0;
	}
	@Override
	public void setField(int id, int val) {
		//NO-OP
	}
	@Override
	public int getFieldCount() {
		return 0;
	}
	@Override
	public boolean hasCustomName() {
		return true;
	}
	@Override
	public String getName() {
		return name;
	}
	@Override
	public ITextComponent getDisplayName() {
		return new TextComponentString("Control");
	}

	@Override
	public ItemStack removeStackFromSlot(int slot) {
		ItemStack stack = getStackInSlot(slot);
		if (stack != null) {
			setInventorySlotContents(slot, null);
		}
		return stack;
	}
}
