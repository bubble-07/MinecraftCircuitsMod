package com.circuits.circuitsmod.controlblock.gui;

import com.circuits.circuitsmod.controlblock.ControlContainer;
import com.circuits.circuitsmod.controlblock.ControlTileEntity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class ControlGuiHandler implements IGuiHandler {
	
	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, 
			int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
		if (tileEntity instanceof ControlTileEntity) {
			return new ControlContainer(player.inventory, (ControlTileEntity) tileEntity);
		}
		return null;
	}
	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, 
			int x, int y, int z) {
		TileEntity tileEntity = world.getTileEntity(new BlockPos(x, y, z));
		if (tileEntity instanceof ControlTileEntity) {
			return new ControlGui(player.inventory, (ControlTileEntity) tileEntity);
		}
		return null;
	}
}
