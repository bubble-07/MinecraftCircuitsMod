package minecraftcircuits.controlblock;

import java.util.Arrays;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;

/**
 * User: brandon3055
 * Date: 06/01/2015
 *
 * This is a simple tile entity implementing IInventory that can store 9 item stacks
 */
public class TileEntityInventoryBasic extends TileEntity implements IInventory {
	// Create and initialize the items variable that will store store the items
	final int NUMBER_OF_SLOTS = 9;
	private ItemStack[] itemStacks = new ItemStack[NUMBER_OF_SLOTS];

	/* The following are some IInventory methods you are required to override */

	// Gets the number of slots in the inventory
	@Override
	public int func_70302_i_() {
		return itemStacks.length;
	}

	// Gets the stack in the given slot
	@Override
	public ItemStack func_70301_a(int slotIndex) {
		return itemStacks[slotIndex];
	}

	/**
	 * Removes some of the units from itemstack in the given slot, and returns as a separate itemstack
 	 * @param slotIndex the slot number to remove the items from
	 * @param count the number of units to remove
	 * @return a new itemstack containing the units removed from the slot
	 */
	@Override
	public ItemStack func_70298_a(int slotIndex, int count) {
		ItemStack itemStackInSlot = func_70301_a(slotIndex);
		if (itemStackInSlot == null) return null;

		ItemStack itemStackRemoved;
		if (itemStackInSlot.field_77994_a <= count) {
			itemStackRemoved = itemStackInSlot;
			func_70299_a(slotIndex, null);
		} else {
			itemStackRemoved = itemStackInSlot.func_77979_a(count);
			if (itemStackInSlot.field_77994_a == 0) {
				func_70299_a(slotIndex, null);
			}
		}
	  func_70296_d();
		return itemStackRemoved;
	}

	// overwrites the stack in the given slotIndex with the given stack
	@Override
	public void func_70299_a(int slotIndex, ItemStack itemstack) {
		itemStacks[slotIndex] = itemstack;
		if (itemstack != null && itemstack.field_77994_a > func_70297_j_()) {
			itemstack.field_77994_a = func_70297_j_();
		}
		func_70296_d();
	}

	// This is the maximum number if items allowed in each slot
	// This only affects things such as hoppers trying to insert items you need to use the container to enforce this for players
	// inserting items via the gui
	@Override
	public int func_70297_j_() {
		return 64;
	}

	// Return true if the given player is able to use this block. In this case it checks that
	// 1) the world tileentity hasn't been replaced in the meantime, and
	// 2) the player isn't too far away from the centre of the block
	@Override
	public boolean func_70300_a(EntityPlayer player) {
		if (this.field_145850_b.func_175625_s(this.field_174879_c) != this) return false;
		final double X_CENTRE_OFFSET = 0.5;
		final double Y_CENTRE_OFFSET = 0.5;
		final double Z_CENTRE_OFFSET = 0.5;
		final double MAXIMUM_DISTANCE_SQ = 8.0 * 8.0;
		return player.func_70092_e(field_174879_c.func_177958_n() + X_CENTRE_OFFSET, field_174879_c.func_177956_o() + Y_CENTRE_OFFSET, field_174879_c.func_177952_p() + Z_CENTRE_OFFSET) < MAXIMUM_DISTANCE_SQ;
	}

	// Return true if the given stack is allowed to go in the given slot.  In this case, we can insert anything.
	// This only affects things such as hoppers trying to insert items you need to use the container to enforce this for players
	// inserting items via the gui
	@Override
	public boolean func_94041_b(int slotIndex, ItemStack itemstack) {
		return true;
	}

	// This is where you save any data that you don't want to lose when the tile entity unloads
	// In this case, it saves the itemstacks stored in the container
	@Override
	public NBTTagCompound func_189515_b(NBTTagCompound parentNBTTagCompound)
	{
		super.func_189515_b(parentNBTTagCompound); // The super call is required to save and load the tileEntity's location

		// to use an analogy with Java, this code generates an array of hashmaps
		// The itemStack in each slot is converted to an NBTTagCompound, which is effectively a hashmap of key->value pairs such
		//   as slot=1, id=2353, count=1, etc
		// Each of these NBTTagCompound are then inserted into NBTTagList, which is similar to an array.
		NBTTagList dataForAllSlots = new NBTTagList();
		for (int i = 0; i < this.itemStacks.length; ++i) {
			if (this.itemStacks[i] != null)	{
				NBTTagCompound dataForThisSlot = new NBTTagCompound();
				dataForThisSlot.func_74774_a("Slot", (byte) i);
				this.itemStacks[i].func_77955_b(dataForThisSlot);
				dataForAllSlots.func_74742_a(dataForThisSlot);
			}
		}
		// the array of hashmaps is then inserted into the parent hashmap for the container
		parentNBTTagCompound.func_74782_a("Items", dataForAllSlots);
		// return the NBT Tag Compound
		return parentNBTTagCompound;
	}

	// This is where you load the data that you saved in writeToNBT
	@Override
	public void func_145839_a(NBTTagCompound parentNBTTagCompound)
	{
		super.func_145839_a(parentNBTTagCompound); // The super call is required to save and load the tiles location
		final byte NBT_TYPE_COMPOUND = 10;       // See NBTBase.createNewByType() for a listing
		NBTTagList dataForAllSlots = parentNBTTagCompound.func_150295_c("Items", NBT_TYPE_COMPOUND);

		Arrays.fill(itemStacks, null);           // set all slots to empty
		for (int i = 0; i < dataForAllSlots.func_74745_c(); ++i) {
			NBTTagCompound dataForOneSlot = dataForAllSlots.func_150305_b(i);
			int slotIndex = dataForOneSlot.func_74771_c("Slot") & 255;

			if (slotIndex >= 0 && slotIndex < this.itemStacks.length) {
				this.itemStacks[slotIndex] = ItemStack.func_77949_a(dataForOneSlot);
			}
		}
	}

	// set all slots to empty
	@Override
	public void func_174888_l() {
		Arrays.fill(itemStacks, null);
	}

	// will add a key for this container to the lang file so we can name it in the GUI
	@Override
	public String func_70005_c_() {
		return "container.mbe30_inventory_basic.name";
	}

	@Override
	public boolean func_145818_k_() {
		return false;
	}

	// standard code to look up what the human-readable name is
	@Override
	public ITextComponent func_145748_c_() {
		return this.func_145818_k_() ? new TextComponentString(this.func_70005_c_()) : new TextComponentTranslation(this.func_70005_c_());
	}

	// -----------------------------------------------------------------------------------------------------------
	// The following methods are not needed for this example but are part of IInventory so they must be implemented

	/**
	 * This method removes the entire contents of the given slot and returns it.
	 * Used by containers such as crafting tables which return any items in their slots when you close the GUI
	 * @param slotIndex
	 * @return
	 */
	@Override
	public ItemStack func_70304_b(int slotIndex) {
		ItemStack itemStack = func_70301_a(slotIndex);
		if (itemStack != null) func_70299_a(slotIndex, null);
		return itemStack;
	}

	@Override
	public void func_174889_b(EntityPlayer player) {}

	@Override
	public void func_174886_c(EntityPlayer player) {}

	@Override
	public int func_174887_a_(int id) {
		return 0;
	}

	@Override
	public void func_174885_b(int id, int value) {}

	@Override
	public int func_174890_g() {
		return 0;
	}
}
