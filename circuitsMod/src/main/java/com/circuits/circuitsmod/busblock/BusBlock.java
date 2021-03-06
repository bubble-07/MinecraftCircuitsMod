package com.circuits.circuitsmod.busblock;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.circuits.circuitsmod.circuitblock.CircuitBlock;
import com.circuits.circuitsmod.circuitblock.CircuitTileEntity;
import com.circuits.circuitsmod.common.BlockFace;
import com.circuits.circuitsmod.common.IMetaBlockName;
import com.circuits.circuitsmod.common.Log;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
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

public class BusBlock extends Block implements IMetaBlockName {
	public BusBlock()
	{
		super(Material.ROCK);
		setHardness(0.0F);
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
		NORTHSOUTH("northsouth", 0, false),
		EASTWEST("eastwest", 1, false),
		UPDOWN("updown", 2, true),
		CAP("cap", 3, true),
		INTERSECTION("intersection", 4, false),
		TOPCAP("topcap", 4, false);

		private final String name;
		private final int meta;
		private final boolean full;

		BusFacing(String name, int meta, boolean full) {
			this.name = name;
			this.meta = meta;
			this.full = full;
		}
		public String getName() {
			return this.name;
		}

		public int getMeta() {
			return this.meta;
		}
		public boolean isFullBlock() {
			return this.full;
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
			case 4:
				return BusFacing.INTERSECTION;
			case 5:
				return BusFacing.TOPCAP;
			}
			return null;
		}
	}

	public static final PropertyEnum<BusFacing> FACING = PropertyEnum.create("facing", BusFacing.class, BusFacing.values());


	public enum BitWidth implements IStringSerializable {
		TWOBIT(0, 2, "twobit"),
		FOURBIT(1, 4, "fourbit"),
		EIGHTBIT(2, 8, "eightbit"),
		SIXTEENBIT(3, 16, "sixteenbit"),
		THIRTYTWOBIT(4, 32, "thirtytwobit"),
		SIXTYFOURBIT(5, 64, "sixtyfourbit");

		/**
		 * The metadata tag stored in the high bits of the bus' metadata for this given bit width.
		 */
		private final int meta_tag;
		/**
		 * The actual bit width of the bus
		 */
		private final int bit_width;

		private final String internalname;

		BitWidth(int meta_tag, int bit_width, String internalname) {
			this.meta_tag = meta_tag;
			this.bit_width = bit_width;
			this.internalname = internalname;
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
	}  

	private static final String widthTag = "bitwidth";
	protected static IProperty<BitWidth> WIDTH = PropertyEnum.create(widthTag, BitWidth.class);

	@Override
	public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
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

	private boolean canConnect(IBlockAccess worldIn, IBlockState state, BlockFace face) {
		int meta = this.getMetaFromState(state);
		BlockPos otherPos = face.adjacent();
		IBlockState neighborState = worldIn.getBlockState(otherPos);
		if (neighborState.getBlock() instanceof BusBlock) {
			return this.getMetaFromState(neighborState) == meta;
		}
		if (neighborState.getBlock() instanceof CircuitBlock) {
			Optional<CircuitTileEntity> te = CircuitBlock.getCircuitTileEntityAt(worldIn, face.adjacent());
			if (te.isPresent() && !te.get().isClientInit()) {
				te.get().tryInitClient();
			}
			Optional<BusSegment> seg = CircuitBlock.getBusSegmentAt(worldIn, face.otherSide());
			return seg.isPresent() && seg.get().getWidth() == busWidths[meta].getWidth();
		}
		return false;
	}

	/**
	 * Here, we have to set the visual state of the block (the facing) based on its neighbors
	 */
	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos)
	{
		BiPredicate<BlockFace, BlockFace> connectable = (f1, f2) -> canConnect(worldIn, state, f1) || canConnect(worldIn, state, f2);

		
		boolean up = canConnect(worldIn, state, BlockFace.up(pos));
		boolean down = canConnect(worldIn, state, BlockFace.down(pos));
		boolean updown = up || down;
		boolean northsouth = connectable.test(BlockFace.north(pos), BlockFace.south(pos));
		boolean eastwest = connectable.test(BlockFace.east(pos), BlockFace.west(pos));

		BusFacing facing = BusFacing.CAP;
		if (!up && down && (northsouth || eastwest)) {
			facing = BusFacing.TOPCAP;
		}
		else if (updown && !northsouth && !eastwest) {
			facing = BusFacing.UPDOWN;
		}
		else if (!updown && northsouth && !eastwest) {
			facing = BusFacing.NORTHSOUTH;
		}
		else if (!updown && !northsouth && eastwest) {
			facing = BusFacing.EASTWEST;
		}
		else if (!updown && eastwest && northsouth) {
			facing = BusFacing.INTERSECTION;
		}
		return state.withProperty(FACING, facing);
	}
	
	private static Predicate<BlockPos> connectablePredicate(World worldIn, BlockPos pos, int meta) {
		return (p) -> {
			IBlockState bState = worldIn.getBlockState(p);
			if (bState.getBlock() instanceof BusBlock) {
				int otherMeta = StartupCommonBus.busBlock.getMetaFromState(bState);
				if (meta == otherMeta) {
					return true;
				}
			}
			//Only other "safe" case is if it's the block we're currently in the process of placing.
			//We stop whenever we hit a circuit tile entity and then rely on the success condition testing,
			//and so we don't include circuit tile entities at all
			return p.equals(pos);
		};
	}
	
	private static Predicate<BlockFace> circuitPredicate(World worldIn, BlockPos pos, int meta) {
		//Use the metadata within this bus block to determine what the width of any and all connecting
		//circuit inputs should be
		int connectingWidth = busWidths[meta].getWidth();
		return (p) -> {
			//We only stop if we find a circuit with a present bus segment whose defined width matches the width of this bus
			Optional<BusSegment> seg = CircuitBlock.getBusSegmentAt(worldIn, p);
			if (seg.isPresent()) {
				return seg.get().getWidth() == connectingWidth;
			}
			return false;
		};
	}
	
	public static void connectOnPlace(World worldIn, BlockPos pos, int meta) {
		
		//Note: The "connectable" predicate should depend on the width of the bus, as should the "success" predicate

		Predicate<BlockPos> connectable = connectablePredicate(worldIn, pos, meta);

		//Note: we need to make the incremental components thing able to go into places with unsafe block positions
		//if the success condition holds.
		Predicate<BlockFace> circuit = circuitPredicate(worldIn, pos, meta);

		//We may be a connecting block here...
		Set<BlockFace> facesToUnify = IncrementalConnectedComponents.unifyOnAdd(pos, connectable, circuit);
		Set<BusSegment> toUnify = facesToUnify.stream()
				.map((p) -> CircuitBlock.getBusSegmentAt(worldIn, p).get())
				.collect(Collectors.toSet());
		if (!toUnify.isEmpty()) {                              ;
			BusSegment overlord = toUnify.stream().findAny().get();
			for (BusSegment seg : toUnify) {
				if (overlord != seg) {
					overlord.unifyWith(worldIn, seg);
				}
			}
		}

	}
	
    /**
     * Upon placing a bus, we need to determine whether or not this bus bridges two bus segments,
     * and update existing bus segments accordingly
     * IBlockstate
     */
	@Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer)
    {
		connectOnPlace(worldIn, pos, meta);
        return this.getStateFromMeta(meta);
    }
	
	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state)
	{
		//Here, we'll need to separate BusSegments that have been split up by this change. 
		
		int meta = getMetaFromState(state);
		Predicate<BlockPos> connectable = connectablePredicate(worldIn, pos, meta);
		Predicate<BlockFace> circuit = circuitPredicate(worldIn, pos, meta);
		
		super.breakBlock(worldIn, pos, state);

		Set<Set<BlockFace>> partition = IncrementalConnectedComponents.separateOnDelete(pos, connectable, circuit);
		//Okay, now that we got a new partition of the block faces that are circuit blocks,
		//what we need to do is to create a copy of the bus segment they used to share in common
		//and then use that to inform new bus segment assignments
		if (partition.isEmpty() || partition.iterator().next().isEmpty()) {
			return;
		}
		BlockFace sampleFace = partition.iterator().next().iterator().next();
		Optional<BusSegment> maybeSeg = CircuitBlock.getBusSegmentAt(worldIn, sampleFace);
		if (!maybeSeg.isPresent()) {
			Log.internalError("BusBlock#breakBlock -- separateOnDelete returned a circuit TE face that doesn't exist!");
			return;
		}
		BusSegment parentSeg = maybeSeg.get();
		for (Set<BlockFace> equivClass : partition) {
			//For each equivalence class, split off a new bus segment filtered on the inputs/outputs of the segment
			BusSegment classSeg = parentSeg.splitOff(equivClass);
			for (BlockFace bf : equivClass) {
				CircuitBlock.setBusSegmentAt(worldIn, bf, classSeg);
			}
		}
			
	}

	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer()
	{
		return BlockRenderLayer.SOLID;
	}
	//TODO: Intelligently cull faces here.
	@Override
	public boolean isOpaqueCube(IBlockState iBlockState) {
		return false;
	}
	@Override
	public boolean isFullCube(IBlockState iBlockState) {
		return false;
	}
	@Override
	public boolean isFullyOpaque(IBlockState iBlockState) {
		return false;
	}
	
	@Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
    	if (getActualState(state, source, pos).getValue(BusBlock.FACING).isFullBlock()) {
    		return Block.FULL_BLOCK_AABB;
    	}
        return new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.5D, 1.0D);
    }
	
    @Override
    public void addCollisionBoxToList(IBlockState state, World worldIn, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn)
    {
        addCollisionBoxToList(pos, entityBox, collidingBoxes, getBoundingBox(state, worldIn, pos));
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public AxisAlignedBB getSelectedBoundingBox(IBlockState state, World worldIn, BlockPos pos)
    {
        return this.getBoundingBox(state, worldIn, pos).offset(pos);
    }


	@Override
	public String getSpecialName(ItemStack stack) {
		return BusBlock.busWidths[stack.getItemDamage()].getName();
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		return new ItemStack(Item.getItemFromBlock(this), 1, this.getMetaFromState(world.getBlockState(pos)));
	}
} 
