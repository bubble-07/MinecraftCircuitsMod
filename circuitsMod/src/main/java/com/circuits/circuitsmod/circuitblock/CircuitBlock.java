package com.circuits.circuitsmod.circuitblock;

import java.util.Optional;
import java.util.Random;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.busblock.BusSegment;
import com.circuits.circuitsmod.busblock.IBusConnectable;
import com.circuits.circuitsmod.busblock.IncrementalConnectedComponents;
import com.circuits.circuitsmod.circuit.CircuitUID;
import com.circuits.circuitsmod.common.BlockFace;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.OptionalUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class CircuitBlock extends BlockDirectional implements ITileEntityProvider, IBusConnectable {

	private static final String name = "circuitBlock";
	//TODO: Give this thing side/bottom textures!

	public CircuitBlock()
	{
		super(Material.IRON);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));

		this.isBlockContainer = false;
		setUnlocalizedName(CircuitsMod.MODID + "_" + name);
		setCreativeTab(CreativeTabs.BUILDING_BLOCKS);
		setHardness(0.5F);
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
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}
	
	@Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack)
    {
		//Block placed
		CircuitTileEntity tileEntity = (CircuitTileEntity)worldIn.getTileEntity(pos);
		if (tileEntity != null) {
			Optional<CircuitUID> uid = CircuitItem.getUIDFromStack(stack);
			if (uid.isPresent()) {
				tileEntity.init(worldIn, uid.get());
			}
			else {
				Log.internalError("Circuit UID does not exist for item stack " + stack);
			}
		}
    }
	
	@Override
    public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return true;
	}

	@Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn) {
		worldIn.scheduleUpdate(pos, this, 1);
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {
		CircuitTileEntity tileEntity = (CircuitTileEntity) worldIn.getTileEntity(pos);
		if (tileEntity != null) {
			tileEntity.update(state);
		}
	}

	@Override
    public int getWeakPower(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		CircuitTileEntity tileEntity = (CircuitTileEntity) worldIn.getTileEntity(pos);
		if (tileEntity != null) {
			return tileEntity.isProvidingWeakPower(state, side);
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
	 public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		 Optional<CircuitTileEntity> te = getCircuitTileEntityAt(world, pos);
		 if (!te.isPresent()) {
			 return null;
		 }
		 return CircuitItem.getStackFromUID(te.get().getCircuitUID());
	 }

	public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
	{
		//Okay, so before we go about doing stuff, we need to mess with this thing's bus segment
		//to remove this CircuitBlock as one of the potential inputs/outputs
		Optional<CircuitTileEntity> te = CircuitBlock.getCircuitTileEntityAt(worldIn, pos);
		if (te.isPresent()) {
			for (BusSegment seg : te.get().getBusSegments()) {
				seg.removeAllAt(pos);
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
		if (result > 0) {
			return result;
		}
		return 4;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[]{FACING});
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return true;
	}
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
	}

}
