package com.circuits.circuitsmod.controlblock.tester;

import java.util.ArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.circuits.circuitsmod.circuit.CircuitInfo;
import com.circuits.circuitsmod.circuit.SpecializedCircuitInfo;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.controlblock.frompoc.ControlTileEntity;
import com.circuits.circuitsmod.controlblock.frompoc.Microchips;
import com.circuits.circuitsmod.controlblock.tester.net.TestStateUpdate;
import com.circuits.circuitsmod.frameblock.StartupCommonFrame;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
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
	CircuitImpl impl = null;
	int testindex = 0;
	TestConfig config = null;
	
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
	
	public Tester(ControlTileEntity parent, SpecializedCircuitInfo circuit, TestConfig config) {
		this.parent = parent;
		this.circuitUID = circuit.getUID();
		this.config = config;
		
		this.testing = circuit;
		
		this.impl = Microchips.implMap.get(testing.getName());
		initTesting();
		setupNewTest();
		
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
		return new TestState(circuitUID, testindex, impl.test.numTests(), this.finished, this.success, this.config);
	}
	
	public void update() {
		if (!this.finished) {
			if (testWait == 0) {
				testWait = config.tickDelay;
				if (!finishNewTest()) {
					//Test failed
					this.finished = true;
					this.success = false;
					restoreInitState();
				}
				else {
					testindex++;
					if (testindex == impl.test.numTests()) {
						this.finished = true;
						this.success = true;
						restoreInitState();
						RecipeDeterminer.determineRecipe(this);
					}
					else {
						setupNewTest();
					}
				}
				parent.updateState(this.getState());
				if (!parent.getWorld().isRemote) {
					Microchips.network.sendToAll(new TestStateUpdate.Message(this.getState(), parent.getPos()));
				}
				
			}
			else {
				testWait--;
			}
		}
	}
	
	private void setupNewTest() {
		boolean[] testInputs = impl.test.testCase(testindex);
		
		for (int i = 0; i < inputBlocks.size(); i++) {
			if (testInputs[i]) {
				//TODO: __don't__ place redstone blocks, place a block just like it that, when broken, explodes.
				replaceWith(inputBlocks.get(i), Blocks.REDSTONE_BLOCK);
			}
			else {
				replaceWith(inputBlocks.get(i), Blocks.AIR);
			}
		}
		testWait = 100;
	}
	private boolean finishNewTest() {
		System.out.println("Test index" + testindex);
		boolean[] testInputs = impl.test.testCase(testindex);
		boolean[] expectedOutputs = impl.impl.compute(testInputs);
		for (int i = 0; i < outputBlocks.size(); i++) {
			boolean actual = parent.getWorld().isBlockPowered(outputBlocks.get(i)) || 
							(parent.getWorld().isBlockIndirectlyGettingPowered(outputBlocks.get(i)) > 0);
			if (actual != expectedOutputs[i]) {
				return false;
			}
		}
		return true;
		
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
	
	private BlockPos findSignTextIn(String text, AxisAlignedBB box) {
		return forPosIn(box).filter((pos) -> {
			TileEntity entity = parent.getWorld().getTileEntity(pos);
			if (entity instanceof TileEntitySign) {
				ITextComponent[] chatlines = ((TileEntitySign) entity).signText;
				return approxStringMatch(text, signTextToString(chatlines));
			}
			return false;
		}).findFirst().orElse(null);
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
		
		testbbox = new AxisAlignedBB(parent.getPos().add(-neg_x_extent, 0, -neg_z_extent), 
								     parent.getPos().add(pos_x_extent, vertExtent, pos_z_extent));
		System.out.println(testbbox);
		
		for (int i = 0; i < impl.impl.inputNames().length; i++) {
			BlockPos pos = findSignTextIn(impl.impl.inputNames()[i], testbbox);
			if (pos != null) {
				inputBlocks.add(pos);
				initInputStates.add(parent.getWorld().getBlockState(pos));
			}
		}
		
		for (int i = 0; i < impl.impl.outputNames().length; i++) {
			BlockPos pos = findSignTextIn(impl.impl.outputNames()[i], testbbox);
			if (pos != null) {
				outputBlocks.add(pos);
			}
		}
	}
	
	public IBlockState replaceWith(BlockPos pos, Block newBlock) {
		IBlockState backupstate = parent.getWorld().getBlockState(pos);
		
		parent.getWorld().destroyBlock(pos, false);
		parent.getWorld().setBlockState(pos, newBlock.getDefaultState(), 3);
		
		return backupstate;
	}
}
