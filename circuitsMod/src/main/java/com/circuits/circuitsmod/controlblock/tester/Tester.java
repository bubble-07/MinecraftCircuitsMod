package com.circuits.circuitsmod.controlblock.tester;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.busblock.BusSegment;
import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.PersistentCircuitUIDs;
import com.circuits.circuitsmod.circuit.SpecializedCircuitInfo;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.circuitblock.CircuitBlock;
import com.circuits.circuitsmod.circuitblock.CircuitTileEntity;
import com.circuits.circuitsmod.circuitblock.StartupCommonCircuitBlock;
import com.circuits.circuitsmod.common.BlockFace;
import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.common.PosUtils;
import com.circuits.circuitsmod.common.StreamUtils;
import com.circuits.circuitsmod.controlblock.ControlTileEntity;
import com.circuits.circuitsmod.controlblock.tester.net.TestStateUpdate;
import com.circuits.circuitsmod.frameblock.StartupCommonFrame;
import com.circuits.circuitsmod.recipes.RecipeDeterminer;
import com.circuits.circuitsmod.reflective.ChipInvoker;
import com.circuits.circuitsmod.reflective.Invoker;
import com.circuits.circuitsmod.reflective.SpecializedChipImpl;
import com.circuits.circuitsmod.reflective.TestGenerator;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class Tester {
	ControlTileEntity parent;
	
	public SpecializedCircuitInfo testing = null;
	public SpecializedChipImpl internalImpls;
	Serializable internalTestState;
	Invoker.State internalCircuitState;
	
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
	
	SpecializedCircuitUID circuitUID;
	
	AxisAlignedBB testbbox = null;
	ArrayList<BlockFace> inputFaces = new ArrayList<>();
	ArrayList<BlockFace> outputFaces = new ArrayList<>();
	
	public Tester(EntityPlayer player, ControlTileEntity parent, SpecializedCircuitInfo circuit, TestConfig config) {
		this.parent = parent;
		this.circuitUID = circuit.getUID();
		this.config = config;
		this.invokingPlayer = player;
		this.testing = circuit;
		
		this.internalImpls = CircuitInfoProvider.getSpecializedImpl(circuitUID);
		
		initTesting();
		setupNewTest();
		
	}
	
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
				             this.finished, this.success, this.config, this.internalTestState, this.currentInputCase);
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
	
	public void update() {
		if (!this.finished) {
			if (testWait == 0) {
				testWait = config.tickDelay;
				if (!getResultOfTest()) {
					//Test failed
					this.finished = true;
					this.success = false;
					removeBusSegFaces();
				}
				else {
					testindex++;
					boolean moreTests = setupNewTest();
					if (!moreTests) {
						this.finished = true;
						this.success = true;
						RecipeDeterminer.determineRecipe(this);
						removeBusSegFaces();
					}
					else {
						deliverTestInputs();
					}
				}
				parent.updateState(this.getState());
				if (!parent.getWorld().isRemote) {
					CircuitsMod.network.sendToAll(new TestStateUpdate.Message(this.getState(), parent.getPos()));
				}
				
			}
			else {
				testWait--;
			}
		}
	}
	
	/**
	 * 
	 * @return true if we were able to set up a new test, false if there are no more tests
	 */
	private boolean setupNewTest() {
		TestGenerator testGen = this.internalImpls.getTestGenerator();
		Optional<List<BusData>> testData = testGen.invoke(this.internalTestState);
		if (testData.isPresent()) {
			this.currentInputCase = testData.get();
			return true;
		}
		testWait = config.tickDelay;
		return false;
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
		List<BusData> expected = internalImpls.getInvoker().invoke(this.internalCircuitState, this.currentInputCase);
		
		List<BusData> actual = Lists.newArrayList();
		for (int i = 0; i < outputFaces.size(); i++) {
			BlockFace face = outputFaces.get(i);
			Optional<BusSegment> segToRead = CircuitBlock.getBusSegmentAt(getWorld(), face);
			if (segToRead.isPresent()) {
				long reading = segToRead.get().getCurrentVal().getData();
				actual.add(new BusData(expected.get(i).getWidth(), reading));
			}
		}
		return actual.equals(expected);
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
	
	private void initTesting() {
		//For now, must be placed in a bottom-most corner
		testindex = 0;
		
		//TODO: Also check for transparent blocks extending in a 1 block shell!
		
		Block frameBlock = StartupCommonFrame.frameBlock;
		
		//Get the vertical extent
		int vertExtent = 0;
		while (parent.getWorld().getBlockState(parent.getPos().up(vertExtent + 1)).getBlock()
				== frameBlock) {
			vertExtent++;
		}
		int pos_x_extent = 0;
		while (parent.getWorld().getBlockState(parent.getPos().add(pos_x_extent + 1, 0, 0)).getBlock()
				== frameBlock) {
			pos_x_extent++;
		}
		int neg_x_extent = 0;
		while (parent.getWorld().getBlockState(parent.getPos().add(-neg_x_extent - 1, 0, 0)).getBlock()
				== frameBlock) {
			neg_x_extent++;
		}
		int pos_z_extent = 0;
		while (parent.getWorld().getBlockState(parent.getPos().add(0, 0, pos_z_extent + 1)).getBlock()
				== frameBlock) {
			pos_z_extent++;
		}
		int neg_z_extent = 0;
		while (parent.getWorld().getBlockState(parent.getPos().add(0, 0, -neg_z_extent - 1)).getBlock()
				== frameBlock) {
			neg_z_extent++;
		}
		
		//TODO: Warn the user if no testing bounding box was found!
		
		testbbox = new AxisAlignedBB(parent.getPos().add(-neg_x_extent, 0, -neg_z_extent), 
								     parent.getPos().add(pos_x_extent, vertExtent, pos_z_extent));
		
		//TODO: Bring optional named inputs into circuit configs
		ChipInvoker invoker = this.internalImpls.getInvoker();
		
		for (int i = 0; i < invoker.numInputs(); i++) {
			//TODO: Should we warn if we can't find a formally-defined input?
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
		this.internalCircuitState = this.internalImpls.getInvoker().initState();
	}
	
	public IBlockState replaceWith(BlockPos pos, Block newBlock) {
		IBlockState backupstate = parent.getWorld().getBlockState(pos);
		
		parent.getWorld().destroyBlock(pos, false);
		parent.getWorld().setBlockState(pos, newBlock.getDefaultState(), 3);
		
		return backupstate;
	}
}
