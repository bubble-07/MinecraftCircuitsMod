package com.circuits.circuitsmod.testingclasses;

import com.circuits.circuitsmod.testblock.TileEntityTesting;

import net.minecraft.world.World;

public interface PuzzleTest {
	
	public TestTickResult test(World worldIn, TileEntityTesting testEntity);
	public void createInputData(TileEntityTesting testEntity);
	//public void checkIfStillSucceeding(TileEntityTesting testEntity, TestTickResult testResult, boolean isSuccessPowered);
	public void setAndOutputData(World worldIn, int index);
	//public void determineOverallSuccess(TestTickResult testResult, TileEntityTesting testEntity);
	
}
