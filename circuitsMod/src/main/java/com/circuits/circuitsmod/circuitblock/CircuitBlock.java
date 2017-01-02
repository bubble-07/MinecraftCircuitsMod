package com.circuits.circuitsmod.circuitblock;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.busblock.BusSegment;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.common.BlockFace;
import com.circuits.circuitsmod.common.Log;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class CircuitBlock extends BlockDirectional implements ITileEntityProvider {

	private static final String name = "circuitBlock";

	public CircuitBlock()
	{
		super(Material.CIRCUITS);

		this.isBlockContainer = false;
		setUnlocalizedName(CircuitsMod.MODID + "_" + name);
		setCreativeTab(null);
		setHardness(0.0F);
		this.setSoundType(SoundType.METAL);
	}
	
	public static Optional<CircuitTileEntity> getCircuitTileEntityAt(IBlockAccess worldIn, BlockPos pos) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te == null) {
			return Optional.empty();
		}
		try {
			return Optional.of((CircuitTileEntity) te);
		}
		catch (ClassCastException e) {
			return Optional.empty();
		}
	}
	public static Optional<BusSegment> getBusSegmentAt(IBlockAccess worldIn, BlockFace face) {
		return getCircuitTileEntityAt(worldIn, face.getPos()).flatMap(te -> te.getBusSegment(face.getFacing()));
	}
	public static void setBusSegmentAt(IBlockAccess worldIn, BlockFace face, BusSegment segment) {
		Optional<CircuitTileEntity> circuitTE = getCircuitTileEntityAt(worldIn, face.getPos());
		if (circuitTE.isPresent()) {
			circuitTE.get().setBusSegment(face.getFacing(), segment);
		}
	}

	public static String getName() {
		return name;
	}

	@Override
	public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
	{
		//Whenever a circuit tile entity comes online (operational, with a loaded implementation),
		//then we will perform the appropriate logic for initializing bus segments on all valid input/output faces.
		worldIn.scheduleUpdate(pos, this, 1);
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing());
	}
	
	@Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
		//Block placed
		CircuitTileEntity tileEntity = (CircuitTileEntity)worldIn.getTileEntity(pos);
		if (tileEntity != null) {
			Optional<SpecializedCircuitUID> uid = CircuitItem.getUIDFromStack(stack);
			if (uid.isPresent()) {
				tileEntity.init(worldIn, uid.get());
				worldIn.scheduleUpdate(pos, this, 1);
			}
			else {
				Log.internalError("Circuit UID does not exist for item stack " + stack);
			}
		}
    }
	
	@Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		if (side == null) {
			return false;
		}
		
		Optional<CircuitTileEntity> te = CircuitBlock.getCircuitTileEntityAt(world, pos);
		if (te.isPresent()) {
			return te.get().getBusSegment(side.getOpposite()).map((seg) -> seg.getWidth() == 1).orElse(false);
		}
		return true;
	}
	
	private void updateTEIfNecessary(CircuitTileEntity TE, IBlockState state) {
		if (TE.getWorld().getTotalWorldTime() % 2 != 0) {
			TE.getWorld().scheduleUpdate(TE.getPos(), StartupCommonCircuitBlock.circuitBlock, 1);
			return;
		}
		TE.getWorld().scheduleUpdate(TE.getPos(), StartupCommonCircuitBlock.circuitBlock, 2);
		if (!TE.hasUpdatedThisTick()) {
			TE.update(state);
			TE.getWorld().notifyNeighborsOfStateChange(TE.getPos(), StartupCommonCircuitBlock.circuitBlock);
		}
	}
	
	public void update(World worldIn, BlockPos pos, IBlockState state) {
		CircuitTileEntity tileEntity = (CircuitTileEntity) worldIn.getTileEntity(pos);
		if (tileEntity != null) {
			updateTEIfNecessary(tileEntity, state);
		}
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		update(worldIn, pos, state);
	}

	@Override
    public int getWeakPower(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		CircuitTileEntity tileEntity = (CircuitTileEntity) worldIn.getTileEntity(pos);
		if (tileEntity != null) {
			//TODO: I really don't get why redstone inputs in particular are backwards. 
			return tileEntity.getWeakPower(state, side.getOpposite());
		}
		return 0;
	}
	
    @Override
    public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side)
    {
        return getWeakPower(blockState, blockAccess, pos, side);
    }
    
    @Override
    public boolean canProvidePower(IBlockState state) {
    	return true;
    }

	@Override
	public TileEntity createNewTileEntity(World world, int ignored) {
		return new CircuitTileEntity();
	}
	
	@Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune)
    {
		 Optional<CircuitTileEntity> te = getCircuitTileEntityAt(world, pos);
		 if (!te.isPresent()) {
			 return null;
		 }
		 return Lists.newArrayList(CircuitItem.getStackFromUID(te.get().getCircuitUID()));
    }
	
    @Override
    public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
        if (willHarvest) return true; //If it will harvest, delay deletion of the block until after getDrops
        return super.removedByPlayer(state, world, pos, player, willHarvest);
    }
    @Override
    public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity te, ItemStack tool)
    {
        super.harvestBlock(world, player, pos, state, te, tool);
        world.setBlockToAir(pos);
    }

	public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
	{
		//Okay, so before we go about doing stuff, we need to mess with this thing's bus segment
		//to remove this CircuitBlock as one of the potential inputs/outputs
		Optional<CircuitTileEntity> te = CircuitBlock.getCircuitTileEntityAt(worldIn, pos);
		if (te.isPresent()) {
			for (BusSegment seg : te.get().getBusSegments()) {
				seg.removeAllAt(pos);
				seg.forceUpdate(worldIn);
			}
		}
		
		super.breakBlock(worldIn, pos, state);
		worldIn.removeTileEntity(pos);
	}
	

	/**
	 * Called on both Client and Server when World#addBlockEvent is called
	 */
    public boolean eventReceived(IBlockState state, World worldIn, BlockPos pos, int eventID, int eventParam)
    {
		super.eventReceived(state, worldIn, pos, eventID, eventParam);
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity == null ? false : tileentity.receiveClientEvent(eventID, eventParam);
    }

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	@Override
	public IBlockState getStateFromMeta(int meta)
	{
		if (meta < 4 && meta >= 0) {
			return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
		}
		return this.getDefaultState();
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state)
	{
		int result = ((EnumFacing)state.getValue(FACING)).getHorizontalIndex();
		if (result >= 0) {
			return result;
		}
		return 3;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[]{FACING});
	}
    
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
        return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
    }

    /**
     * Checks if an IBlockState represents a block that is opaque and a full cube.
     */
    public boolean isFullyOpaque(IBlockState state)
    {
        return false;
    }
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

}
