package com.circuits.circuitsmod.testingclasses;

import java.util.HashMap;

import com.circuits.circuitsmod.busblock.BusSegment;
import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.testblock.TileEntityTesting;

import net.minecraft.world.World;

public class TestingUtilityMethods {
	public static HashMap<Integer, Long> maskMap = new HashMap<Integer, Long>();
	static {
		addMasks();
	}
	
	public static void checkIfRedstoneSucceeds(TileEntityTesting testEntity, TestTickResult testResult, boolean expectedAnswer) {
		if (expectedAnswer) {
			if (TileEntityTesting.isSidePowered(testEntity, testEntity.getInputFace().getFacing())) {
				testResult.setCurrentlySucceeding(true);
			} else{
				testResult.setCurrentlySucceeding(false);
			}
		} else {
			if (!TileEntityTesting.isSidePowered(testEntity, testEntity.getInputFace().getFacing())) {
				testResult.setCurrentlySucceeding(true);
			} else {
				testResult.setCurrentlySucceeding(false);
			}
		}
	}
	
	public static void checkIfBusSucceeds(TileEntityTesting testEntity, TestTickResult testResult, BusData expectedAnswer) {
		BusSegment dummySeg = testEntity.getDummySeg();
		dummySeg.forceUpdate(testEntity.getWorld());
		long data = maskMap.get(dummySeg.getWidth()) & dummySeg.getCurrentVal().getData();
		BusData maskedData = new BusData(2, data);
		if (maskedData.equals(expectedAnswer)) {
			testResult.setCurrentlySucceeding(true);
		} else {
			testResult.setCurrentlySucceeding(false);
		}
	}
	
	private static void addMasks() {
		maskMap.put(1, (long) 0b0000000000000000000000000000000000000000000000000000000000000001);
		maskMap.put(2, (long) 0b0000000000000000000000000000000000000000000000000000000000000011);
		maskMap.put(4, (long) 0b0000000000000000000000000000000000000000000000000000000000001111);
		maskMap.put(8, (long) 0b0000000000000000000000000000000000000000000000000000000011111111);
		maskMap.put(16, (long) 0b0000000000000000000000000000000000000000000000001111111111111111);
		maskMap.put(32, (long) 0b0000000000000000000000000000000011111111111111111111111111111111);
		maskMap.put(64, (long) ~0);
	}
	
	public static TestCapsule createInputData(TileEntityTesting testEntity) {
		TestCapsule capsule = new TestCapsule(testEntity);
		return capsule;
	}
	
	public static void setAndOutputData(World worldIn, TestCapsule capsule, int index) {
		BusData testingData = new BusData(capsule.emitterSeg.getWidth(), index);
		capsule.emitterSeg.accumulate(worldIn, capsule.inputFace, testingData);
		capsule.emitterSeg.forceUpdate(worldIn);
	}
	
}
