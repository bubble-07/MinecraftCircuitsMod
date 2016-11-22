package com.circuits.circuitsmod.circuitblock;


import org.lwjgl.opengl.GL11;

import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.CircuitUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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
			
			GL11.glPushMatrix();
			GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
			
			GlStateManager.translate(x, y, z);

			this.bindTexture(rsc);
			
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_BLEND);
			GlStateManager.color(1.0f, 1.0f, 1.0f);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 255, 0);
			
			float[][] texCoords = {{0, 1}, {1, 1}, {1, 0}, {0, 0}};
			int i = 0;
			if (tileEntity.getParentFacing() != null) {
				i = (tileEntity.getParentFacing().getHorizontalIndex() + 1) % 4;
			}

			VertexBuffer buf = Tessellator.getInstance().getBuffer();
			buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buf.pos(1.0, 0.5, 1.0).tex(texCoords[i % 4][0], texCoords[i % 4][1]).endVertex();
			buf.pos(1.0, 0.5, 0).tex(texCoords[(i+1) % 4][0], texCoords[(i+1) % 4][1]).endVertex();
			buf.pos(0, 0.5, 0).tex(texCoords[(i+2) % 4][0], texCoords[(i+2) % 4][1]).endVertex();
			buf.pos(0, 0.5, 1.0).tex(texCoords[(i+3) % 4][0], texCoords[(i+3) % 4][1]).endVertex();

			tessellator.draw();
			GL11.glPopAttrib();
			GL11.glPopMatrix();
		}
		
	}
}
