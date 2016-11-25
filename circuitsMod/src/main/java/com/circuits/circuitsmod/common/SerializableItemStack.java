package com.circuits.circuitsmod.common;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class SerializableItemStack implements Serializable {
	private static final long serialVersionUID = 1L;
	public int stackSize;
	public int itemDamage;
	public int id;
	
	public SerializableItemStack(ItemStack in) {
		this.id = Item.getIdFromItem(in.getItem());
		this.stackSize = in.stackSize;
		this.itemDamage = in.getItemDamage();
	}
	
	public ItemStack toItemStack() {
		return new ItemStack(Item.getItemById(id), stackSize, itemDamage);
	}
	
	public static List<SerializableItemStack> serializeItemStacks(List<ItemStack> in) {
		return in.stream().map(SerializableItemStack::new).collect(Collectors.toCollection(ArrayList::new));
	}
	
	public static List<ItemStack> deserializeItemStacks(List<SerializableItemStack> in) {
		return in.stream().map(SerializableItemStack::toItemStack).collect(Collectors.toCollection(ArrayList::new));
	}
	
	public static List<ItemStack> itemStacksFromFile(File file) throws IOException {
		List<ItemStack> result = new ArrayList<>();
		
		NBTTagCompound cpd = CompressedStreamTools.read(file);
		NBTTagList lerst = (NBTTagList) cpd.getTag("Stacks");
		for (int i = 0; i < lerst.tagCount(); i++) {
			NBTTagCompound itemCpd = (NBTTagCompound) lerst.get(i);
			ItemStack stack = ItemStack.loadItemStackFromNBT(itemCpd);
			result.add(stack);
		}
		return result;
	}
	public static void itemStacksToFile(List<ItemStack> stacks, File file) throws IOException {
		NBTTagCompound cpd = new NBTTagCompound();
		NBTTagList lerst = new NBTTagList();
		for (ItemStack item : stacks) {
			NBTTagCompound itemCpd = new NBTTagCompound();
			item.writeToNBT(itemCpd);
			lerst.appendTag(itemCpd);
		}
		cpd.setTag("Stacks", lerst);
		
		CompressedStreamTools.write(cpd, file);
	}
}
