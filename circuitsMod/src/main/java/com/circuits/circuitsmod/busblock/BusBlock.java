package com.circuits.circuitsmod.busblock;

import java.util.Collection;

import com.circuits.circuitsmod.common.OptionalUtils;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
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
 * Correspondingly, the lower 2 bits of the Bus Block's 4-bit metadata will be used to store these four possible states.
 * The other two bits will be used to store the type of the bus -- this means that there are in fact __two__ different bus blocks
 */

public abstract class BusBlock extends Block
{
  public BusBlock()
  {
    super(Material.ROCK);
    this.setCreativeTab(CreativeTabs.BUILDING_BLOCKS);   // the block will appear on the Blocks tab in creative
  }
  
  protected BitWidth[] smallBusWidths = {BitWidth.TWOBIT, BitWidth.FOURBIT, BitWidth.EIGHTBIT, BitWidth.SIXTEENBIT};
  protected BitWidth[] largeBusWidths = {BitWidth.THIRTYTWOBIT, BitWidth.SIXTYFOURBIT};
  
  protected int busBank = 0; //0 if a small bus, 1 if a large bus.
  
  /**
   * The different facing directions (North/South, East/West, Up/Down, Cap) of a bus block
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
	  TWOBIT(0, 2, "twobit"),
	  FOURBIT(1, 4, "fourbit"),
	  EIGHTBIT(2, 8, "eightbit"),
	  SIXTEENBIT(3, 16, "sixteenbit"),
	  THIRTYTWOBIT(0, 32, "thirtytwobit"),
	  SIXTYFOURBIT(1, 64, "sixtyfourbit");
	  
	  /**
	   * The metadata tag stored in the high bits of the bus' metadata for this given bit width.
	   */
	  private final int meta_tag;
	  /**
	   * The actual bit width of the bus
	   */
	  private final int bit_width;
	  
	  private final String name;
	  
	  BitWidth(int meta_tag, int bit_width, String name) {
		  this.meta_tag = meta_tag;
		  this.bit_width = bit_width;
		  this.name = name;
	  }
	  
	  public int getTag() {
		  return this.meta_tag;
	  }
	  public int getWidth() {
		  return this.bit_width;
	  }
	  public String getName() {
		  return name;
	  }
	  
  }  
  
  /**
   * Possible property values for bus widths. An abstract class, since
   * the allowed bus widths are different for the different buses!
   * @author bubble-07
   *
   */
  public abstract class BusWidthProperty implements IProperty<BitWidth> {

	@Override
	public String getName() {
		return "buswidth";
	}

	@Override
	public Class<BitWidth> getValueClass() {
		return BitWidth.class;
	}

	@Override
	public Optional<BitWidth> parseValue(String value) {
		return OptionalUtils.toGoogle(getAllowedValues().stream()
				                      .filter((v) -> v.getName().equals(value))
				                      .findFirst());
	}

	@Override
	public String getName(BitWidth value) {
		return value.getName();
	}
	  
  }
  
  public abstract IProperty<BitWidth> widthProperty();
  
  public abstract BitWidth lookupWidth(int meta);
  
  public static class NarrowBusBlock extends BusBlock {

	@Override
	public IProperty<BitWidth> widthProperty() {
		return PropertyEnum.create("bitwidth", BitWidth.class, new BitWidth[]{BitWidth.TWOBIT, BitWidth.FOURBIT, BitWidth.EIGHTBIT, BitWidth.SIXTEENBIT});
	}

	@Override
	public BitWidth lookupWidth(int meta) {
		return this.smallBusWidths[meta >> 2]; 
	} 
  }
  
  public static class WideBusBlock extends BusBlock {

	@Override
	public IProperty<BitWidth> widthProperty() {
		return PropertyEnum.create("bitwidth", BitWidth.class, new BitWidth[]{BitWidth.THIRTYTWOBIT, BitWidth.SIXTYFOURBIT});
	}

	@Override
	public BitWidth lookupWidth(int meta) {
		return this.largeBusWidths[meta >> 2]; 
	} 
  }
  
  /**
   * Convert the given metadata into a BlockState for this Block
   */
  public IBlockState getStateFromMeta(int meta)
  {
      return this.getDefaultState().withProperty(FACING, BusFacing.fromMeta(meta & 3)).withProperty(widthProperty(), lookupWidth(meta >> 2));
  }

  /**
   * Convert the BlockState into the correct metadata value
   */
  public int getMetaFromState(IBlockState state)
  {
      int i = 0;
      i = i | ((BusFacing)state.getValue(FACING)).getMeta();
      
      int width = ((BitWidth)state.getValue(widthProperty())).getWidth();
      i = i + (width << 2);
      return i;
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
} 
