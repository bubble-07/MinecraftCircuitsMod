package com.circuits.circuitsmod.controlblock.gui;

import java.awt.Color;
import java.io.IOException;
import org.lwjgl.opengl.GL11;

import com.circuits.circuitsmod.controlblock.frompoc.CircuitListModel;
import com.circuits.circuitsmod.controlblock.frompoc.ControlContainer;
import com.circuits.circuitsmod.controlblock.frompoc.ControlTileEntity;
import com.circuits.circuitsmod.controlblock.frompoc.Microchips;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ControlGui extends GuiContainer {
	public CircuitListModel model;
	ControlTileEntity tileEntity;
	ControlGuiPage currentPage;
	
	public FontRenderer getFontRenderer() {
		return fontRendererObj;
	}
	
	public void drawHorizontalLine(int startX, int endX, int y, int color) {
		//TODO: Figure out how to convert the color argument!
		super.drawHorizontalLine(startX, endX, y, Color.GREEN.getRGB());
	}
	
	public void drawVerticalLine(int x, int startY, int endY, int color) {
		super.drawVerticalLine(x, startY, endY, Color.GREEN.getRGB());
	}
	
	public void drawBox(int x, int y, int width, int height) {
		drawHorizontalLine(x - 2, x + width, y - 3, ControlGuiPage.elementColor);
		drawHorizontalLine(x - 2, x + width, y + height, ControlGuiPage.elementColor);
		drawVerticalLine(x - 2, y - 3, y + height, ControlGuiPage.elementColor);
		drawVerticalLine(x + width, y - 3, y + height, ControlGuiPage.elementColor);
	}
	
	public void renderItemStack(ItemStack material, int x, int y) {
		RenderHelper.enableGUIStandardItemLighting();
		this.itemRender.renderItemAndEffectIntoGUI(material, x, y);
		this.itemRender.renderItemOverlayIntoGUI(fontRendererObj, material, x, y, "");
		RenderHelper.disableStandardItemLighting();
	}
	
	public RenderItem getItemRender() {
		return itemRender;
	}
	
	public void setDisplayPage(ControlGuiPage page) {
		this.currentPage = page;
	}
	
	public ControlGui(InventoryPlayer inventoryPlayer, ControlTileEntity tileEntity) {
		super(new ControlContainer(inventoryPlayer, tileEntity));
		
		if (tileEntity.getWorld().isRemote) {
			Microchips.requestClientModelUpdate();
		}
		model = Microchips.mainModel;
		this.tileEntity = tileEntity;
		
		this.setDisplayPage(new ServerWaitPage(this));
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
		super.mouseClicked(mouseX, mouseY, button);
		
		if (button != 0) {
			return;
		}
		
		//TODO: Make this more robust in full-screen
		
		//Convert to more useful coordinates
		mouseX -= 125;
		mouseY -= 42;
		
		if (mouseX > ControlGuiPage.screenX && 
				mouseX < ControlGuiPage.screenX + ControlGuiPage.screenWidth + ControlGuiPage.scrollBarWidth &&
			mouseY > 0 && mouseY < ControlGuiPage.screenHeight) {
			
			if (!currentPage.handleElementClicks(mouseX, mouseY)) {
				currentPage.handleClick(mouseX, mouseY);	
			}
		}
		System.out.println("Mouse pos" + mouseX + "," + mouseY + ":" + button);	
	}
	
	@Override
	protected void keyTyped(char typed, int keyCode) throws IOException {
		super.keyTyped(typed, keyCode);
		currentPage.handleElementKeys(typed, keyCode);
	}
		
	@Override
	protected void drawGuiContainerForegroundLayer(int param1, int param2) {
		currentPage.draw();
		currentPage.drawElements();
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		//Drawin' GUI stuffs
		ResourceLocation loc = new ResourceLocation("microchips", "textures/gui/controlGui.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(loc);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}
}
