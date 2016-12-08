package com.circuits.circuitsmod.testblock;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.circuits.circuitsmod.busblock.BusSegment;
import com.circuits.circuitsmod.circuit.CircuitConfigOptions;
import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.CircuitUID;
import com.circuits.circuitsmod.circuit.PersistentCircuitUIDs;
import com.circuits.circuitsmod.circuit.SpecializedCircuitInfo;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.circuitblock.CircuitBlock;
import com.circuits.circuitsmod.circuitblock.CircuitTileEntity;
import com.circuits.circuitsmod.common.BlockFace;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.PosUtils;
import com.circuits.circuitsmod.reflective.TestGeneratorInvoker;
import com.circuits.circuitsmod.telecleaner.StartupCommonCleaner;
import com.circuits.circuitsmod.tester.ControlBlockTester;
import com.circuits.circuitsmod.tester.PuzzleBlockTester;
import com.circuits.circuitsmod.tester.TestConfig;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.*;
@SuppressWarnings("unused")
public class TileEntityTesting extends TileEntity implements ITickable {

	private int levelID;
	private final String name = "tileentitytesting";
	
	private static class CircuitTest {
		private SpecializedCircuitUID uid;
		private TestConfig testConfig;
		
		public CircuitTest(int circuitId, CircuitConfigOptions opts) {
			this(circuitId, opts, 20);
		}
		
		public CircuitTest(int circuitId, CircuitConfigOptions opts, int delay) {
			this(new SpecializedCircuitUID(CircuitUID.fromInteger(circuitId), opts), new TestConfig(delay));

		}

		public CircuitTest(int circuitId, int delay) {
			this(circuitId, new CircuitConfigOptions(), delay);
		}
		
		public CircuitTest(int circuitId) {
			this(circuitId, new CircuitConfigOptions());
		}
		
		public CircuitTest(SpecializedCircuitUID uid, TestConfig testConfig) {
			this.uid = uid;
			this.testConfig = testConfig;
		}

		public SpecializedCircuitUID getUID() {
			return this.uid;
		}
		public TestConfig getConfig() {
			return this.testConfig;
		}
	}
	
	private static HashMap<Integer, CircuitTest> testMap = new HashMap<Integer, CircuitTest>();
	
	static {
		testMap.put(0, new CircuitTest(PersistentCircuitUIDs.AND_CIRCUIT, new CircuitConfigOptions(1)));
		testMap.put(1, new CircuitTest(PersistentCircuitUIDs.INVERTER_CIRCUIT, new CircuitConfigOptions(2)));
		testMap.put(2, new CircuitTest(PersistentCircuitUIDs.OR_CIRCUIT, new CircuitConfigOptions(1)));
		testMap.put(3, new CircuitTest(PersistentCircuitUIDs.MUX_CIRCUIT, new CircuitConfigOptions(1)));
	}

	public int getLevelID() {
		return this.levelID;
	}
	
	private PuzzleBlockTester tester;
	
	public PuzzleBlockTester getTester() {
		return this.tester;
	}
	
	//Server-only
	public void startTest(World worldIn) {
		CircuitTest test = testMap.get(this.levelID);
		if (test == null) {
			Log.internalError("Puzzle tester not found for id: " + this.levelID);
		}
		SpecializedCircuitUID circuitUID = test.getUID();
		
		
		Optional<SpecializedCircuitInfo> circuit = CircuitInfoProvider.getSpecializedInfoFor(circuitUID);
		if (!circuit.isPresent()) {
			Log.internalError("Circuit entry not present! " + circuitUID);
			return;
		}
		this.tester = new PuzzleBlockTester(this, circuit.get(), test.getConfig());
	}

	public void init(World worldIn, int levelID) {
		if (!getWorld().isRemote) {
			this.levelID = levelID;
		}
	}

	public void update() {		
		if (tester != null) {
			tester.update();
		}
	}



	@Override
	public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
	{
		return oldState.getBlock() != newState.getBlock();
	}

	public void spawnTeleCleaner() {
		getWorld().setBlockState(getPos().offset(EnumFacing.UP), StartupCommonCleaner.teleCleaner.getDefaultState(), 2);
	}

	public EnumFacing getParentFacing() {
		IBlockState parentState = getWorld().getBlockState(getPos());
		return (EnumFacing)parentState.getValue(BlockDirectional.FACING);
	}


}
