package com.circuits.circuitsmod.controlblock;


import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.SpecializedCircuitInfo;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.circuitblock.CircuitItem;
import com.circuits.circuitsmod.common.ItemUtils;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.SerialUtils;
import com.circuits.circuitsmod.controlblock.gui.net.ServerGuiMessage;
import com.circuits.circuitsmod.frameblock.StartupCommonFrame;
import com.circuits.circuitsmod.recipes.RecipeDeterminer;
import com.circuits.circuitsmod.tester.ControlBlockTester;
import com.circuits.circuitsmod.tester.TestConfig;
import com.circuits.circuitsmod.tester.TestState;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.EnumPacketDirection;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

public class ControlTileEntity extends TileEntity implements IInventory, ITickable {
	public ItemStack[] inv;
	
	ControlBlockTester tester = null;
	TestState state = null;
	
	private SpecializedCircuitUID craftingCell = null;
	private UUID craftingPlayer = null;
	
	//TODO: I can't see a better way to send messages from the server back to the client than this.
	//is there in fact a better way?
	//TODO: should the results be in a queue? Some other structure?
	private HashMap<UUID, ServerGuiMessage> pendingGuiMessages = new HashMap<>();
	
	public void postGuiMessage(UUID player, ServerGuiMessage msg) {
		this.pendingGuiMessages.put(player, msg);
	}
	
	public Optional<ServerGuiMessage> getGuiMessage(UUID player) {
		ServerGuiMessage msg = this.pendingGuiMessages.get(player);
		if (msg != null) {
			this.pendingGuiMessages.remove(player);
		}
		return Optional.ofNullable(msg);
	}
	
	
	private final String name = "controltileentity";
	
	public ControlTileEntity() {
		inv = new ItemStack[8];
	}
	
	public ControlBlockTester getTester() {
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
		if (tester != null) {
			tester.cleanup();
		}
		tester = null;
		state = null;
	}
	public void unsetCraftingCell() {
		this.craftingCell = null;
		this.craftingPlayer = null;
	}
	
	public void setCraftingCell(UUID player, SpecializedCircuitUID craftingCell) {
		this.craftingPlayer = player;
		this.craftingCell = craftingCell;
	}
	public SpecializedCircuitUID getCraftingCell() {
		return this.craftingCell;
	}
	
	public void updateCraftingGrid() {
		if (craftingCell != null) {
			int numCraftable = numCraftable();
			//TODO: Set this to work with chips
			if (numCraftable != 0 && (inv[7] == null || inv[7].getItem() == Item.getItemFromBlock(StartupCommonFrame.frameBlock))) {
				inv[7] = getCircuitStack(craftingCell, numCraftable);
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
		Optional<List<ItemStack>> cost = getCost(craftingPlayer, craftingCell);
		if (!cost.isPresent()) {
			return 0;
		}
		for (ItemStack stack : cost.get()) {
			numCraftable = Math.min(numCraftable, getNumTimesIngredient(stack));
		}
		if (numCraftable == 9999) {
			numCraftable = 0;
		}
		return numCraftable;
	}
	
	public void craftingSlotPickedUp(int numCrafted) {
		//TODO: Make this __also__ use the item metadata, not just the item
		
		inv[7] = null;
		if (craftingCell != null) {
			List<ItemStack> totalCost = ItemUtils.mapOverQty(getCost(craftingPlayer, craftingCell).get(), (qty) -> (qty * numCrafted));
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
			
			
			player.inventory.addItemStackToInventory(getCircuitStack(craftingCell, numCrafted));
		}
	}
	
	private static ItemStack getCircuitStack(SpecializedCircuitUID uid, int numCrafted) {
		ItemStack result = CircuitItem.getStackFromUID(uid);
		result.stackSize = numCrafted;
		return result;
	}
	
	private Optional<List<ItemStack>> getCost(UUID craftingPlayer, SpecializedCircuitUID uid) {
		return RecipeDeterminer.getRecipeFor(getWorld(), craftingPlayer, uid.getUID());
	}
	
	public void updateState(TestState newState) {
		this.state = newState;
	}
	
	public World getWorld() {
		return this.worldObj;
	}
	
	//Server-only
	public void startTest(UUID playerId, SpecializedCircuitUID circuitUID, TestConfig config) {
		
		EntityPlayer player = getWorld().getPlayerEntityByUUID(playerId);
		
		Optional<SpecializedCircuitInfo> circuit = CircuitInfoProvider.getSpecializedInfoFor(circuitUID);
		if (!circuit.isPresent()) {
			Log.internalError("Circuit entry not present! " + circuitUID);
			return;
		}
		this.tester = new ControlBlockTester(player, this, circuit.get(), config);
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
		if (worldObj.isRemote) return;
		final IBlockState state = getWorld().getBlockState(getPos());
		getWorld().notifyBlockUpdate(getPos(), state, state, 3);
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
		
		this.pendingGuiMessages = (HashMap<UUID, ServerGuiMessage>) 
				                 SerialUtils.fromByteArray(getTileData().getByteArray("PendingGuiMessages"));
		
		this.state = (TestState) SerialUtils.fromByteArray(getTileData().getByteArray("TestState"));
		
		this.craftingCell = (SpecializedCircuitUID) SerialUtils.fromByteArray(getTileData().getByteArray("CraftingCell"));
		this.craftingPlayer = (UUID) SerialUtils.fromByteArray(getTileData().getByteArray("CraftingPlayer"));
		
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
		
		NBTTagCompound tileData = tagCompound.getCompoundTag("ForgeData");
		
		//TODO: This is problematic -- this should only write to the passed tagCompound
		//okay, so this is problematic, but well, uh technically, nah
		this.getTileData().setByteArray("TestState", SerialUtils.toByteArray(this.state));
		this.getTileData().setByteArray("PendingGuiMessages", SerialUtils.toByteArray(this.pendingGuiMessages));
		this.getTileData().setByteArray("CraftingCell", SerialUtils.toByteArray(this.craftingCell));
		this.getTileData().setByteArray("CraftingPlayer", SerialUtils.toByteArray(this.craftingPlayer));
		
		
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
	public NBTTagCompound getUpdateTag()
	{
		NBTTagCompound tag = super.getUpdateTag();
		tag.setTag("CircuitsUpdateData", writeToNBT(new NBTTagCompound()));
		return tag;
	}

	@Override
	public void handleUpdateTag(NBTTagCompound tag)
	{
		super.handleUpdateTag(tag);
		NBTTagCompound updateData = tag.getCompoundTag("CircuitsUpdateData");
		readFromNBT(updateData);
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(getPos(), getBlockMetadata(), getUpdateTag());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		if (net.getDirection() == EnumPacketDirection.CLIENTBOUND) {
			readFromNBT(pkt.getNbtCompound());		
		}
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
