package com.circuits.circuitsmod.testingclasses;

import com.circuits.circuitsmod.busblock.BusSegment;
import com.circuits.circuitsmod.common.BlockFace;
import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.testblock.TileEntityTesting;

import net.minecraft.world.World;

public interface PuzzleTest {
	
	public TestTickResult test(World worldIn, TileEntityTesting testEntity);
	
}
