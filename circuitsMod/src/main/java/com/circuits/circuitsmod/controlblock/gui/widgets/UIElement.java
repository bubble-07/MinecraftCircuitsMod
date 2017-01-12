package com.circuits.circuitsmod.controlblock.gui.widgets;

import com.circuits.circuitsmod.controlblock.gui.ControlGui;

public abstract class UIElement {
	protected int x;
	protected int y;
	protected int width;
	protected int height;
	protected ControlGui parent;

	public UIElement(ControlGui parent, int x, int y, int width, int height) {
		this.x = x;
		this.parent = parent;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public boolean isClickIn(int mouseX, int mouseY) {
		return (x < mouseX && x + width > mouseX && y < mouseY && y + height > mouseY);
	}

	public boolean handleClick(int mouseX, int mouseY) {
		if (isClickIn(mouseX, mouseY)) {
			handleClick();
			return true;
		}
		return false;
	}
	
	public void handleKey(char typed, int keyCode) { }
	
	public boolean handleClick() { return false; }
	public abstract void draw();
}

