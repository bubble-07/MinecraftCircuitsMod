package com.circuits.circuitsmod.recorder;

import java.util.List;
import java.util.Optional;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.common.BlockFace;
import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.controlblock.ControlTileEntity;
import com.circuits.circuitsmod.controlblock.tester.net.SequenceReaderStateUpdate;
import com.circuits.circuitsmod.tester.CircuitSequenceReader;
import com.circuits.circuitsmod.tester.TestConfig;
import com.circuits.circuitsmod.tester.UnbreakiumCageManager;

public class CircuitRecorder extends CircuitSequenceReader<ControlTileEntity, RecordingState> {
	
	private CircuitRecording recording = null;
	private UnbreakiumCageManager<ControlTileEntity> cageManager;
	private String circuitName;

	public CircuitRecorder(String circuitName, ControlTileEntity parent, TestConfig config) {
		super(parent, config);
		this.circuitName = circuitName;
		this.cageManager = new UnbreakiumCageManager<>(this);
		init();
		cageManager.setupUnbreakiumCage();
	}
	
	public Optional<CircuitRecording> getRecording() {
		return Optional.ofNullable(recording);
	}
	
	@Override
	public void cleanup() {
		cageManager.tearDownUnbreakiumCage();
	}

	@Override
	public void successAction() {
		cageManager.tearDownUnbreakiumCage();
	}

	@Override
	public void stateUpdateAction() {
		parent.updateState(this.getState());
		if (!parent.getWorld().isRemote) {
			CircuitsMod.network.sendToAll(new SequenceReaderStateUpdate.Message(this.getState(), parent.getPos()));
		}		
	}

	@Override
	public void respondToOutput(List<BusData> output) {
		this.recording.recordResult(output);
	}

	@Override
	public void failureAction() {
		cageManager.tearDownUnbreakiumCage();		
	}
	

	@Override
	public Optional<AxisAlignedBB> getTestingBox() {
		return cageManager.getTestingBox();
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
	public int getNumTests() {
		return this.recording.getNumEntries();
	}

	@Override
	public List<BusData> getCurrentInputCase() {
		return this.recording.currentInputCase();
	}

	@Override
	protected boolean hasNotFailed(List<BusData> actualOutputs) {
		//Recorder can never fail w.r.t. outputs
		return true;
	}

	@Override
	public void populateInputOutputFaces() {
		for (int i = 0; i < 4; i++) {
			Optional<BlockFace> face = getInputFace(i, testbbox);
			if (!face.isPresent()) {
				break;
			}
			this.inputFaces.add(face.get());
		}
		for (int j = 0; j < 4; j++) {
			Optional<BlockFace> face = getOutputFace(j, testbbox);
			if (!face.isPresent()) {
				break;
			}
			this.outputFaces.add(face.get());
		}
		
		int[] inputWidths = new int[inputFaces.size()];
		int[] outputWidths = new int[outputFaces.size()];
		
		for (int i = 0; i < inputWidths.length; i++) {
			inputWidths[i] = getWidthOfOppSeg(inputFaces.get(i)).get();
		}
		for (int i = 0; i < outputWidths.length; i++) {
			outputWidths[i] = getWidthOfOppSeg(outputFaces.get(i)).get();
		}
		this.recording = CircuitRecording.initCircuitRecording(inputWidths, outputWidths).orElse(null);
		
		if (this.recording == null) {
			fail("Input space too large!");
		}
		
		if (this.inputFaces.size() + this.outputFaces.size() > 4) {
			fail("Too many I/O faces!");
		}	
		if (this.inputFaces.size() < 1) {
			fail("No input faces!");
		}
		if (this.outputFaces.size() < 1) {
			fail("No output faces!");
		}
	}

	@Override
	public void initTestState() {
	}

	@Override
	public RecordingState getState() {
		return new RecordingState(this.circuitName, this.testindex, this.getNumTests(), this.finished, this.success,
				                  this.config, this.currentInputCase, this.failureReason);
	}

}
