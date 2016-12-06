package com.circuits.circuitsmod.testingclasses;
import com.circuits.circuitsmod.busblock.BusSegment;
import com.circuits.circuitsmod.circuit.CircuitConfigOptions;
import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.CircuitUID;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.common.BlockFace;
import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.reflective.SpecializedChipImpl;
import com.circuits.circuitsmod.testblock.TileEntityTesting;

import net.minecraft.world.World;

public class TestMultiplexer implements PuzzleTest {

	//right and the switch on the right side of the emitter, left side on the left.
	//The left output is fed through when the switch is on.  The right input is fed through when the switch
	//is NOT on.  
	
	BlockFace inputFace;
	BusSegment emitterSeg;
	TestCapsule capsule = new TestCapsule();
	int[] config = {1};
	CircuitConfigOptions options = new CircuitConfigOptions(config);
	SpecializedChipImpl impl = CircuitInfoProvider.getSpecializedImpl(new SpecializedCircuitUID(CircuitUID.fromInteger(14), options));
	

	@Override
	public TestTickResult test(World worldIn, TileEntityTesting testEntity) {
		System.out.println("Testing");
		TestTickResult testResult = new TestTickResult();
		
		
		
		
//		switch (testEntity.testCounter) {
//		case 1:
//			TestingUtilityMethods.checkIfRedstoneSucceeds(testEntity, testResult, false);
//			break;
//		case 2:
//			TestingUtilityMethods.checkIfRedstoneSucceeds(testEntity, testResult, false);
//			break;
//		case 3:
//			TestingUtilityMethods.checkIfRedstoneSucceeds(testEntity, testResult, false);
//			break;
//		case 4:
//			TestingUtilityMethods.checkIfRedstoneSucceeds(testEntity, testResult, true);
//			break;
//		}

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