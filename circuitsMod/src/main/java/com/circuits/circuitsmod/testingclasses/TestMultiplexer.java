package com.circuits.circuitsmod.testingclasses;
import com.circuits.circuitsmod.busblock.BusSegment;
import com.circuits.circuitsmod.common.BlockFace;
import com.circuits.circuitsmod.testblock.TileEntityTesting;

import net.minecraft.world.World;

public class TestMultiplexer implements PuzzleTest {

	BlockFace inputFace;
	BusSegment emitterSeg;
	TestCapsule capsule = new TestCapsule();

	@Override
	public TestTickResult test(World worldIn, TileEntityTesting testEntity) {
		System.out.println("Testing");
		TestTickResult testResult = new TestTickResult();

		switch (testEntity.testCounter) {
		case 1:
			TestingUtilityMethods.checkIfRedstoneSucceeds(testEntity, testResult, false);
			break;
		case 2:
			TestingUtilityMethods.checkIfRedstoneSucceeds(testEntity, testResult, false);
			break;
		case 3:
			TestingUtilityMethods.checkIfRedstoneSucceeds(testEntity, testResult, false);
			break;
		case 4:
			TestingUtilityMethods.checkIfRedstoneSucceeds(testEntity, testResult, true);
			break;
		}

		if (testEntity.testCounter >= 4) {
			testResult.setAtEndOfTest(true);
		}
		return testResult;
	}


	public void createInputData(TileEntityTesting testEntity) {
		TestCapsule capsule = TestingUtilityMethods.createInputData(testEntity);
		this.capsule = capsule;
		emitterSeg = capsule.emitterSeg;
		inputFace = capsule.inputFace;
		emitterSeg.addInput(inputFace);
	}

	public void setAndOutputData(World worldIn, int index) {
		TestingUtilityMethods.setAndOutputData(worldIn, capsule, index);
	}


}
