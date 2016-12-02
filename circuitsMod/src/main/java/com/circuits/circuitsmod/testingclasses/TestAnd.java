package com.circuits.circuitsmod.testingclasses;
import com.circuits.circuitsmod.busblock.BusSegment;
import com.circuits.circuitsmod.common.BlockFace;
import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.testblock.TileEntityTesting;

import net.minecraft.world.World;

public class TestAnd implements PuzzleTest {

	int testCounter = 1;
	
	@Override
	public TestTickResult test(World worldIn, TileEntityTesting testEntity) {
		TestTickResult testResult = new TestTickResult();
		BusSegment segment = testEntity.getBusSegment();
		BlockFace inputFace = testEntity.getInputFace();
		
		switch (testCounter) {
		case 1:
				setAndOutputData(worldIn, segment, inputFace, 0);
				checkIfStillSucceeding(testEntity, testResult, inputFace, false);
				break;
		case 2:
				setAndOutputData(worldIn, segment, inputFace, 1);
				checkIfStillSucceeding(testEntity, testResult, inputFace, false); 
			break;
		case 3:
				setAndOutputData(worldIn, segment, inputFace, 2);
				checkIfStillSucceeding(testEntity, testResult, inputFace, false);
			break;
		case 4:
			setAndOutputData(worldIn, segment, inputFace, 3);
			checkIfStillSucceeding(testEntity, testResult, inputFace, true);
			break;
		}
		testCounter++;
		determineOverallSuccess(testResult);
		return testResult;
	}

	private void checkIfStillSucceeding(TileEntityTesting testEntity, TestTickResult testResult, BlockFace inputFace, boolean isNegated) {
		if (!isNegated) {
			if (TileEntityTesting.isSidePowered(testEntity, inputFace.getFacing())) {
				testResult.setCurrentlySucceeding(false);
			}
		} else {
			if (!TileEntityTesting.isSidePowered(testEntity, inputFace.getFacing())) {
				testResult.setCurrentlySucceeding(false);
			}
		}
	}

	private void setAndOutputData(World worldIn, BusSegment segment, BlockFace inputFace, int index) {
		BusData testingData;
		testingData = new BusData(4, index);
		segment.accumulate(worldIn, inputFace, testingData);
		segment.forceUpdate(worldIn);
	}

	private void determineOverallSuccess(TestTickResult testResult) {
		if (testCounter > 4 && testResult.getCurrentlySucceeding())
			testResult.setAtEndOfTest(true);
		else if (!testResult.getCurrentlySucceeding() || testCounter > 4)
			testCounter = 0;
	}

}
