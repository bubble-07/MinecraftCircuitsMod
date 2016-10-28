package com.circuits.circuitsmod.busblock;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.circuits.circuitsmod.circuitblock.CircuitBlock;
import com.circuits.circuitsmod.common.BlockFace;
import com.circuits.circuitsmod.common.IMetaBlockName;
import com.circuits.circuitsmod.common.PosUtils;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * Bus block implementation
 * 
 * This will house the implementation for the bus block, a block which allows carrying more than one
 * redstone signal over a single wire.
 * 
 * BusBlocks themselves are dumb, and don't really do much of anything but look pretty. Bus Networks will be where all the logic here will be.
 * 
 * BusBlocks have the following possible visual states:
 * -A bus traveling across a cardinal direction (North/South, East/West, Up/Down)
 * where ports are only visible at the ends. Happens whenever a bus is connected to two other buses on opposite sides.
 * -A bus "cap", which has visible ports on all sides. Happens in all other cases.
 * 
 *  The visual states, as above, need not be stored in metadata, as they will be computed based on what blocks are nearby
 * 
 * Correspondingly, the lower 2 bits of the Bus Block's 4-bit metadata will be used to store these four possible states.
 * The other two bits will be used to store the type of the bus -- this means that there are in fact __two__ different bus blocks
 */

