package minecraftbyexample.mbe12_item_nbt_animate;

import minecraftbyexample.MinecraftByExample;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

/**
 * User: The Grey Ghost
 * Date: 30/12/2014
 * Item (teleportation gem) which stores NBT information and also provides a custom animation when being "used".
 * Basic usage:
 * 1) Shift-click to "store" the current location in the gem
 * 2) Hold right button down to "charge up" the gem.  When fully charged, the gem teleports you to the last saved
 *    location.  The gem is destroyed.
 */
public class ItemNBTAnimate extends Item
{
  public ItemNBTAnimate() {
    this.func_77656_e(0);
    this.func_77627_a(false);
    this.func_77625_d(1);
    this.func_77637_a(CreativeTabs.field_78026_f);   // items will appear on the Miscellaneous creative tab

    // We use a PropertyOverride for this item to change the appearance depending on the state of the property.
    //  See ItemNBTanimationTimer for more information.
    // Note - you must not addPropertyOverride on the DedicatedServer otherwise it will crash the game when
    //   your mod is run on a dedicated server, because IItemPropertyGetter does not exist there
    if (!MinecraftByExample.proxy.isDedicatedServer()) {
      this.func_185043_a(new ResourceLocation("chargefraction"), new ItemNBTanimationTimer());
    }
  }

  // When the user presses and holds right click, there are three phases:
  // 1) an initial pause, then
  // 2) a visual 'charging up' of the gem, then
  // 3) the teleportation occurs
  // NB there are twenty minecraft game loop "ticks" per second.
  static public final int CHARGE_UP_INITIAL_PAUSE_TICKS = 10;
  static public final int CHARGE_UP_DURATION_TICKS = 20;

  // if the gem is bound to a location, give it an "effect" i.e. the enchanted glint
  @Override
  public boolean func_77636_d(ItemStack stack) {
    NBTTagCompound nbtTagCompound = stack.func_77978_p();
    if (nbtTagCompound == null) return false;
    return nbtTagCompound.func_74764_b("Bound");
  }

  // what animation to use when the player holds the "use" button
  @Override
  public EnumAction func_77661_b(ItemStack stack) {
    return EnumAction.BLOCK;
  }

  // how long the player needs to hold down the right button in order to activate the gem, in ticks (1 tick = 1/20 second)
  @Override
  public int func_77626_a(ItemStack stack) {
    return CHARGE_UP_DURATION_TICKS + CHARGE_UP_INITIAL_PAUSE_TICKS;
  }

  // called when the player starts holding right click;
  // --> if the gem is unbound, store the current location
  //  if the gem is bound, start the charge up sequence
  @Override
  public ActionResult<ItemStack> func_77659_a(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
  {
    NBTTagCompound nbtTagCompound = itemStackIn.func_77978_p();

    if (playerIn.func_70093_af()) { // shift pressed; save (or overwrite) current location
      if (nbtTagCompound == null) {
        nbtTagCompound = new NBTTagCompound();
        itemStackIn.func_77982_d(nbtTagCompound);
      }
      nbtTagCompound.func_74757_a("Bound", true);
      nbtTagCompound.func_74780_a("X", (int) playerIn.field_70165_t);
      nbtTagCompound.func_74780_a("Y", (int)playerIn.field_70163_u);
      nbtTagCompound.func_74780_a("Z", (int)playerIn.field_70161_v);
      return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
    }

    boolean bound = false;
    if (nbtTagCompound != null && nbtTagCompound.func_74764_b("Bound")  ) {
      bound = nbtTagCompound.func_74767_n("Bound");
    }
    if (bound) {
      playerIn.func_184598_c(hand); // start the charge up sequence
      return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
    } else {
      if (worldIn.field_72995_K) {  // only on the client side, else you will get two messages..
        playerIn.func_146105_b(new TextComponentString("Gem doesn't have a stored location! Shift right click to store your current location"));
      }
      return new ActionResult<ItemStack>(EnumActionResult.FAIL, itemStackIn);
    }
  }

  // called when the player has held down the right click for the full charge-up duration
  // in this case - destroy the item
  @Override
  public ItemStack func_77654_b(ItemStack stack, World worldIn, EntityLivingBase entityLiving)
  {
    NBTTagCompound nbtTagCompound = stack.func_77978_p();
    if (nbtTagCompound == null || !nbtTagCompound.func_74764_b("Bound") || nbtTagCompound.func_74767_n("Bound") != true ) {
      return stack;
    }

    double x = nbtTagCompound.func_74769_h("X");  // returns a default if not present
    double y = nbtTagCompound.func_74769_h("Y");
    double z = nbtTagCompound.func_74769_h("Z");

    // teleport

    // on the client side, play the sound locally
    // on the server side, teleport the player and play the sound for all other players nearby except this player
    //  (doing it this way reduces the perceived lag for this player, i.e the sound plays instantly, instead of being
    //   delayed while the message goes to the server and comes back again)
    if (worldIn.field_72995_K) {  // client side
      worldIn.func_184134_a(x, y, z, SoundEvents.field_187534_aX, SoundCategory.PLAYERS, 1.0F, 1.0F, false);
    } else {  // server side
      if (entityLiving instanceof EntityPlayerMP) { // should be an EntityPlayerMP; check first just to be sure to avoid crash
        EntityPlayerMP entityPlayerMP = (EntityPlayerMP)entityLiving;
        entityPlayerMP.field_71135_a.func_147364_a(x, y, z, entityPlayerMP.field_70177_z, entityPlayerMP.field_70125_A);
        final EntityPlayerMP dontPlayForThisPlayer = entityPlayerMP;
        worldIn.func_184148_a(dontPlayForThisPlayer, x, y, z, SoundEvents.field_187534_aX, SoundCategory.PLAYERS, 1.0F, 1.0F);
      }
    }
    return null;
//    for items with multiple count, decrease stack size and return the itemstack, eg
//    stack.stackSize--;
//    return stack;
  }

  // adds 'tooltip' text
  @SideOnly(Side.CLIENT)
  @SuppressWarnings("unchecked")
  @Override
  public void func_77624_a(ItemStack stack, EntityPlayer playerIn, List tooltip, boolean advanced) {
    NBTTagCompound nbtTagCompound = stack.func_77978_p();
    if (nbtTagCompound != null && nbtTagCompound.func_74764_b("Bound") && nbtTagCompound.func_74767_n("Bound") == true ) {
      tooltip.add("Stored destination=");
      tooltip.add("X: " + nbtTagCompound.func_74762_e("X"));
      tooltip.add("Y: " + nbtTagCompound.func_74762_e("Y"));
      tooltip.add("Z: " + nbtTagCompound.func_74762_e("Z"));
      tooltip.add("Hold down right click to teleport.");
    }
    else
    {
      tooltip.add("Hold down shift and then right");
      tooltip.add("  click to store your current location");
    }
  }
}
