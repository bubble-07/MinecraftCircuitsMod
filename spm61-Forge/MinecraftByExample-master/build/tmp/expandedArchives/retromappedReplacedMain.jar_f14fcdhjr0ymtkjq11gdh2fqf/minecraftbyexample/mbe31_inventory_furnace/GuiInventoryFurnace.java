package minecraftbyexample.mbe31_inventory_furnace;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * User: brandon3055
 * Date: 06/01/2015
 *
 * GuiInventoryAdvanced is a gui similar to that of a furnace. It has a progress bar and a burn time indicator.
 * Both indicators have mouse over text
 */
@SideOnly(Side.CLIENT)
public class GuiInventoryFurnace extends GuiContainer {

	// This is the resource location for the background image
	private static final ResourceLocation texture = new ResourceLocation("minecraftbyexample", "textures/gui/mbe31_inventory_furnace_bg.png");
	private TileInventoryFurnace tileEntity;

	public GuiInventoryFurnace(InventoryPlayer invPlayer, TileInventoryFurnace tileInventoryFurnace) {
		super(new ContainerInventoryFurnace(invPlayer, tileInventoryFurnace));

		// Set the width and height of the gui
		field_146999_f = 176;
		field_147000_g = 207;

		this.tileEntity = tileInventoryFurnace;
	}

	// some [x,y] coordinates of graphical elements
	final int COOK_BAR_XPOS = 49;
	final int COOK_BAR_YPOS = 60;
	final int COOK_BAR_ICON_U = 0;   // texture position of white arrow icon
	final int COOK_BAR_ICON_V = 207;
	final int COOK_BAR_WIDTH = 80;
	final int COOK_BAR_HEIGHT = 17;

	final int FLAME_XPOS = 54;
	final int FLAME_YPOS = 80;
	final int FLAME_ICON_U = 176;   // texture position of flame icon
	final int FLAME_ICON_V = 0;
	final int FLAME_WIDTH = 14;
	final int FLAME_HEIGHT = 14;
	final int FLAME_X_SPACING = 18;

	@Override
	protected void func_146976_a(float partialTicks, int x, int y) {
		// Bind the image texture
		Minecraft.func_71410_x().func_110434_K().func_110577_a(texture);
		// Draw the image
		GlStateManager.func_179131_c(1.0F, 1.0F, 1.0F, 1.0F);
		func_73729_b(field_147003_i, field_147009_r, 0, 0, field_146999_f, field_147000_g);

		// get cook progress as a double between 0 and 1
		double cookProgress = tileEntity.fractionOfCookTimeComplete();
		// draw the cook progress bar
		func_73729_b(field_147003_i + COOK_BAR_XPOS, field_147009_r + COOK_BAR_YPOS, COOK_BAR_ICON_U, COOK_BAR_ICON_V,
						              (int)(cookProgress * COOK_BAR_WIDTH), COOK_BAR_HEIGHT);

		// draw the fuel remaining bar for each fuel slot flame
		for (int i = 0; i < tileEntity.FUEL_SLOTS_COUNT; ++i) {
			double burnRemaining = tileEntity.fractionOfFuelRemaining(i);
			int yOffset = (int)((1.0 - burnRemaining) * FLAME_HEIGHT);
			func_73729_b(field_147003_i + FLAME_XPOS + FLAME_X_SPACING * i, field_147009_r + FLAME_YPOS + yOffset,
														FLAME_ICON_U, FLAME_ICON_V + yOffset, FLAME_WIDTH, FLAME_HEIGHT - yOffset);
		}
	}

	@Override
	protected void func_146979_b(int mouseX, int mouseY) {
		super.func_146979_b(mouseX, mouseY);

		final int LABEL_XPOS = 5;
		final int LABEL_YPOS = 5;
		field_146289_q.func_78276_b(tileEntity.func_145748_c_().func_150260_c(), LABEL_XPOS, LABEL_YPOS, Color.darkGray.getRGB());

		List<String> hoveringText = new ArrayList<String>();

		// If the mouse is over the progress bar add the progress bar hovering text
		if (isInRect(field_147003_i + COOK_BAR_XPOS, field_147009_r + COOK_BAR_YPOS, COOK_BAR_WIDTH, COOK_BAR_HEIGHT, mouseX, mouseY)){
			hoveringText.add("Progress:");
			int cookPercentage =(int)(tileEntity.fractionOfCookTimeComplete() * 100);
			hoveringText.add(cookPercentage + "%");
		}

		// If the mouse is over one of the burn time indicator add the burn time indicator hovering text
		for (int i = 0; i < tileEntity.FUEL_SLOTS_COUNT; ++i) {
			if (isInRect(field_147003_i + FLAME_XPOS + FLAME_X_SPACING * i, field_147009_r + FLAME_YPOS, FLAME_WIDTH, FLAME_HEIGHT, mouseX, mouseY)) {
				hoveringText.add("Fuel Time:");
				hoveringText.add(tileEntity.secondsOfFuelRemaining(i) + "s");
			}
		}
		// If hoveringText is not empty draw the hovering text
		if (!hoveringText.isEmpty()){
			drawHoveringText(hoveringText, mouseX - field_147003_i, mouseY - field_147009_r, field_146289_q);
		}
//		// You must re bind the texture and reset the colour if you still need to use it after drawing a string
//		Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
//		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

	}

	// Returns true if the given x,y coordinates are within the given rectangle
	public static boolean isInRect(int x, int y, int xSize, int ySize, int mouseX, int mouseY){
		return ((mouseX >= x && mouseX <= x+xSize) && (mouseY >= y && mouseY <= y+ySize));
	}
}
