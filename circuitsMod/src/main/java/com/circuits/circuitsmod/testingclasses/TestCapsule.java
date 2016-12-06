package com.circuits.circuitsmod.testingclasses;

import com.circuits.circuitsmod.busblock.BusSegment;
import com.circuits.circuitsmod.common.BlockFace;
import com.circuits.circuitsmod.testblock.TileEntityTesting;

public class TestCapsule {

	public BusSegment emitterSeg;
	public BlockFace inputFace;
	public BusSegment dummySeg;
	
	public TestCapsule() {
		emitterSeg = new BusSegment(0);
		inputFace = new BlockFace(null, null);
		dummySeg = new BusSegment(0);
	}
	
	public TestCapsule(TileEntityTesting testEntity) {
		emitterSeg = testEntity.getEmitterSegment();
		inputFace = testEntity.getInputFace();
		dummySeg = testEntity.getDummySeg();
	}
	
}
