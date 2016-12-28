package com.circuits.circuitsmod.tester;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.circuit.SpecializedCircuitInfo;
import com.circuits.circuitsmod.common.PosUtils;
import com.circuits.circuitsmod.controlblock.ControlTileEntity;
import com.circuits.circuitsmod.controlblock.StartupCommonControl;
import com.circuits.circuitsmod.controlblock.tester.net.TestStateUpdate;
import com.circuits.circuitsmod.frameblock.StartupCommonFrame;
import com.circuits.circuitsmod.recipes.RecipeDeterminer;
import com.circuits.circuitsmod.unbreakium.StartupCommonUnbreakium;
import com.circuits.circuitsmod.unbreakium.UnbreakiumBlock;

public class ControlBlockTester extends Tester<ControlTileEntity> {

	public ControlBlockTester(EntityPlayer player, ControlTileEntity parent,
			SpecializedCircuitInfo circuit, TestConfig config) {
		super(player, parent, circuit, config);
		setupUnbreakiumCage();
	}
	
	private Stream<BlockPos> unbreakiumCageBottomStream() {
		AxisAlignedBB bottomTestFace = PosUtils.getBBoxFacet(testbbox, EnumFacing.DOWN);
		
		Stream<AxisAlignedBB> bottomSides = Stream.of(EnumFacing.EAST, EnumFacing.WEST, EnumFacing.SOUTH, EnumFacing.NORTH)
		      .map((f) -> PosUtils.getBBoxFacet(bottomTestFace, f).offset(0.0, -1.0, 0.0));
		
		AxisAlignedBB bottomFace = bottomTestFace.offset(0.0, -2.0, 0.0);
		
		return Stream.concat(Stream.of(bottomFace), bottomSides).flatMap((bb) -> PosUtils.streamBlockPosIn(bb));
	}
	
	private Stream<BlockPos> unbreakiumFrameStream() {
		return PosUtils.extremalPosIn(testbbox).filter((pos) -> parent.getWorld().getBlockState(pos).getBlock() != StartupCommonControl.controlBlock);
	}
	
	private Stream<BlockPos> unbreakiumCageSidesStream() {
		

		
		Stream<AxisAlignedBB> otherSides = Stream.of(EnumFacing.values()).filter((f) -> !f.equals(EnumFacing.DOWN))
		                                         .map((f) -> PosUtils.shrinkBBox(PosUtils.getBBoxFacet(testbbox, f), 1.0));
		
		return otherSides.flatMap((bb) -> PosUtils.streamBlockPosIn(bb));
	}
	
	private Stream<BlockPos> unbreakiumCageStream() {
		return Stream.concat(unbreakiumCageBottomStream(), unbreakiumCageSidesStream());
	}
	
	private void setupUnbreakiumCage() {
		unbreakiumCageStream().forEach((pos) -> this.parent.getWorld().setBlockState(pos, StartupCommonUnbreakium.unbreakiumBlock.getDefaultState(), 3));
		unbreakiumFrameStream().forEach((pos) -> this.parent.getWorld().setBlockState(pos, 
				                                 StartupCommonUnbreakium.unbreakiumBlock.getDefaultState().withProperty(UnbreakiumBlock.FRAMEMIMIC, true), 3));
		
	}
	
	private void tearDownUnbreakiumCage() {
		unbreakiumCageSidesStream().forEach((pos) -> {
			this.parent.getWorld().setBlockToAir(pos);
		});
		unbreakiumCageBottomStream().forEach((pos) -> {
			this.parent.getWorld().setBlockState(pos, Blocks.STONE.getDefaultState(), 3);
		});
		unbreakiumFrameStream().forEach((pos) -> {
			this.parent.getWorld().setBlockState(pos, StartupCommonFrame.frameBlock.getDefaultState(), 3);
		});
	}
	
	public void cleanup() {
		tearDownUnbreakiumCage();
	}
	
	@Override
	public void successAction() {
		tearDownUnbreakiumCage();
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
	protected boolean checkForCheating() {
		List<EntityLivingBase> entities = this.getWorld().getEntitiesWithinAABB(EntityLivingBase.class, this.testbbox);
		return !entities.isEmpty();
	}
	
	@Override
	protected int timeToNextCheatCheck() {
		return 20;
	}
	
	@Override
	public Optional<AxisAlignedBB> getTestingBox() {
		//For now, must be placed in a bottom-most corner
		//TODO: Also check for transparent blocks extending in a 1 block shell!
		
		Block frameBlock = StartupCommonFrame.frameBlock;
		
		Predicate<BlockPos> isFrame = (pos) -> {
			IBlockState state = parent.getWorld().getBlockState(pos);
			return (state.getBlock() == StartupCommonFrame.frameBlock) || 
					(state.getBlock() == StartupCommonUnbreakium.unbreakiumBlock && state.getValue(UnbreakiumBlock.FRAMEMIMIC));
		};
		
		//Get the vertical extent
		int vertExtent = 0;
		while (isFrame.test(parent.getPos().up(vertExtent + 1))) {
			vertExtent++;
		}
		int pos_x_extent = 0;
		while (isFrame.test(parent.getPos().add(pos_x_extent + 1, 0, 0))) {
			pos_x_extent++;
		}
		int neg_x_extent = 0;
		while (isFrame.test(parent.getPos().add(-neg_x_extent - 1, 0, 0))) {
			neg_x_extent++;
		}
		int pos_z_extent = 0;
		while (isFrame.test(parent.getPos().add(0, 0, pos_z_extent + 1))) {
			pos_z_extent++;
		}
		int neg_z_extent = 0;
		while (isFrame.test(parent.getPos().add(0, 0, -neg_z_extent - 1))) {
			neg_z_extent++;
		}
				
		return Optional.of(new AxisAlignedBB(parent.getPos().add(-neg_x_extent, 0, -neg_z_extent), 
			     parent.getPos().add(pos_x_extent, vertExtent, pos_z_extent)));
	}

	@Override
	public void failureAction() {
		tearDownUnbreakiumCage();
	}

}
