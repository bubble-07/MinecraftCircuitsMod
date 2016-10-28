package com.circuits.circuitsmod.circuitblock;


import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.CircuitUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

public class CircuitEntitySpecialRenderer extends TileEntitySpecialRenderer<CircuitTileEntity> {
	
	public CircuitEntitySpecialRenderer() {}
	
	@Override
	public void renderTileEntityAt(CircuitTileEntity tileEntity, double x, double y, double z, float f, int state) {
		
		CircuitUID uid = tileEntity.getCircuitUID();
		
		if (uid == null) { return; }
		
		Tessellator tessellator = Tessellator.getInstance();
		ResourceLocation rsc = CircuitInfoProvider.getTexture(uid);
		if (rsc != null) {

			this.bindTexture(rsc);
			
			float[][] texCoords = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};
			int i = 0;
			if (tileEntity.getParentFacing() != null) {
				i = (tileEntity.getParentFacing().getHorizontalIndex() + 3) % 4;
			}
			
			Minecraft.getMinecraft().renderEngine.bindTexture(rsc);

			GlStateManager.color(1F, 1F, 1F);
			VertexBuffer buf = Tessellator.getInstance().getBuffer();
			buf.begin(7, DefaultVertexFormats.POSITION_TEX);
			buf.pos(16, 16, 16).tex(texCoords[i % 4][0], texCoords[i % 4][1]).endVertex();
			buf.pos(16, 16, 0).tex(texCoords[(i+1) % 4][0], texCoords[(i+1) % 4][1]).endVertex();
			buf.pos(0, 16, 0).tex(texCoords[(i+2) % 4][0], texCoords[(i+2) % 4][1]).endVertex();
			buf.pos(0, 16, 16).tex(texCoords[(i+3) % 4][0], texCoords[(i+3) % 4][1]).endVertex();
			
			tessellator.draw();
		}
		
	}
}
