package com.circuits.circuitsmod.controlblock.gui;

import java.util.ArrayList;
import java.util.List;

public abstract class ControlGuiPage {
	
	final ControlGui parent;
	final static int screenX = 6;
	final static int screenY = 6;
	final static int screenWidth = 120;
	final static int screenHeight = 100;
	final static int scrollBarWidth = 6;
	final static int elementColor = 65280;
	
	final static int shortLabelWidth = 25;
	final static int shortLabelHeight = 16;
	
	private List<UIElement> elements = new ArrayList<>();
	
	
	public ControlGuiPage(ControlGui parent) {
		this.parent = parent;
	}
	
	protected void handleScrollDown() {
		
	}
	protected void handleScrollUp() {
		
	}
	
	protected void handleClick(int mouseX, int mouseY) {
	}
	
	protected void handleKeyboardInput(char charTyped, int keyCode) {
	}
	
	public void addElement(UIElement toAdd) {
		elements.add(toAdd);
	}
	public void removeElement(UIElement in) {
		elements.remove(in);
	}
	
	public boolean handleElementClicks(int mouseX, int mouseY) {		
		for (UIElement element : elements) {
			if (element.handleClick(mouseX, mouseY)) {
				if (element.isClickIn(mouseX, mouseY) && element instanceof UIFocusable) {
					((UIFocusable) element).requestFocus();
					//Cool! now unfocus all of the others
					for (UIElement other : elements) {
						if (other != element && other instanceof UIFocusable) {
							((UIFocusable) other).unFocus();
						}
					}
				}
				return true;
			}
		}
		return false;
	}
	
	public void handleElementKeys(char typed, int keyCode) {
		elements.stream().forEach(element -> element.handleKey(typed, keyCode));
	}
	
	public void drawElements() {
		elements.stream().forEach(UIElement::draw);
	}
	
	protected abstract void draw();
}