public class BusBlock extends Block implements IBusConnectable, IMetaBlockName {
	public BusBlock()
	{
		super(Material.ROCK);
		this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);   // the block will appear on the Blocks tab in creative
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, BusFacing.CAP)
				.withProperty(WIDTH, WIDTH.getAllowedValues().iterator().next()));
	}

	public static BitWidth[] busWidths = {BitWidth.TWOBIT, BitWidth.FOURBIT, BitWidth.EIGHTBIT, BitWidth.SIXTEENBIT, BitWidth.THIRTYTWOBIT, BitWidth.SIXTYFOURBIT};

	/**
	 * The different facing directions (North/South, East/West, Up/Down, Cap) of a bus block
	 * If we ever get more refined block models, and are okay dealing with the explosion
	 * in the number of models, this should probably be changed to something along the lines
	 * of implementations of buildcraft pipes/other pipes. 
	 * @author bubble-07
	 *
	 */
	public enum BusFacing implements IStringSerializable {
		NORTHSOUTH("northsouth", 0),
		EASTWEST("eastwest", 1),
		UPDOWN("updown", 2),
		CAP("cap", 3);

		private final String name;
		private final int meta;

		BusFacing(String name, int meta) {
			this.name = name;
			this.meta = meta;
		}
		public String getName() {
			return this.name;
		}

		public int getMeta() {
			return this.meta;
		}

		public static BusFacing fromMeta(int value) {
			switch (value) {
			case 0:
				return BusFacing.NORTHSOUTH;
			case 1:
				return BusFacing.EASTWEST;
			case 2:
				return BusFacing.UPDOWN;
			case 3:
				return BusFacing.CAP;
			}
			return null;
		}
	}

	public static final PropertyEnum<BusFacing> FACING = PropertyEnum.create("facing", BusFacing.class, BusFacing.values());


	public enum BitWidth implements IStringSerializable {
		TWOBIT(0, 2, "twobit", "2-bit Bus"),
		FOURBIT(1, 4, "fourbit", "4-bit Bus"),
		EIGHTBIT(2, 8, "eightbit", "8-bit Bus"),
		SIXTEENBIT(3, 16, "sixteenbit", "16-bit Bus"),
		THIRTYTWOBIT(4, 32, "thirtytwobit", "32-bit Bus"),
		SIXTYFOURBIT(5, 64, "sixtyfourbit", "64-bit Bus");

		/**
		 * The metadata tag stored in the high bits of the bus' metadata for this given bit width.
		 */
		private final int meta_tag;
		/**
		 * The actual bit width of the bus
		 */
		private final int bit_width;

		private final String internalname;

		private final String externalname;

		BitWidth(int meta_tag, int bit_width, String internalname, String externalname) {
			this.meta_tag = meta_tag;
			this.bit_width = bit_width;
			this.internalname = internalname;
			this.externalname = externalname;
		}

		public int getTag() {
			return this.meta_tag;
		}
		public int getWidth() {
			return this.bit_width;
		}
		public String getName() {
			return internalname;
		}
		public String getDisplayName() {
			return externalname;
		}

	}  

	private static final String widthTag = "bitwidth";
	protected static IProperty<BitWidth> WIDTH = PropertyEnum.create(widthTag, BitWidth.class);

	@Override
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List list) {
		for (int i = 0; i < busWidths.length; i++) {
			list.add(new ItemStack(itemIn, 1, i));
		}
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[]{FACING, WIDTH});
	}

	/**
	 * Convert the given metadata into a BlockState for this Block
	 */
	public IBlockState getStateFromMeta(int meta)
	{
		return this.getDefaultState().withProperty(WIDTH, busWidths[meta]);
	}

	/**
	 * Convert the BlockState into the correct metadata value
	 */
	public int getMetaFromState(IBlockState state)
	{
		return ((BitWidth)state.getValue(WIDTH)).getTag();
	}

	@Override
	public int damageDropped(IBlockState state) {
		return getMetaFromState(state);
	}

	private boolean canConnect(IBlockAccess worldIn, BlockPos pos) {
		return (worldIn.getBlockState(pos).getBlock() instanceof IBusConnectable);
	}

	/**
	 * Here, we have to set the visual state of the block (the facing) based on its neighbors
	 */
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
	{
		boolean updown = canConnect(worldIn, pos.up()) || canConnect(worldIn, pos.down());
		boolean northsouth = canConnect(worldIn, pos.south()) || canConnect(worldIn, pos.north());
		boolean eastwest = canConnect(worldIn, pos.east()) || canConnect(worldIn, pos.west());

		BusFacing facing = BusFacing.CAP;
		if (updown && !northsouth && !eastwest) {
			facing = BusFacing.UPDOWN;
		}
		else if (!updown && northsouth && !eastwest) {
			facing = BusFacing.NORTHSOUTH;
		}
		else if (!updown && !northsouth && eastwest) {
			facing = BusFacing.EASTWEST;
		}
		return state.withProperty(FACING, facing);
	}
	
    /**
     * Upon placing a bus, we need to determine whether or not this bus bridges two bus segments,
     * and update existing bus segments accordingly
     * IBlockstate
     */
	@Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
		Predicate<BlockPos> connectable = (p) -> (p.equals(pos) || worldIn.getBlockState(p).getBlock() instanceof IBusConnectable);
		Predicate<BlockFace> circuit = (p) -> CircuitBlock.getBusSegmentAt(worldIn, p).isPresent();
		

		if (PosUtils.neighbors(pos).filter(connectable).count() > 1) {
			//We may be a connecting block here...
			Set<BlockFace> facesToUnify = IncrementalConnectedComponents.unifyOnAdd(pos, connectable, circuit);
			Set<BusSegment> toUnify = facesToUnify.stream()
					                              .map((p) -> CircuitBlock.getBusSegmentAt(worldIn, p).get())
					                              .collect(Collectors.toSet());
			
			
		}
		
        return this.getStateFromMeta(meta);
    }

	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.SOLID;
	}
	@Override
	public boolean isOpaqueCube(IBlockState iBlockState) {
		return true;
	}
	@Override
	public boolean isFullCube(IBlockState iBlockState) {
		return true;
	}

	@Override
	public String getSpecialName(ItemStack stack) {
		return this.busWidths[stack.getItemDamage()].getDisplayName();
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(Item.getItemFromBlock(this), 1, this.getMetaFromState(world.getBlockState(pos)));
	}
} 
