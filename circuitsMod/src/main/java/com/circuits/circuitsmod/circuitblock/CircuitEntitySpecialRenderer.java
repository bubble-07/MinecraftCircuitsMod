package com.circuits.circuitsmod.circuitblock;


import java.util.Optional;

import org.lwjgl.opengl.GL11;

import com.circuits.circuitsmod.busblock.BusSegment;
import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

public class CircuitEntitySpecialRenderer extends TileEntitySpecialRenderer<CircuitTileEntity> {
	
	public CircuitEntitySpecialRenderer() {}
	
	private static float[] getColorForFace(CircuitTileEntity tileEntity, EnumFacing face) {
		//TODO: Whenever we figure out what to communicate over the network w.r.t.
		//input/output face mappings, update this so that it __actually__ yields the correct mappings
		//for the bus segments
		Optional<BusSegment> seg = tileEntity.getBusSegment(face);
		
		if (seg.isPresent()) {
			//Special case: analog input/output
			
			if (seg.get().getWidth() == 4 && tileEntity.isAnalog(face)) {
				return new float[]{1.0f, 0.0f, 0.0f};
			}
			
			switch (seg.get().getWidth()) {
			case 1:
				return new float[]{1.0f, 0.0f, 0.0f};
			case 2:
				return new float[]{1.0f, 0.5f, 0.0f};
			case 4:
				return new float[]{1.0f, 1.0f, 0.5f};
			case 8:
				return new float[]{0.0f, 1.0f, 0.0f};
			case 16:
				return new float[]{0.0f, 0.0f, 1.0f};
			case 32:
				return new float[]{1.0f, 0.0f, 1.0f};
			case 64:
				return new float[]{0.9f, 0.9f, 0.9f};
			}
		}
		
		return new float[]{0.0f, 0.0f, 0.0f};
	}
	
	@Override
	public void renderTileEntityAt(CircuitTileEntity tileEntity, double x, double y, double z, float f, int state) {
		
		SpecializedCircuitUID uid = tileEntity.getCircuitUID();
		if (!tileEntity.isClientInit()) {
			tileEntity.tryInitClient();
			return;
		}
		
		if (uid == null) { return; }
		
		Tessellator tessellator = Tessellator.getInstance();
		ResourceLocation rsc = CircuitInfoProvider.getTexture(uid.getUID());
		if (rsc != null) {
			
			GL11.glPushMatrix();
			GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
			
			GlStateManager.translate(x, y, z);

			this.bindTexture(rsc);
			
			GL11.glDisable(GL11.GL_LIGHTING);
			GL11.glDisable(GL11.GL_BLEND);
			GlStateManager.clearColor(0, 0, 0, 0);
			GlStateManager.color(1.0f, 1.0f, 1.0f);
			OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 255, 0);
			GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
			
			float[][] texCoords = {{0, 1}, {1, 1}, {1, 0}, {0, 0}};
			int i = 0;
			if (tileEntity.getParentFacing() != null) {
				i = (tileEntity.getParentFacing().getHorizontalIndex() + 3) % 4;
			}
			
			double height = 0.5;
			
			
			//Render the top face, which displays the icon for the circuit
			VertexBuffer buf = Tessellator.getInstance().getBuffer();
			buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buf.pos(1.0, height, 1.0).tex(texCoords[i % 4][0], texCoords[i % 4][1]).endVertex();
			buf.pos(1.0, height, 0).tex(texCoords[(i+1) % 4][0], texCoords[(i+1) % 4][1]).endVertex();
			buf.pos(0, height, 0).tex(texCoords[(i+2) % 4][0], texCoords[(i+2) % 4][1]).endVertex();
			buf.pos(0, height, 1.0).tex(texCoords[(i+3) % 4][0], texCoords[(i+3) % 4][1]).endVertex();
			tessellator.draw();
			GlStateManager.disableTexture2D();
			
			double[][] xzcoords = {{0, 0}, {1, 0}, {1, 1}, {0, 1}};
			
			buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
			//Now, render all of the sides, which are colored according to what bus inputs are expected
			for (int j = 0; j < 4; j++) {
				int ind = (i + j) % 4;
				EnumFacing facing = EnumFacing.getHorizontal((i + j + 2) % 4);
				float[] colorChannels = getColorForFace(tileEntity, facing);
				float r = colorChannels[0];
				float g = colorChannels[1];
				float b = colorChannels[2];
				buf.pos(xzcoords[ind][0], 0, xzcoords[ind][1]).color(r, g, b, 1.0f).endVertex();
				buf.pos(xzcoords[ind][0], height, xzcoords[ind][1]).color(r, g, b, 1.0f).endVertex();
				buf.pos(xzcoords[(ind + 1) % 4][0], height, xzcoords[(ind + 1) % 4][1]).color(r, g, b, 1.0f).endVertex();
				buf.pos(xzcoords[(ind + 1) % 4][0], 0, xzcoords[(ind + 1) % 4][1]).color(r, g, b, 1.0f).endVertex();
			}
			//Draw the bottom face
			for (int j = 3; j >= 0; j--) {
				buf.pos(xzcoords[j][0], 0, xzcoords[j][1]).color(0.0f, 0.0f, 0.0f, 1.0f).endVertex();
			}
			tessellator.draw();
			
			GlStateManager.enableTexture2D();
			
			GlStateManager.pushMatrix();
			GlStateManager.rotate(90, 1, 0, 0);
			GlStateManager.translate(0, 0.0, -height - 0.01);
			GlStateManager.scale(0.01, 0.01, 0.01);
			
			

			Minecraft.getMinecraft().fontRendererObj.drawString(uid.getOptions().getRawDispString(), 0, 0, 0, false);
			GL11.glPopMatrix();
			
			
			GL11.glPopAttrib();
			GL11.glPopMatrix();
		}
		
	}
}
