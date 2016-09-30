package minecraftbyexample.mbe30_inventory_basic;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * User: brandon3055
 * Date: 06/01/2015
 *
 * GuiInventoryBasic is a simple gui that does nothing but draw a background image and a line of text on the screen
 * everything else is handled by the vanilla container code
 */
@SideOnly(Side.CLIENT)
public class GuiInventoryBasic extends GuiContainer {

	// This is the resource location for the background image for the GUI
	private static final ResourceLocation texture = new ResourceLocation("minecraftbyexample", "textures/gui/mbe30_inventory_basic_bg.png");
	private TileEntityInventoryBasic tileEntityInventoryBasic;

	public GuiInventoryBasic(InventoryPlayer invPlayer, TileEntityInventoryBasic tile) {
		super(new ContainerBasic(invPlayer, tile));
		tileEntityInventoryBasic = tile;
		// Set the width and height of the gui.  Should match the size of the texture!
		field_146999_f = 176;
		field_147000_g = 133;
	}

	// draw the background for the GUI - rendered first
	@Override
	protected void func_146976_a(float partialTicks, int x, int y) {
		// Bind the image texture of our custom container
		Minecraft.func_71410_x().func_110434_K().func_110577_a(texture);
		// Draw the image
		GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
		func_73729_b(field_147003_i, field_147009_r, 0, 0, field_146999_f, field_147000_g);
	}

	// draw the foreground for the GUI - rendered after the slots, but before the dragged items and tooltips
	// renders relative to the top left corner of the background
	@Override
	protected void func_146979_b(int mouseX, int mouseY) {
		final int LABEL_XPOS = 5;
		final int LABEL_YPOS = 5;
		field_146289_q.func_78276_b(tileEntityInventoryBasic.func_145748_c_().func_150260_c(), LABEL_XPOS, LABEL_YPOS, Color.darkGray.getRGB());
	}
}
