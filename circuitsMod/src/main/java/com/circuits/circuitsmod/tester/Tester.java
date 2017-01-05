package com.circuits.circuitsmod.tester;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.SpecializedCircuitInfo;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.common.BlockFace;
import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.reflective.ChipInvoker;
import com.circuits.circuitsmod.reflective.SpecializedChipImpl;
import com.circuits.circuitsmod.reflective.TestGenerator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

/**
 * Parent class for testers of __particular circuits__, with cheat detection
 * @author bubble-07
 *
 * @param <TEType>
 */
public abstract class Tester<TEType extends TileEntity> extends CircuitSequenceReader<TEType, TestState> {
	
	public SpecializedCircuitInfo testing = null;
	public SpecializedChipImpl internalImpls;
	Serializable internalTestState;
	
	EntityPlayer invokingPlayer;
	
	SpecializedCircuitUID circuitUID;
	
	public Tester(EntityPlayer player, TEType parent, SpecializedCircuitInfo circuit, TestConfig config) {
		super(parent, config);
		this.circuitUID = circuit.getUID();
		this.invokingPlayer = player;
		this.testing = circuit;
		
		//If we're able to get into a state with an invalid implementation here, we have bigger problems.
		this.internalImpls = CircuitInfoProvider.getSpecializedImpl(circuitUID).get();
	}
	
	public SpecializedCircuitUID getUID() {
		return this.circuitUID;
	}
	
	@Override
	public TestState getState() {
		return new TestState(circuitUID, testindex, this.getNumTests(), 
				             this.finished, this.success, this.config, this.currentInputCase, this.failureReason);
	}
	
	public EntityPlayer getInvokingPlayer() {
		return this.invokingPlayer;
	}
	
	@Override
	public int getNumTests() {
		TestGenerator testGen = this.internalImpls.getTestGenerator();
		return testGen.totalTests();
	}
	@Override
	public List<BusData> getCurrentInputCase() {
		TestGenerator testGen = this.internalImpls.getTestGenerator();
		return testGen.generate(this.internalTestState);
	}
	
	@Override
	public void respondToOutput(List<BusData> output) {
		
	}
		
	@Override
	protected boolean hasNotFailed(List<BusData> actualOutputs) {
		return this.internalImpls.getTestGenerator().test(this.internalTestState, actualOutputs);
	}
	
	
	@Override
	public void populateInputOutputFaces() {
		ChipInvoker invoker = this.internalImpls.getInvoker();
		
		for (int i = 0; i < invoker.numInputs(); i++) {
			Optional<BlockFace> face = getInputFace(i, testbbox);
			if (face.isPresent()) {
				this.inputFaces.add(face.get());
			}
		}
		for (int i = 0; i < invoker.numOutputs(); i++) {
			Optional<BlockFace> face = getOutputFace(i, testbbox);
			if (face.isPresent()) {
				this.outputFaces.add(face.get());
			}
		}
	}
	@Override
	public void initTestState() {
		this.internalTestState = this.internalImpls.getTestGenerator().initState();
	}
}
