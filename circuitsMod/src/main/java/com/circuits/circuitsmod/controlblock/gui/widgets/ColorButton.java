package com.circuits.circuitsmod.controlblock.gui.widgets;

import net.minecraft.client.gui.Gui;

import com.circuits.circuitsmod.controlblock.gui.ControlGui;
import com.circuits.circuitsmod.controlblock.gui.model.CustomCircuitImage;

public class ColorButton extends UIElement {
	
	private final int colorToDraw;
	private final Runnable onClick;

	public ColorButton(ControlGui parent, int x, int y, int width, int height, CustomCircuitImage.Color color, Runnable onClick) {
		super(parent, x, y, width, height);
		colorToDraw = CustomCircuitImage.toGUIColor(color);
		this.onClick = onClick;
	}

	@Override
	public void draw() {
		Gui.drawRect(x - 2, y - 2, 
		        x + width, y + height, colorToDraw);
		parent.drawBox(x, y, width, height);
	}
	
	@Override
	public boolean handleClick() {
		onClick.run();
		return true;
	}

}
