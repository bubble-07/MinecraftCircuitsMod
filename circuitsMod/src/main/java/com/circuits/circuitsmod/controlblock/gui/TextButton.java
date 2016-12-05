package com.circuits.circuitsmod.controlblock.gui;

public class TextButton extends UIElement {
	Runnable onClick;
	String text;
	public TextButton(ControlGui parent, String text, int x, int y, Runnable action) {
		super(parent, x, y, parent.getFontRenderer().getStringWidth(text), parent.getFontRenderer().FONT_HEIGHT);
		this.text = text;
		onClick = action;
	}
	
	public boolean handleClick() {
		onClick.run();
		return true;
	}
	
	public void draw() {
		parent.getFontRenderer().drawString(text, x, y, ControlGuiPage.elementColor);
		parent.drawBox(x, y, width, height);
	}
}
