package com.circuits.circuitsmod.controlblock.tester;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.circuit.CircuitInfo;
import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.SpecializedCircuitInfo;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.common.BusData;
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
	ArrayList<BlockPos> inputBlocks = new ArrayList<>();
	ArrayList<BlockPos> outputBlocks = new ArrayList<>();
	ArrayList<IBlockState> initInputStates = new ArrayList<>();
	
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
	
	private void restoreInitState() {
		for (int i = 0; i < inputBlocks.size(); i++) {
			parent.getWorld().destroyBlock(inputBlocks.get(i), false);
			parent.getWorld().setBlockState(inputBlocks.get(i), initInputStates.get(i), 3);
		}
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
	
	public void update() {
		if (!this.finished) {
			if (testWait == 0) {
				testWait = config.tickDelay;
				if (!getResultOfTest()) {
					//Test failed
					this.finished = true;
					this.success = false;
					restoreInitState();
				}
				else {
					testindex++;
					boolean moreTests = setupNewTest();
					if (!moreTests) {
						this.finished = true;
						this.success = true;
						restoreInitState();
						RecipeDeterminer.determineRecipe(this);
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
		for (int i = 0; i < this.inputBlocks.size(); i++) {
			//TODO: support multi-bit inputs!
			//TODO: Also don't do this -- instead, set the redstone power level or something like that
			//the user should never be able to harvest redstone blocks during a test!
			if (currentInputCase.get(i).getData() > 0) {
				replaceWith(inputBlocks.get(i), Blocks.REDSTONE_BLOCK);
			}
			else {
				replaceWith(inputBlocks.get(i), Blocks.AIR);
			}
		}
	}
	
	private boolean getResultOfTest() {
		List<BusData> expected = internalImpls.getInvoker().invoke(this.internalCircuitState, this.currentInputCase);
		//TODO: Support multi-bit outputs!
		List<BusData> actual = Lists.newArrayList();
		for (int i = 0; i < outputBlocks.size(); i++) {
			boolean reading = parent.getWorld().isBlockPowered(outputBlocks.get(i)) || 
					(parent.getWorld().isBlockIndirectlyGettingPowered(outputBlocks.get(i)) > 0);
			if (reading) {
				actual.add(new BusData(1, reading ? 1 : 0));
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
	
	private static BlockPos findPosMatching(World world, AxisAlignedBB box, Predicate<IBlockState> statePred) {
		return forPosIn(box).filter((pos) -> statePred.test(world.getBlockState(pos)))
		                    .findFirst().orElse(null);
	}
	
	private BlockPos findItemBlockIn(ItemStack item, AxisAlignedBB box) {
		if (item == null) {
			return null;
		}
		
		Block itemBlock = Block.getBlockFromItem(item.getItem());
		
		return findPosMatching(parent.getWorld(), box, (state) -> {
			Block block = state.getBlock();
			if (itemBlock == block) {
				int itemMeta = item.getMetadata();
				int blockMeta = block.getMetaFromState(state);
				if (itemMeta == blockMeta) {
					return true;
				}
			}
			return false;
		});
	}
	
	private static String signTextToString(ITextComponent[] comps) {
		return Stream.of(comps).map(ITextComponent::getUnformattedText).reduce("", String::concat);
	}
	
	public boolean approxStringMatch(String one, String two) {
		//TODO: Implement fuzzy string matching
		return one.equalsIgnoreCase(two);
	}
	
	private Optional<BlockPos> findSignFor(boolean isInput, int index, AxisAlignedBB box) {
		final String text = (isInput ? "I" : "O") + index;
		
		return forPosIn(box).filter((pos) -> {
			TileEntity entity = parent.getWorld().getTileEntity(pos);
			if (entity instanceof TileEntitySign) {
				ITextComponent[] chatlines = ((TileEntitySign) entity).signText;
				return approxStringMatch(text, signTextToString(chatlines));
			}
			return false;
		}).findFirst();
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
			Optional<BlockPos> pos = findSignFor(true, i, testbbox);
			if (pos.isPresent()) {
				inputBlocks.add(pos.get());
				initInputStates.add(parent.getWorld().getBlockState(pos.get()));
			}
		}
		for (int i = 0; i < invoker.numOutputs(); i++) {
			Optional<BlockPos> pos = findSignFor(true, i, testbbox);
			if (pos.isPresent()) {
				outputBlocks.add(pos.get());
			}
		}
		
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
