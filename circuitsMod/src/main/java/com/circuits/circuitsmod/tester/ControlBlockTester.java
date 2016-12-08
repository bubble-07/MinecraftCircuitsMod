package com.circuits.circuitsmod.tester;

import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.circuit.SpecializedCircuitInfo;
import com.circuits.circuitsmod.controlblock.ControlTileEntity;
import com.circuits.circuitsmod.controlblock.tester.net.TestStateUpdate;
import com.circuits.circuitsmod.frameblock.StartupCommonFrame;
import com.circuits.circuitsmod.recipes.RecipeDeterminer;

public class ControlBlockTester extends Tester<ControlTileEntity> {

	public ControlBlockTester(EntityPlayer player, ControlTileEntity parent,
			SpecializedCircuitInfo circuit, TestConfig config) {
		super(player, parent, circuit, config);
	}
	
	@Override
	public void successAction() {
		RecipeDeterminer.determineRecipe(this);
	}
	
	@Override
	public void stateUpdateAction() {
		parent.updateState(this.getState());
		if (!parent.getWorld().isRemote) {
			CircuitsMod.network.sendToAll(new TestStateUpdate.Message(this.getState(), parent.getPos()));
		}
	}
	
	@Override
	public Optional<AxisAlignedBB> getTestingBox() {
		//For now, must be placed in a bottom-most corner
		//TODO: Also check for transparent blocks extending in a 1 block shell!
		
		Block frameBlock = StartupCommonFrame.frameBlock;
		
		//Get the vertical extent
		int vertExtent = 0;
		while (parent.getWorld().getBlockState(parent.getPos().up(vertExtent + 1)).getBlock()
				== frameBlock) {
			vertExtent++;
		}
		int pos_x_extent = 0;
		while (parent.getWorld().getBlockState(parent.getPos().add(pos_x_extent + 1, 0, 0)).getBlock()
				== frameBlock) {
			pos_x_extent++;
		}
		int neg_x_extent = 0;
		while (parent.getWorld().getBlockState(parent.getPos().add(-neg_x_extent - 1, 0, 0)).getBlock()
				== frameBlock) {
			neg_x_extent++;
		}
		int pos_z_extent = 0;
		while (parent.getWorld().getBlockState(parent.getPos().add(0, 0, pos_z_extent + 1)).getBlock()
				== frameBlock) {
			pos_z_extent++;
		}
		int neg_z_extent = 0;
		while (parent.getWorld().getBlockState(parent.getPos().add(0, 0, -neg_z_extent - 1)).getBlock()
				== frameBlock) {
			neg_z_extent++;
		}
				
		return Optional.of(new AxisAlignedBB(parent.getPos().add(-neg_x_extent, 0, -neg_z_extent), 
			     parent.getPos().add(pos_x_extent, vertExtent, pos_z_extent)));
	}

	@Override
	public void failureAction() {
		System.out.println("Unimplemented");
	}

}
