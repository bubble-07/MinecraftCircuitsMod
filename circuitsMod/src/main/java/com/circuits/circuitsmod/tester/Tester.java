package com.circuits.circuitsmod.tester;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.circuits.circuitsmod.busblock.BusSegment;
import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.PersistentCircuitUIDs;
import com.circuits.circuitsmod.circuit.SpecializedCircuitInfo;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.circuitblock.CircuitBlock;
import com.circuits.circuitsmod.circuitblock.CircuitTileEntity;
import com.circuits.circuitsmod.common.BlockFace;
import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.common.PosUtils;
import com.circuits.circuitsmod.reflective.ChipInvoker;
import com.circuits.circuitsmod.reflective.Invoker;
import com.circuits.circuitsmod.reflective.SpecializedChipImpl;
import com.circuits.circuitsmod.reflective.TestGenerator;
import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class Tester<TEType extends TileEntity> {
	TEType parent;
	
	public SpecializedCircuitInfo testing = null;
	public SpecializedChipImpl internalImpls;
	Serializable internalTestState;
	
	EntityPlayer invokingPlayer;
	
	int testindex = 0;
	TestConfig config = null;
	
	List<BusData> currentInputCase;
	
	boolean finished = false;
	boolean success = false;
	/** 
	 * Time remaining before starting the next test
	 */
	int testWait = 0;
	/**
	 * Time remaining before checking for cheating again
	 */
	int cheatCheckWait = 0;
	
	SpecializedCircuitUID circuitUID;
	
	AxisAlignedBB testbbox = null;
	ArrayList<BlockFace> inputFaces = new ArrayList<>();
	ArrayList<BlockFace> outputFaces = new ArrayList<>();
	
	public Tester(EntityPlayer player, TEType parent, SpecializedCircuitInfo circuit, TestConfig config) {
		this.parent = parent;
		this.circuitUID = circuit.getUID();
		this.config = config;
		this.invokingPlayer = player;
		this.testing = circuit;
		
		//If we're able to get into a state with an invalid implementation here, we have bigger problems.
		this.internalImpls = CircuitInfoProvider.getSpecializedImpl(circuitUID).get();
		
		initTesting();
		setupNewTest();
		
	}
	
	
	public abstract void successAction();
	public abstract void stateUpdateAction();
	
	public SpecializedCircuitUID getUID() {
		return this.circuitUID;
	}
	
	public boolean testInProgress() {
		return !finished;
	}
	
	public AxisAlignedBB getBBox() {
		return testbbox;
	}
	
	public World getWorld() {
		return parent.getWorld();
	}
	
	public TestState getState() {
		return new TestState(circuitUID, testindex, this.internalImpls.getTestGenerator().totalTests(), 
				             this.finished, this.success, this.config, this.currentInputCase);
	}
	
	public EntityPlayer getInvokingPlayer() {
		return this.invokingPlayer;
	}
	
	public void addBusSegFaces() {
		for (BlockFace face : this.inputFaces) {
			CircuitBlock.getBusSegmentAt(getWorld(), face).ifPresent((s) -> s.addInput(face));
		}
		for (BlockFace face : this.outputFaces) {
			CircuitBlock.getBusSegmentAt(getWorld(), face).ifPresent((s) -> s.addOutput(face));
		}
	}
	
	public void removeBusSegFaces() {
		for (BlockFace face : this.inputFaces) {
			CircuitBlock.getBusSegmentAt(getWorld(), face).ifPresent((s) -> {
				s.removeInput(face);
				s.forceUpdate(getWorld());
			});
		}
		for (BlockFace face : this.outputFaces) {
			CircuitBlock.getBusSegmentAt(getWorld(), face).ifPresent((s) -> {
				s.removeOutput(face);
				s.forceUpdate(getWorld());
			});
		}
	}
	
	/**
	 * @return true if "cheating" was detected
	 */
	protected boolean checkForCheating() {
		return false;
	}
	
	protected int timeToNextCheatCheck() {
		return 1000;
	}
	
	
	
	public void update() {
		if (this.testbbox == null) {
			this.finished = true;
			this.success = false;
			failureAction();
			this.stateUpdateAction();
		}
		
		if (!this.finished) {
			if (cheatCheckWait == 0) {
				cheatCheckWait = timeToNextCheatCheck();
				if (checkForCheating()) {
					this.finished = true;
					this.success = false;
					removeBusSegFaces();
					failureAction();
					this.stateUpdateAction();
				}
			}
			else {
				cheatCheckWait--;
			}
		}
		
		if (!this.finished) {
			if (testWait == 0) {
				if (!getResultOfTest()) {
					//Test failed
					this.finished = true;
					this.success = false;
					removeBusSegFaces();
					failureAction();
				}
				else {
					testindex++;
					boolean moreTests = setupNewTest();
					if (!moreTests) {
						this.finished = true;
						this.success = true;
						successAction();
						removeBusSegFaces();
					}
					else {
						deliverTestInputs();
					}
				}
				this.stateUpdateAction();
			}
			else {
				testWait--;
			}
		}
	}
	
	public abstract void failureAction();
	
	/**
	 * 
	 * @return true if we were able to set up a new test, false if there are no more tests
	 */
	private boolean setupNewTest() {
		if (this.testbbox == null) {
			return true;
		}
		this.testWait = config.tickDelay;
		
		TestGenerator testGen = this.internalImpls.getTestGenerator();
		if (this.testindex >= testGen.totalTests()) {
			return false;
		}
		this.currentInputCase = testGen.generate(this.internalTestState);
		return true;
	}
	
	private void deliverTestInputs() {
		for (int i = 0; i < this.inputFaces.size(); i++) {
			long toPush = currentInputCase.get(i).getData();
			BlockFace face = inputFaces.get(i);
			Optional<BusSegment> segToPush = CircuitBlock.getBusSegmentAt(getWorld(), face);
			if (segToPush.isPresent()) {
				segToPush.get().accumulate(getWorld(), face, new BusData(64, toPush));
				segToPush.get().forceUpdate(getWorld());
			}
		}
	}
	
	private boolean getResultOfTest() {
		
		int[] outputWidths = this.internalImpls.getInvoker().outputWidths();
				
		List<BusData> actual = Lists.newArrayList();
		for (int i = 0; i < outputFaces.size(); i++) {
			BlockFace face = outputFaces.get(i);
			CircuitBlock.getCircuitTileEntityAt(getWorld(), face.getPos()).ifPresent((te) -> {
				te.update(getWorld().getBlockState(face.getPos()));
			});
			Optional<BusSegment> segToRead = CircuitBlock.getBusSegmentAt(getWorld(), face);
			if (segToRead.isPresent()) {
				segToRead.get().forceUpdate(getWorld());
				long reading = segToRead.get().getCurrentVal().getData();
				actual.add(new BusData(outputWidths[i], reading));
			}
		}
		
		return this.internalImpls.getTestGenerator().test(this.internalTestState, actual);
	}
	
	private static Stream<BlockPos> forPosIn(AxisAlignedBB box) {
		return IntStream.range((int)box.minX, (int)box.maxX)
			   .boxed().flatMap((x) -> IntStream.range((int)box.minY, (int)box.maxY)
			   .boxed().flatMap((y) -> IntStream.range((int)box.minZ, (int)box.maxZ)
					   .mapToObj((z) -> new BlockPos(x, y, z))));
	}
	
	private Predicate<BlockPos> isCircuitWithOption(int circuitID, int option) {
		return (p) -> {
			Optional<CircuitTileEntity> te = CircuitBlock.getCircuitTileEntityAt(getWorld(), p);
			if (!te.isPresent()) {
				return false;
			}
			SpecializedCircuitUID uid = te.get().getCircuitUID();
			if (uid.getUID().toInteger() != circuitID) {
				return false;
			}
			int[] opts = uid.getOptions().asInts();
			if (opts.length < 1) {
				return false;
			}
			return opts[0] == option;
		};
	}
	
	private Predicate<BlockFace> isBlockFaceWithBusWidth(int width) {
		return (face) -> {
			Optional<BusSegment> seg = CircuitBlock.getBusSegmentAt(getWorld(), face);
			if (!seg.isPresent()) {
				return false;
			}
			return seg.get().getWidth() == width;
		};
	}
	
	private Optional<BlockFace> getInputFace(int index, AxisAlignedBB searchBox) {
				return forPosIn(searchBox).filter(isCircuitWithOption(PersistentCircuitUIDs.INPUT_CIRCUIT, index))
						.flatMap(PosUtils::faces)
						.filter(isBlockFaceWithBusWidth(64)).findAny();
	}
	
	private Optional<BlockFace> getOutputFace(int index, AxisAlignedBB searchBox) {
		return forPosIn(searchBox).filter(isCircuitWithOption(PersistentCircuitUIDs.OUTPUT_CIRCUIT, index))
				.flatMap(PosUtils::faces)
				.filter(isBlockFaceWithBusWidth(64)).findAny();
	}
	
	
	public abstract Optional<AxisAlignedBB> getTestingBox();
	
	private void initTesting() {
		testindex = 0;
		
		this.testbbox = getTestingBox().orElse(null);
		
		if (this.testbbox == null) {
			return;
		}
		
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
		
		this.addBusSegFaces();
		
		this.internalTestState = this.internalImpls.getTestGenerator().initState();
	}
}
