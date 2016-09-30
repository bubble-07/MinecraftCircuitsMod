package minecraftbyexample.mbe03_block_variants;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

/**
 * User: The Grey Ghost
 * Date: 27/12/2014
 * We need a custom item to represent the different sub-types (colours) of BlockVariants.
 * The Itemstack metadata represents the subtype.
 * You could also re-use ItemMultiTexture or ItemCloth or ItemColored.
 * Look at Item.registerItems() for inspiration
 */
public class ItemBlockVariants extends ItemBlock
{
  // you must use Block in the constructor, not BlockVariants, otherwise you won't be able to register the block properly.
  //   i.e. using GameRegistry.registerBlock(block, ItemBlockVariants.class, name)
  public ItemBlockVariants(Block block)
  {
    super(block);
    this.func_77656_e(0);
    this.func_77627_a(true);
  }

  @Override
  public int func_77647_b(int metadata)
  {
    return metadata;
  }

  // create a unique unlocalised name for each colour, so that we can give each one a unique name
  @Override
  public String func_77667_c(ItemStack stack)
  {
    BlockVariants.EnumColour colour = BlockVariants.EnumColour.byMetadata(stack.func_77960_j());
    return super.func_77658_a() + "." + colour.toString();
  }
}
