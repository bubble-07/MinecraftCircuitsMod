package com.circuits.circuitsmod.circuitblock;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CircuitRendererManager {
	public CircuitRendererManager() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	CircuitSmartModel itemRenderer = null;
	
	public void registerItemRenderer(CircuitItem item, CircuitSmartModel itemRenderer) {
		this.itemRenderer = itemRenderer;
		item.setRenderer(itemRenderer);
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, CircuitSmartModel.variantTag);
	}
	
	@SubscribeEvent
	public void bakeModel(ModelBakeEvent event) {
		itemRenderer = new CircuitSmartModel();
		event.getModelRegistry().putObject(CircuitSmartModel.variantTag, itemRenderer);
	}
	
}
