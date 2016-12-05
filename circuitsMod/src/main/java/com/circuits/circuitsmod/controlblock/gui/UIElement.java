package com.circuits.circuitsmod.controlblock.gui;

public abstract class UIElement {
	int x;
	int y;
	int width;
	int height;
	ControlGui parent;

	public UIElement(ControlGui parent, int x, int y, int width, int height) {
		this.x = x;
		this.parent = parent;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	protected boolean isClickIn(int mouseX, int mouseY) {
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

