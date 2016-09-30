package minecraftbyexample.mbe08_creative_tab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

// This creative tab is very similar to the basic CreativeTab, but overrides displayAllReleventItems to
//  customise the list of displayed items - filters through all the items looking for ones whose name starts
//  with "mbe"

public class AllMbeItemsTab extends CreativeTabs {
  public AllMbeItemsTab(String label) {
    super(label);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public Item func_78016_d() {
    return Items.field_151122_aG;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void func_78018_a(List<ItemStack> itemsToShowOnTab)
  {
    for (Item item : Item.field_150901_e) {
      if (item != null) {
        if (item.func_77658_a().contains(".mbe")) {
          item.func_150895_a(item, this, itemsToShowOnTab);  // add all sub items to the list
        }
      }
    }
  }

}
