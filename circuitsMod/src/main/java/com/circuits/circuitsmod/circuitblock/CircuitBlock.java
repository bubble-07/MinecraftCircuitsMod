package com.circuits.circuitsmod.circuitblock;

import java.util.Optional;
import java.util.Random;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.busblock.BusSegment;
import com.circuits.circuitsmod.busblock.IBusConnectable;
import com.circuits.circuitsmod.circuit.CircuitUID;
import com.circuits.circuitsmod.common.BlockFace;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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
	
	public static Optional<CircuitTileEntity> getCircuitTileEntityAt(World worldIn, BlockPos pos) {
		return OptionalUtils.tryCast(worldIn.getTileEntity(pos), CircuitTileEntity.class);
	}
	public static Optional<BusSegment> getBusSegmentAt(World worldIn, BlockFace face) {
		return getCircuitTileEntityAt(worldIn, face.getPos()).flatMap(te -> te.getBusSegment(face.getFacing()));
	}
	public static void setBusSegmentAt(World worldIn, BlockFace face, BusSegment segment) {
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
		//TODO: Maybe need to do stuff with bus segments here?
		return this.getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
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
	public TileEntity createNewTileEntity(World world, int ignored) {
		return new CircuitTileEntity();
	}

	/**
	 * The type of render function that is called for this block
	 */
	 public int getRenderType()
	{
		 return 3;
	}

	public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
	{
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
		return this.getDefaultState().withProperty(FACING, EnumFacing.getHorizontal(meta));
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	@Override
	public int getMetaFromState(IBlockState state)
	{
		return ((EnumFacing)state.getValue(FACING)).getHorizontalIndex();
	}
	
	public CircuitUID getUIDFromState(IBlockState state) {
		//TODO: Implement me!
		return null;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[]{FACING});
	}

}
