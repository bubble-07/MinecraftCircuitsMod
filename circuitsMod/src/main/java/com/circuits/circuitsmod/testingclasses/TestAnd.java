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
		BusData testingData;
		
		switch (testCounter) {
		case 1:
				testingData = new BusData(4, 0);
				segment.accumulate(worldIn, inputFace, testingData);
				if (TileEntityTesting.isSidePowered(testEntity, inputFace.getFacing())) {
					testResult.setCurrentlySucceeding(false);
				}
				break;
		case 2:
				testingData = new BusData(4, 1);
				segment.accumulate(worldIn, inputFace, testingData);
				if (TileEntityTesting.isSidePowered(testEntity, inputFace.getFacing())) {
					testResult.setCurrentlySucceeding(false);
				} 
			break;
		case 3:
				testingData = new BusData(4, 2);
				segment.accumulate(worldIn, inputFace, testingData);
				if (TileEntityTesting.isSidePowered(testEntity, inputFace.getFacing())) {
					testResult.setCurrentlySucceeding(false);
				}
			break;
		case 4:
				testingData = new BusData(4, 3);
				segment.accumulate(worldIn, inputFace, testingData);
				if (!TileEntityTesting.isSidePowered(testEntity, inputFace.getFacing())) {
					testResult.setCurrentlySucceeding(false);
				}
			break;
		}
		testCounter++;
		if (testCounter > 4 && testResult.getCurrentlySucceeding())
			testResult.setAtEndOfTest(true);
		else if (!testResult.getCurrentlySucceeding() || testCounter > 4)
			testCounter = 0;
		return testResult;
	}

}
