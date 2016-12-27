package com.circuits.circuitsmod.circuitblock;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import com.circuits.circuitsmod.busblock.BusBlock;
import com.circuits.circuitsmod.busblock.BusSegment;
import com.circuits.circuitsmod.busblock.StartupCommonBus;
import com.circuits.circuitsmod.circuit.CircuitConfigOptions;
import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.CircuitUID;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.circuitblock.WireDirectionMapper.WireDirectionGenerator;
import com.circuits.circuitsmod.common.BlockFace;
import com.circuits.circuitsmod.common.BusData;
import com.circuits.circuitsmod.common.RedstoneUtils;
import com.circuits.circuitsmod.reflective.ChipInvoker;
import com.circuits.circuitsmod.reflective.Invoker;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.BlockDirectional;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CircuitTileEntity extends TileEntity {
	
	public Collection<BusSegment> getBusSegments() {
		return connectedBuses.values();
	}

	public Optional<BusSegment> getBusSegment(EnumFacing face) {
		return connectedBuses.containsKey(face) ? Optional.of(connectedBuses.get(face)) : Optional.empty();
	}
	
	public void setBusSegment(EnumFacing face, BusSegment seg) {
		connectedBuses.put(face, seg);
	}
	
	public SpecializedCircuitUID getCircuitUID() {
		return this.circuitUID;
	}
	
	/**
	 * The specialized circuit UID is the __only__ thing saved with this tile entity
	 * other than the invocation state
	 * The facing direction of the circuit block is stored in its block metadata
	 */
	private SpecializedCircuitUID circuitUID = null;
	
	private final String name = "circuittileentity";
	
	/**
	 * Mapping from faces to bus segments on this circuit tile entity
	 */
	private Map<EnumFacing, BusSegment> connectedBuses = Maps.newHashMap();
	
	/**
	 * Stores the implementation invoker for this circuit
	 */
	private ChipInvoker impl = null;
	
	/**
	 * Stores the current state of the circuit (if sequential)
	 */
	private Invoker.State state = null;
	
	private WireDirectionMapper wireMapper = null;
	
	/**
	 * Buffer to store all redstone output signals delivered at this tick
	 */
	private int[] redstoneOutputs = new int[EnumFacing.values().length];
	
	/**
	 * List of current impending inputs to this CircuitTileEntity,
	 * as passed by bus networks
	 */
	private List<BusData> inputData = null;
	
	private NBTTagCompound loadingFromFile = null;
	
	public void receiveInput(EnumFacing face, BusData data) {
		Optional<Integer> inputIndex = wireMapper.getInputIndexOf(face);
		if (inputIndex.isPresent()) {
			inputData.set(inputIndex.get(), data);
		}
	}
	
	public boolean isAnalog(EnumFacing facing) {
		Optional<Integer> index = wireMapper.getInputIndexOf(facing);
		if (index.isPresent()) {
			return CircuitInfoProvider.getAnalogInputs(this.circuitUID)[index.get()];
		}
		index = wireMapper.getOutputIndexOf(facing);
		if (index.isPresent()) {
			return CircuitInfoProvider.getAnalogOutputs(this.circuitUID)[index.get()];
		}
		return false;
	}
	

	public void init(World worldIn, SpecializedCircuitUID circuitUID) {
		
		this.circuitUID = circuitUID;
		
		if (!worldIn.isRemote && impl == null) {
			CircuitInfoProvider.ensureServerModelInit();
			
			IBlockState state = worldIn.getBlockState(getPos());
			update(state);
		}
		else if (worldIn.isRemote && !this.isClientInit()) {
			IBlockState state = worldIn.getBlockState(getPos());
			update(state);
			
			this.tryInitClient();
		}
	}
	
	public EnumFacing getParentFacing() {
		IBlockState parentState = getWorld().getBlockState(getPos());
		return (EnumFacing)parentState.getValue(BlockDirectional.FACING);
	}
	
	int getSidePower(EnumFacing side) {
		return RedstoneUtils.getSidePower(getWorld(), getPos(), side);
	}
	
	boolean isSidePowered(EnumFacing side) {
		return RedstoneUtils.isSidePowered(getWorld(), getPos(), side);
	}
	
	private void notifyNeighbor(EnumFacing side) {
        BlockPos blockpos1 = pos.offset(side);
        if(net.minecraftforge.event.ForgeEventFactory.onNeighborNotify(getWorld(), pos, getWorld().getBlockState(pos), java.util.EnumSet.of(side)).isCanceled())
            return;
        getWorld().notifyBlockOfStateChange(blockpos1, StartupCommonCircuitBlock.circuitBlock);
        getWorld().notifyNeighborsOfStateExcept(blockpos1, StartupCommonCircuitBlock.circuitBlock, side.getOpposite());
	}
	
	/**
	 * Clears any impending inputs from this circuit tile entity.
	 */
	private void clearInputs() {
		this.inputData = Lists.newArrayList();
		for (int width : CircuitInfoProvider.getInputWidths(circuitUID)) {
			this.inputData.add(new BusData(width, 0));
		}
	}
	
	/**
	 * Clears all redstone outputs from this circuit tile entity
	 * @param state
	 */
	private void clearOutputs() {
		this.redstoneOutputs = new int[EnumFacing.values().length];
	}
	
	/**
	 * Initializes the bus segment map for this circuit tile entity to
	 * be a collection of unique bus segments, one for each valid input/output
	 * as defined in the reflectively-called implementation
	 */
	private void initBusSegments() {	
		for (int i = 0; i < CircuitInfoProvider.getNumInputs(circuitUID); i++) {
			EnumFacing inputFace = this.wireMapper.getInputFace(i);
			BusSegment inputSeg = new BusSegment(CircuitInfoProvider.getInputWidths(circuitUID)[i]);
			//Input to a circuit tile entity is output from a bus segment
			inputSeg.addOutput(new BlockFace(this.getPos(), inputFace));
			this.connectedBuses.put(inputFace, inputSeg);
		}
		
		for (int i = 0; i < CircuitInfoProvider.getNumOutputs(circuitUID); i++) {
			EnumFacing outputFace = this.wireMapper.getOutputFace(i);
			BusSegment outputSeg = new BusSegment(CircuitInfoProvider.getOutputWidths(circuitUID)[i]);
			//Output from a circuit tile entity is input to a bus segment
			outputSeg.addInput(new BlockFace(this.getPos(), outputFace));
			this.connectedBuses.put(outputFace, outputSeg);
		}
	}
	
	/**
	 * Unifies all existing bus segments on this circuit tile entity with bus segments
	 * of surrounding blocks according to incidence logic
	 */
	private void connectBuses() {
		//NOTE: For now, this is deliberately hilariously bad and inefficient, too
		//But for now, I don't care, because correctness matters more.
		
		Function<EnumFacing, Optional<BusSegment>> getSeg = (f) -> {
			return CircuitBlock.getBusSegmentAt(getWorld(), new BlockFace(getPos(), f).otherSide());
		};
		
		//We fundamentally need to handle two cases here:
		//direct connections to other circuit blocks, and connections to buses
		
		Consumer<EnumFacing> processDirect = (face) -> {
			Optional<BusSegment> seg = getSeg.apply(face);
			if (seg.isPresent()) {
				//Must be a direct connection
				this.getBusSegment(face).get().unifyWith(getWorld(), seg.get());
				this.getBusSegment(face).get().forceUpdate(getWorld());
			}
			else {
				//Might be a bus, in which case we'll treat all surrounding buses as if they were just placed.
				BlockPos pos = getPos().offset(face);
				IBlockState blockState = getWorld().getBlockState(pos);
				if (blockState.getBlock() instanceof BusBlock) {
					BusBlock.connectOnPlace(getWorld(), pos, StartupCommonBus.busBlock.getMetaFromState(blockState));
				}
			}
		};
		//Direct connections
		for (EnumFacing face : this.wireMapper.getInputfaces()) {
			processDirect.accept(face);
		}
		for (EnumFacing face : this.wireMapper.getOutputFaces()) {
			processDirect.accept(face);
		}
	}
	
	private void initWireDirAndBuses() {
		if (getParentFacing().equals(EnumFacing.DOWN)) {
			return;
		}
		WireDirectionGenerator dirGen = CircuitInfoProvider.getWireDirectionGenerator(circuitUID.getUID());
		this.wireMapper = dirGen.getMapper(getParentFacing(), CircuitInfoProvider.getNumInputs(circuitUID), 
				                                              CircuitInfoProvider.getNumOutputs(circuitUID));
		this.clearInputs();
		this.initBusSegments();
	}
	
	public boolean isClientInit() {
		return this.wireMapper != null;
	}
	public void tryInitClient() {
		if (CircuitInfoProvider.isClientModelInit() && CircuitInfoProvider.hasSpecializedInfoOn(circuitUID)) {
			initWireDirAndBuses();
		}
		else {
			CircuitInfoProvider.requestSpecializedClientInfoFor(circuitUID);
			CircuitInfoProvider.ensureClientModelInit();
		}
	}
	
	public void update(IBlockState state) {
		//This was a really weird bug -- apparently, blocks can decide not to initialize themselves with their default state
		//on load, so we test for a block having its default facing (DOWN) and break if it is
		if (this.getParentFacing().equals(EnumFacing.DOWN)) {
			getWorld().scheduleBlockUpdate(getPos(), StartupCommonCircuitBlock.circuitBlock, 2, 0);
			return;
		}
		
		
		if (impl == null) {
			//If we're on the client, don't care about updating, we're just here
			//to look pretty
			if (getWorld() != null && getWorld().isRemote && !isClientInit()) {
				tryInitClient();
			}
			else if (getWorld() != null && !getWorld().isRemote) {
				if (CircuitInfoProvider.isServerModelInit()) {
					if (CircuitInfoProvider.hasImplOn(circuitUID.getUID())) {
						this.impl = CircuitInfoProvider.getInvoker(circuitUID);
						this.state = this.impl.initState();

						initWireDirAndBuses();
						this.connectBuses();
						
						if (this.loadingFromFile != null) {
							this.readFromNBT(this.loadingFromFile);
							this.loadingFromFile = null;
						}
					}
				}
				else {
					CircuitInfoProvider.ensureServerModelInit();
				}
			}
		}
		//Do not change this to an "else" -- this ensures the circuit updates immediately after initialization!
		if (impl != null) {
			
			//By this point, we should already have received any incoming inputs from incident
			//bus segments, so we only need (for now) to deal explicitly with redstone inputs
			
			//Okay, so first, find all of the input faces with a declared
			//input width of 1 or are analog, and fill the bus data values
			//with actual redstone signals coming into this block
			for (int redstoneIndex : this.impl.getRedstoneInputs()) {
				EnumFacing redstoneFace = this.wireMapper.getInputFace(redstoneIndex);
				if (this.impl.analogInputs()[redstoneIndex]) {
					this.inputData.set(redstoneIndex, new BusData(4, getSidePower(redstoneFace)));
				}
				else {
					this.inputData.set(redstoneIndex, new BusData(1, isSidePowered(redstoneFace) ? 1 : 0));
				}
			}
			
			clearOutputs();
			
			//Okay, now that in theory, we have a complete input list, generate the output list
			//using the wrapped circuit implementation
			List<BusData> outputs = this.impl.invoke(this.state, this.inputData);
						
			//Okay, now we need to deliver any and all redstone output signals
			for (int redstoneIndex : this.impl.getRedstoneOutputs()) {
				EnumFacing face = this.wireMapper.getOutputFace(redstoneIndex);
				if (this.impl.analogOutputs()[redstoneIndex]) {
					this.redstoneOutputs[face.getIndex()] = (int) outputs.get(redstoneIndex).getData();
				}
				else {
					this.redstoneOutputs[face.getIndex()] = ((outputs.get(redstoneIndex).getData() & 1) > 0) ? 15 : 0;
				}
			}
			
			//Okay, great. We've set the redstone outputs to be delivered, so now
			//we need to propagate the remaining outputs to connected bus networks.
			for (int i = 0; i < outputs.size(); i++) {
				
				EnumFacing side = wireMapper.getOutputFace(i);
				
				Optional<BusSegment> busSeg = this.getBusSegment(side);
				BusData data = outputs.get(i);
				if (busSeg.isPresent()) {
					busSeg.get().accumulate(getWorld(), new BlockFace(getPos(), side), data);
				}
				
				//If instead, we sent a redstone signal, just make sure to notify the next block over to update
				if (data.getWidth() == 1) {
					notifyNeighbor(side);
				}
			}
			
			getWorld().scheduleBlockUpdate(getPos(), StartupCommonCircuitBlock.circuitBlock, 2, 0);
			getWorld().notifyBlockOfStateChange(getPos(), StartupCommonCircuitBlock.circuitBlock);
			
			getWorld().notifyNeighborsOfStateChange(getPos(), StartupCommonCircuitBlock.circuitBlock);
		}
	}
	@Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState)
    {
        return oldState.getBlock() != newState.getBlock();
    }
	
	private NBTTagCompound getCircuitStateCompound() {
		NBTTagCompound result = new NBTTagCompound();
		
		if (this.impl != null && this.impl.isSequential()) {
			Optional<byte[]> payload = this.impl.serializeState(this.state);
			if (payload.isPresent()) {
				result.setByteArray("InternalCircuitState", payload.get());
			}
		}
		result.setByteArray("CircuitInputState", BusData.listToBytes(this.inputData));
		return result;
	}
	
	private void setCircuitStateFromCompound(NBTTagCompound compound) {
		Optional<List<BusData>> inputDatas = BusData.listFromBytes(compound.getByteArray("CircuitInputState"));
		if (inputDatas.isPresent()) {
			this.inputData = inputDatas.get();
			if (this.inputData.size() == 0) {
				this.clearInputs();
			}
		}
		if (this.impl != null && this.impl.isSequential()) {
			byte[] serialized = compound.getByteArray("InternalCircuitState");
			Optional<Invoker.State> newState = this.impl.deserializeState(this.state, serialized);
			if (newState.isPresent()) {
				this.state = newState.get();
			}
			else {
				this.state = this.impl.initState();
			}
		}
	}
	
	private NBTTagCompound getUIDTagCompound() {
        NBTTagCompound TEData = new NBTTagCompound();
        TEData.setInteger("CircuitUID", this.circuitUID.getUID().toInteger());
        TEData.setIntArray("ConfigOptions", this.circuitUID.getOptions().asInts());
        return TEData;
	}
	private void setUIDFromCompound(NBTTagCompound compound) {
    	int uidNum = compound.getInteger("CircuitUID");
    	int[] optvals = compound.getIntArray("ConfigOptions");
    	CircuitUID uid = CircuitUID.fromInteger(uidNum);
    	CircuitConfigOptions opts = new CircuitConfigOptions(optvals);
    	this.circuitUID = new SpecializedCircuitUID(uid, opts);
	}
	
    public void readFromNBT(NBTTagCompound compound)
    {
    	super.readFromNBT(compound);
    	NBTTagCompound TEData = compound.getCompoundTag("CircuitTileEntity");
    	if (getWorld() == null) {
    		this.loadingFromFile = compound;
    	}
    	else if (!getWorld().isRemote) {
    		this.setCircuitStateFromCompound(compound.getCompoundTag("CircuitState"));
    	}
    	setUIDFromCompound(TEData);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound)
    {
        NBTTagCompound result = super.writeToNBT(compound);
        result.setTag("CircuitTileEntity", getUIDTagCompound());
        result.setTag("CircuitState", getCircuitStateCompound());
        return result;
    }
    
    @Override
    public NBTTagCompound getUpdateTag() {
    	return this.writeToNBT(new NBTTagCompound());
    }
    @Override
    public void handleUpdateTag(NBTTagCompound compound) {
    	readFromNBT(compound);
    }

	public int isProvidingWeakPower(IBlockState state, EnumFacing side) {
		if (impl != null) {
			return redstoneOutputs[side.getIndex()];
		}
		
		return 0;
	}
}
