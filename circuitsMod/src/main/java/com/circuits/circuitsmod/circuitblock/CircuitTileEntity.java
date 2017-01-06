package com.circuits.circuitsmod.circuitblock;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
	
	private int[] redstoneInputs = new int[EnumFacing.values().length];
	private int[] pendingRedstoneInputs = new int[EnumFacing.values().length];

		
	/**
	 * List of current impending inputs to this CircuitTileEntity,
	 * as passed by bus networks. This is allowed to be in a dirty state,
	 * but is only copied over to the inputData field once we
	 * have been notified (from input bus segments) that all inputs have been finalized
	 * and that we've already processed the input for the current tick
	 */
	private List<BusData> pendingInputData = null;
	
	/**
	 * List of inputs to this CircuitTileEntity from the previous redstone tick.
	 * These are used to compute the actual outputs of a circuit at a given tick
	 */
	private List<BusData> inputData = null;
	
	private int pendingInputs = 0;
	
	/**
	 * Something that flips between true/false on each update so we don't update twice on the same tick!
	 */
	private boolean updateStamp = false;
	
	private long worldTick = -1;
	
	
	private NBTTagCompound loadingFromFile = null;
	
	public void receiveInput(EnumFacing face, BusData data) {
		if (wireMapper == null || this.impl == null) {
			return;
		}
		Optional<Integer> inputIndex = wireMapper.getInputIndexOf(face);
		if (inputIndex.isPresent()) {
			pendingInputData.set(inputIndex.get(), data);
		}
		pendingInputs--;
		if (pendingInputs <= 0) {
			if (!this.getWorld().isRemote) {
				CircuitInfoProvider.createSpecializedInfoFor(circuitUID);
			}
			this.pendingInputs = CircuitInfoProvider.getNumInputs(circuitUID);
			finalizeInputs();
		}
	}
	
	public void finalizeInputs() {
		if (this.hasUpdatedThisTick()) {
			this.inputData = this.pendingInputData.stream().map((data) -> data.copy()).collect(Collectors.toList());
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
		return this.redstoneInputs[side.getIndex()];
	}
	
	boolean isSidePowered(EnumFacing side) {
		return getSidePower(side) > 0;
	}
	
	/**
	 * Clears any impending inputs from this circuit tile entity,
	 * and the old input states as well.
	 */
	private void clearInputs() {
		this.pendingInputData = Lists.newArrayList();
		this.inputData = Lists.newArrayList();
		for (int width : CircuitInfoProvider.getInputWidths(circuitUID)) {
			this.pendingInputData.add(new BusData(width, 0));
			this.inputData.add(new BusData(width, 0));
		}
		this.pendingInputs = CircuitInfoProvider.getNumInputs(circuitUID);
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
		return this.wireMapper != null || !CircuitInfoProvider.hasSpecializedInfoOn(circuitUID);
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
	
	public boolean hasUpdatedThisTick() {
		return this.updateStamp == ((getWorld().getTotalWorldTime() % 4) == 2);
	}
	
	public void forceImmediateUpdate() {
		this.inputData = this.pendingInputData.stream().map((data) -> data.copy()).collect(Collectors.toList());
		update(getWorld().getBlockState(getPos()));
	}
	
	public void updateRedstoneInputs() {
		if (this.getWorld().getTotalWorldTime() != this.worldTick) {
			for (EnumFacing facing : EnumFacing.values()) {
				this.pendingRedstoneInputs[facing.getIndex()] = RedstoneUtils.getSidePower(getWorld(), getPos(), facing);
			}
		}
		else {
			for (EnumFacing facing : EnumFacing.values()) {
				this.redstoneInputs[facing.getIndex()] = RedstoneUtils.getSidePower(getWorld(), getPos(), facing);
			}
		}
	}
	
	public void update(IBlockState state) {
		
		this.worldTick = getWorld().getTotalWorldTime();
		
		this.updateStamp = (getWorld().getTotalWorldTime() % 4) == 2;
		
		if (circuitUID == null) {
			return; //Something's __really__ messed up about this block. Leave it there, but let the user remove it.
		}
		
		if (impl == null || !CircuitInfoProvider.hasSpecializedInfoOn(circuitUID)) {
			//If we're on the client, don't care about updating, we're just here
			//to look pretty
			if (getWorld() != null && getWorld().isRemote && !isClientInit()) {
				tryInitClient();
			}
			else if (getWorld() != null && !getWorld().isRemote) {
				if (CircuitInfoProvider.isServerModelInit()) {
					if (CircuitInfoProvider.hasImplOn(circuitUID.getUID())) {
						
						CircuitInfoProvider.createSpecializedInfoFor(circuitUID);
						
						Optional<ChipInvoker> optInvoker = CircuitInfoProvider.getInvoker(circuitUID);
						if (!optInvoker.isPresent()) {
							//Must be trying to instantiate a circuit with an invalid configuration or missing implementation.
							//Keep the tile entity around (in case the server admin dun goofed), since a warning log message
							//has already been printed, but do not attempt to initialize the tile entity.
							return;
						}
						this.impl = optInvoker.get();
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
		if (impl != null && CircuitInfoProvider.hasSpecializedInfoOn(circuitUID)) {
			
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
			
			for (int i = 0; i < this.redstoneInputs.length; i++) {
				redstoneInputs[i] = pendingRedstoneInputs[i];
			}
			this.updateRedstoneInputs();
						
			clearOutputs();
			
			//Okay, now that in theory, we have a complete input list, generate the output list
			//using the wrapped circuit implementation
			List<BusData> outputs = this.impl.invoke(this.state, this.inputData);
			
			this.inputData = this.pendingInputData.stream().map((data) -> data.copy()).collect(Collectors.toList());
									
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
				
				BusData data = outputs.get(i);
				
				//If we get a redstone signal, tell the next block over to update, and skip this
				if (data.getWidth() == 1 || this.isAnalog(side)) {
					getWorld().notifyNeighborsOfStateChange(getPos(), StartupCommonCircuitBlock.circuitBlock);
					continue;
				}
				
				Optional<BusSegment> busSeg = this.getBusSegment(side);
				if (busSeg.isPresent()) {
					busSeg.get().accumulate(getWorld(), new BlockFace(getPos(), side), data);
				}

			}
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
		result.setByteArray("PendingCircuitInputState", BusData.listToBytes(this.pendingInputData));
		result.setByteArray("CircuitInputState", BusData.listToBytes(this.inputData));
		result.setBoolean("CircuitUpdateStamp", this.updateStamp);
		result.setLong("CircuitWorldTick", this.worldTick);
		
		result.setIntArray("CircuitRedstoneOutputs", redstoneOutputs);
		result.setIntArray("CircuitRedstoneInputs", redstoneInputs);
		result.setIntArray("CircuitPendingRedstoneInputs", pendingRedstoneInputs);
		
		return result;
	}
	
	private void setCircuitStateFromCompound(NBTTagCompound compound) {
		this.updateStamp = compound.getBoolean("CircuitUpdateStamp");
		this.worldTick = compound.getLong("CircuitWorldTick");
		
		int[] redstoneOutputs = compound.getIntArray("CircuitRedstoneOutputs");
		if (redstoneOutputs.length != 0) {
			this.redstoneOutputs = redstoneOutputs;
		}
		int[] redstoneInputs = compound.getIntArray("CircuitRedstoneInputs");
		if (redstoneInputs.length != 0) {
			this.redstoneInputs = redstoneInputs;
		}
		int[] pendingRedstoneInputs = compound.getIntArray("CircuitPendingRedstoneInputs");
		if (pendingRedstoneInputs.length != 0) {
			this.pendingRedstoneInputs = pendingRedstoneInputs;
		}
		
		Optional<List<BusData>> pendingInputDatas = BusData.listFromBytes(compound.getByteArray("PendingCircuitInputState"));
		if (pendingInputDatas.isPresent()) {
			this.pendingInputData = pendingInputDatas.get();
		}
		Optional<List<BusData>> inputDatas = BusData.listFromBytes(compound.getByteArray("CircuitInputState"));
		if (inputDatas.isPresent()) {
			this.inputData = inputDatas.get();
		}
		if (this.pendingInputData.size() == 0 || this.inputData.size() == 0) {
			this.clearInputs();
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
        if (this.circuitUID != null) {
        	TEData.setInteger("CircuitUID", this.circuitUID.getUID().toInteger());
            TEData.setIntArray("ConfigOptions", this.circuitUID.getOptions().asInts());
        }
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

	public int getWeakPower(IBlockState state, EnumFacing side) {
		if (impl != null) {
			return redstoneOutputs[side.getIndex()];
		}
		
		return 0;
	}
}
