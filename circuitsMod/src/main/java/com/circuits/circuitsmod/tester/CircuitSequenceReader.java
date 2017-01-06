package com.circuits.circuitsmod.tester;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.circuits.circuitsmod.busblock.BusSegment;
import com.circuits.circuitsmod.circuit.PersistentCircuitUIDs;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.circuitblock.CircuitBlock;
import com.circuits.circuitsmod.circuitblock.CircuitTileEntity;
import com.circuits.circuitsmod.common.BlockFace;
import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.common.PosUtils;
import com.google.common.collect.Lists;

/**
 * Base class for something which reads a sequence of inputs/outputs from
 * input/output blocks within an AABB in a controlled manner
 * @author bubble-07
 *
 */
public abstract class CircuitSequenceReader<TEType extends TileEntity, StateType extends SequenceReaderState> {
	protected TEType parent;
	
	protected int testindex = 0;
	protected TestConfig config = null;
	
	protected List<BusData> currentInputCase;
	
	protected boolean finished = false;
	protected boolean success = false;
	protected String failureReason = null;
	
	
	/** 
	 * Time remaining before starting the next test
	 */
	protected int testWait = 0;
	/**
	 * Time remaining before checking for cheating again
	 */
	protected int cheatCheckWait = 0;
		
	protected AxisAlignedBB testbbox = null;
	protected ArrayList<BlockFace> inputFaces = new ArrayList<>();
	protected ArrayList<BlockFace> outputFaces = new ArrayList<>();
	
	public CircuitSequenceReader(TEType parent, TestConfig config) {
		this.parent = parent;
		this.config = config;
	}
	
	protected void init() {
		initTesting();
		setupNewTest();
	}
	
	public TEType getParent() {
		return parent;
	}
	
	/**
	 * Cleanup action to perform on cancellation
	 */
	public void cleanup() { }
	
	public abstract void successAction();
	
	/**
	 * Action to perform immediately after updating failure/success state
	 */
	public abstract void stateUpdateAction();
	
	public abstract StateType getState();
	
	
	public boolean testInProgress() {
		return !finished;
	}
	
	public AxisAlignedBB getBBox() {
		return testbbox;
	}
	
	public World getWorld() {
		return parent.getWorld();
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
	
	protected void fail(String failureReason) {
		this.finished = true;
		this.success = false;
		this.failureReason = failureReason;
		failureAction();
		this.stateUpdateAction();
	}
	
	public void update() {
		if (this.testbbox == null) {
			fail("Invalid Testing BBox");
		}
		
		cheatCheckWait--;
		testWait--;
		
		if (!this.finished) {
			if (cheatCheckWait <= 0) {
				cheatCheckWait = timeToNextCheatCheck();
				if (checkForCheating()) {
					removeBusSegFaces();
					fail("Cheating Detected");
				}
			}
		}
		
		if (!this.finished) {
			if (testWait <= 0) {
				List<BusData> result = getResultOfTest();
				respondToOutput(result);
				if (!hasNotFailed(result)) {
					removeBusSegFaces();
					fail(null);
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
					this.stateUpdateAction();
				}
			}
		}
	}

	/**
	 * Action performed in response to receiving a particular update
	 * before setting up a new test
	 */
	public abstract void respondToOutput(List<BusData> output);
	
	public abstract void failureAction();
	
	public abstract int getNumTests();
	
	public abstract List<BusData> getCurrentInputCase();
	
	protected abstract boolean hasNotFailed(List<BusData> actualOutputs);

	
	
	/**
	 * 
	 * @return true if we were able to set up a new test, false if there are no more tests
	 */
	private boolean setupNewTest() {
		if (this.testbbox == null || (this.success == false && this.finished == true)) {
			return true;
		}
		this.testWait = config.tickDelay;
		if (this.testindex >= this.getNumTests()) {
			return false;
		}
		this.currentInputCase = getCurrentInputCase();
		
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
				segToPush.get().accumulate(getWorld(), face, new BusData(64, toPush));
				forceTEUpdateAt(face);
				forceTEUpdateAt(face);
			}
		}
	}
	
	private void forceTEUpdateAt(BlockFace face) {
		CircuitBlock.getCircuitTileEntityAt(getWorld(), face.getPos()).ifPresent((te) -> {
			te.forceImmediateUpdate();
		});
	}
	
	protected Optional<Integer> getWidthOfOppSeg(BlockFace face) {
		Optional<BusSegment> oppSeg = CircuitBlock.getBusSegmentAt(getWorld(), new BlockFace(face.getPos(), face.getFacing().getOpposite()));
		return oppSeg.map(seg -> seg.getWidth());
	}
	
	protected List<BusData> getResultOfTest() {
						
		List<BusData> actual = Lists.newArrayList();
		for (int i = 0; i < outputFaces.size(); i++) {
			BlockFace face = outputFaces.get(i);
			forceTEUpdateAt(face);
			Optional<BusSegment> segToRead = CircuitBlock.getBusSegmentAt(getWorld(), face);
			Optional<Integer> oppSegWidth = getWidthOfOppSeg(face);
			if (segToRead.isPresent() && oppSegWidth.isPresent()) {
				long reading = segToRead.get().getCurrentVal().getData();
				actual.add(new BusData(oppSegWidth.get(), reading));
			}
		}
		return actual;
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
	
	protected Optional<BlockFace> getInputFace(int index, AxisAlignedBB searchBox) {
				return PosUtils.streamBlockPosIn(searchBox).filter(isCircuitWithOption(PersistentCircuitUIDs.INPUT_CIRCUIT, index))
						.flatMap(PosUtils::faces)
						.filter(isBlockFaceWithBusWidth(64)).findAny();
	}
	
	protected Optional<BlockFace> getOutputFace(int index, AxisAlignedBB searchBox) {
		return PosUtils.streamBlockPosIn(searchBox).filter(isCircuitWithOption(PersistentCircuitUIDs.OUTPUT_CIRCUIT, index))
				.flatMap(PosUtils::faces)
				.filter(isBlockFaceWithBusWidth(64)).findAny();
	}
	
	
	public abstract Optional<AxisAlignedBB> getTestingBox();
	
	public abstract void populateInputOutputFaces();
	public abstract void initTestState();

	
	private void initTesting() {
		testindex = 0;
		
		this.testbbox = getTestingBox().orElse(null);
		
		if (this.testbbox == null) {
			return;
		}
		populateInputOutputFaces();
		this.addBusSegFaces();
		initTestState();

	}
}
