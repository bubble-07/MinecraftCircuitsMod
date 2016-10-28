package com.circuits.circuitsmod.circuitblock;

import java.util.Collections;
import java.util.List;

import javax.vecmath.Matrix4f;

import org.apache.commons.lang3.tuple.Pair;
import org.lwjgl.opengl.GL11;

import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.CircuitUID;
import com.google.common.collect.Lists;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.IPerspectiveAwareModel;

//This thing uses a ridiculously-hacky way to get direct GL access for item rendering.
//When this gets closer to release, maybe put in the time investment to learn how to hijack
//the vanilla model loading code to load full-blown custom models for each circuit.
//Then, write a nice script to auto-gen models based on some default template
//with the old textures on their faces. For now, we don't really care, because this
//is a trivial visual thing.
public class CircuitSmartModel implements IBakedModel {
	
	CircuitUID circuitUID = null;
	
	TransformType transformType = null;
	EntityPlayer owner = null;
	
	ModelResourceLocation resourceLocation;
	
	public CircuitSmartModel(ModelResourceLocation resourceLocation) {
		this.resourceLocation = resourceLocation;
	}
	
	protected boolean onGround() {
		return transformType == null;
	}
	
	
	public void setOwner(EntityPlayer player) {
		this.owner = player;
	}
	//TODO: It used to be that you could set the item perspective... what do?
	
	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}
	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}
	@Override
	public boolean isGui3d() {
		return true;
	}
	//TODO: Change this to make sense for all orientations
	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		ItemStack test = new ItemStack(Items.CARROT, 1);
		IBakedModel sample = Minecraft.getMinecraft().getRenderItem().getItemModelMesher().getItemModel(test);
		//Minecraft.getMinecraft().getRenderItem().renderItemIntoGUI(stack, x, y);
		return sample.getItemCameraTransforms();
		//getItemCameraTransforms();
		//return Items.apple.getModel(new ItemStack(Items.apple, 1), owner, 0);
		//return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public List<BakedQuad> getQuads(IBlockState state, EnumFacing side,
			long rand) {
		//Uses the same technique as the one found here:
		//https://gist.github.com/Gliby/77a9d963b2ae3072f6cd
		//First, we finish drawing the set of quads that was already initiated upon calling this method,
		//and then we draw our own stuff.
		Tessellator tessellator = Tessellator.getInstance();
		tessellator.draw();
		
		VertexBuffer buf = tessellator.getBuffer();
		
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.5F, 0.5F, 0.5F);
		GlStateManager.scale(-1.0F, -1.0F, 1.0F);
		
		if (owner != null) {
			if (transformType == TransformType.THIRD_PERSON_LEFT_HAND) {
				if (owner.isSneaking()) {
					GlStateManager.translate(0.0F, -0.2F, 0.0F);
				}
			}
		}
		
		if (onGround()) {
			GlStateManager.scale(-2f, -2f, -2f);
		}
		
		//Render the stupid thing
		ResourceLocation rsc = CircuitInfoProvider.getTexture(circuitUID);
		if (rsc != null) {
			
			TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();
			if (textureManager != null) {
				textureManager.bindTexture(rsc);
			}
			
			GL11.glColor3f(1.0f, 1.0f, 1.0f);
			
			GlStateManager.scale(1.2, 1.2, 1.2);
			
			GlStateManager.translate(-0.5F, -0.5F, 0);
			
			GlStateManager.rotate(45, 0.0F, 1.0F, 0.0F);
			
			Minecraft.getMinecraft().renderEngine.bindTexture(rsc);
			
			buf.begin(7, DefaultVertexFormats.POSITION_TEX);
			buf.pos(16, 16, 0).tex(0, 0).endVertex();
			buf.pos(16, 0, 0).tex(1, 0).endVertex();
			buf.pos(0, 0, 0).tex(1, 1).endVertex();
			buf.pos(0, 16, 0).tex(0, 1).endVertex();
			tessellator.draw();

			buf.pos(0, 16, 0).tex(0, 1).endVertex();
			buf.pos(0, 0, 0).tex(1, 1).endVertex();
			buf.pos(16, 0, 0).tex(1, 0).endVertex();
			buf.pos(16, 16, 0).tex(0, 0).endVertex();
			tessellator.draw();
		}
		
		GlStateManager.popMatrix();
		this.owner = null;
		this.transformType = null;
		return Collections.EMPTY_LIST;
	}

	@Override
	public TextureAtlasSprite getParticleTexture() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemOverrideList getOverrides() {
		return new ItemOverrideList(Lists.newArrayList()) {
			@Override
			public
			IBakedModel handleItemState(IBakedModel originalModel, ItemStack stack, World world, EntityLivingBase entity) {
				circuitUID = stack.getDisplayName();
				return CircuitSmartModel.this;
			}	
		};
	}
}
