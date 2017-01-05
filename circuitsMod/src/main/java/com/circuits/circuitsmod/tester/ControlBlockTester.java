package com.circuits.circuitsmod.tester;

import java.util.List;
import java.util.Optional;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.circuit.SpecializedCircuitInfo;
import com.circuits.circuitsmod.controlblock.ControlTileEntity;
import com.circuits.circuitsmod.controlblock.tester.net.SequenceReaderStateUpdate;
import com.circuits.circuitsmod.recipes.RecipeDeterminer;

public class ControlBlockTester extends Tester<ControlTileEntity> {
	
	UnbreakiumCageManager<ControlTileEntity> cageManager;

	public ControlBlockTester(EntityPlayer player, ControlTileEntity parent,
			SpecializedCircuitInfo circuit, TestConfig config) {
		super(player, parent, circuit, config);
		this.cageManager = new UnbreakiumCageManager<>(this);
		init();
		cageManager.setupUnbreakiumCage();
	}
	
	@Override
	public void cleanup() {
		cageManager.tearDownUnbreakiumCage();
	}
	
	@Override
	public void successAction() {
		cageManager.tearDownUnbreakiumCage();
		RecipeDeterminer.determineRecipe(this);
	}
	
	@Override
	public void stateUpdateAction() {
		parent.updateState(this.getState());
		if (!parent.getWorld().isRemote) {
			CircuitsMod.network.sendToAll(new SequenceReaderStateUpdate.Message(this.getState(), parent.getPos()));
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
		return cageManager.getTestingBox();
	}

	@Override
	public void failureAction() {
		cageManager.tearDownUnbreakiumCage();
	}

}
