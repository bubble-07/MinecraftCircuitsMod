package com.circuits.circuitsmod.controlblock.gui;

import java.awt.Color;
import java.io.IOException;

import org.lwjgl.opengl.GL11;

import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.controlblock.ControlContainer;
import com.circuits.circuitsmod.controlblock.ControlTileEntity;
import com.circuits.circuitsmod.controlblock.gui.model.CircuitListModel;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ControlGui extends GuiContainer {
	public CircuitListModel model;
	ControlTileEntity tileEntity;
	ControlGuiPage currentPage;
	EntityPlayer user;
	
	
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
		
		this.ySize = (5 * this.ySize) / 4;
		
		if (tileEntity.getWorld().isRemote) {
			CircuitInfoProvider.ensureClientModelInit();
		}
		model = CircuitInfoProvider.getCircuitListModel();
		this.tileEntity = tileEntity;
		
		this.user = inventoryPlayer.player;
		user.getUniqueID();
		
		this.setDisplayPage(new ServerWaitPage(this));
	}
	
	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		int scroll = org.lwjgl.input.Mouse.getEventDWheel();
		if (scroll > 0) {
			currentPage.handleScrollUp();
		}
		if (scroll < 0) {
			currentPage.handleScrollDown();
		}
	}
	
	@Override
	protected void mouseClicked(int mouseX, int mouseY, int button) throws IOException {
		super.mouseClicked(mouseX, mouseY, button);
		
		if (button != 0) {
			return;
		}
		mouseX -= this.guiLeft;
		mouseY -= this.guiTop;
		
		if (mouseX > ControlGuiPage.screenX && 
				mouseX < ControlGuiPage.screenX + ControlGuiPage.screenWidth + ControlGuiPage.scrollBarWidth &&
			mouseY > 0 && mouseY < ControlGuiPage.screenHeight) {
			
			
			if (mouseX > ControlGuiPage.screenX + ControlGuiPage.screenWidth && mouseX < ControlGuiPage.screenX + ControlGuiPage.screenWidth + 
					ControlGuiPage.scrollBarWidth &&
					mouseY < ControlGuiPage.screenHeight && mouseY > 0) {
				//Must be tryin' to scroll
				if (mouseY > (ControlGuiPage.screenHeight / 2)) {
					currentPage.handleScrollDown();
				}
				else {
					currentPage.handleScrollUp();
				}
			}	
			
			if (!currentPage.handleElementClicks(mouseX, mouseY)) {
				currentPage.handleClick(mouseX, mouseY);	
			}
		}
	}
	
	@Override
	protected void keyTyped(char typed, int keyCode) throws IOException {
		super.keyTyped(typed, keyCode);
		if (keyCode == 200) {
			currentPage.handleScrollUp();
		}
		if (keyCode == 208) {
			currentPage.handleScrollDown();
		}
		currentPage.handleKeyboardInput(typed, keyCode);
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
		ResourceLocation loc = new ResourceLocation("circuitsMod", "textures/gui/controlGui.png");
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.renderEngine.bindTexture(loc);
		int x = (width - xSize) / 2;
		int y = (height - ySize) / 2;
		this.drawTexturedModalRect(x, y, 0, 0, xSize, ySize);
	}
}
