package com.circuits.circuitsmod.controlblock.gui.widgets;

import java.util.List;
import java.util.Optional;

import com.circuits.circuitsmod.controlblock.gui.ControlGui;
import com.circuits.circuitsmod.controlblock.gui.ControlGuiPage;


public class TextEntryBox extends UIElement implements UIFocusable {
	
	protected boolean hasFocus = false;
	protected String text = "";
	protected int cursorPos = 0;
	protected int blinkTimer = 0;
	protected static final int BLINK_DURATION = 30;
	
	public void setText(String text) {
		this.text = text;
		this.cursorPos = text.length();
	}
	
	public String getText() {
		return this.text;
	}
	
	public TextEntryBox(ControlGui parent, int x, int y, int width, int height, String init) {
		super(parent, x, y, width, height);
		this.text = init;
	}
	public TextEntryBox(ControlGui parent, int x, int y, int width, int height) {
		this(parent, x, y, width, height, "");
	}
	
	public void draw() {
		String drawableText = text;
		blinkTimer++;
		if (blinkTimer > BLINK_DURATION) {
			blinkTimer = -BLINK_DURATION;
		}
		if (blinkTimer > 0 && hasFocus) {
			drawableText = "";
			for (int i = 0; i < text.length(); i++) {
				drawableText += (i == cursorPos + 1) ? "[]" : text.charAt(i);
			}
			if (cursorPos + 1 >= text.length()) {
				drawableText += "[]";
			}
		}
		parent.getFontRenderer().drawSplitString(drawableText, x, y, this.width, ControlGuiPage.elementColor);
		parent.drawBox(x, y, width, height);
		if (hasFocus) {
			parent.drawBox(x - 1, y - 1, width + 2, height + 2);
		}
	}
	
	public void requestFocus() {
		hasFocus = true;
	}
	public void unFocus() {
		hasFocus = false;
	}
	
	public boolean hasFocus() {
		return hasFocus;
	}
	
	private Optional<Integer> clickToStringPos(int mouseX, int mouseY) {
		//Try to back out where the cursor should be
		List<String> lines = parent.getFontRenderer().listFormattedStringToWidth(text, this.width);
		int approxLine = (mouseY - y) / parent.getFontRenderer().FONT_HEIGHT;
		if (approxLine < 0 || approxLine >= lines.size()) {
			return Optional.empty();
		}
		String line = lines.get(approxLine);
		int lineX = 0;
		for (; lineX < line.length(); lineX++) {
			if (parent.getFontRenderer().getStringWidth(line.substring(0, lineX)) >= (mouseX - x)) {
				lineX++;
				break;
			}
		}
		lineX--;
		//Okay, cool. Now determine a string index from that
		int passedChars = 0;
		for (int i = 0; i < approxLine; i++) {
			passedChars += lines.get(i).length();
		}
		passedChars += lineX;
		if (passedChars >= 0 && passedChars < text.length()) {
			return Optional.of(passedChars);
		}
		return Optional.empty();
	}
	
	@Override
	public boolean handleClick(int mouseX, int mouseY) {
		hasFocus = isClickIn(mouseX, mouseY);
		if (hasFocus) {
			Optional<Integer> newCursorPos = clickToStringPos(mouseX, mouseY);
			if (newCursorPos.isPresent()) {
				this.cursorPos = newCursorPos.get();
			}
		}
		return hasFocus;
	}
	@Override
	public void handleKey(char typed, int keyCode) {
		if (!hasFocus) { return; }
		if (keyCode == 14) {
			//Backspace pressed
			if (cursorPos >= text.length() - 1) {
				text = text.substring(0, Math.max(0, text.length() - 1));
				cursorPos = text.length() - 1;
			}
			else {
				text = text.substring(0, cursorPos) + text.substring(Math.max(0, Math.min(cursorPos + 1, text.length() - 1)), text.length());
				cursorPos = Math.max(0, cursorPos - 1);
			}
			return;
		}
		else if (keyCode == 205) {
			cursorPos = Math.min(cursorPos + 1, text.length());
			return;
		}
		else if (keyCode == 203) {
			cursorPos = Math.max(0, cursorPos - 1);
			return;
		}
		else {
			String toInsert = "" + typed;
			if (keyCode == 28) {
				toInsert = "\n";
			}
			String result = "";
			for (int i = 0; i < text.length(); i++) {
				result += text.charAt(i);
				if (i == cursorPos + 1) {
					result += toInsert;
				}
			}
			if (cursorPos + 1 >= text.length()) {
				result += toInsert;
			}
			text = result;
		}
		cursorPos++;
	}
	
	public static class IntEntryBox extends TextEntryBox {

		public IntEntryBox(ControlGui parent, int x, int y, int width,
				int height, int defaultVal) {
			super(parent, x, y, width, height, Integer.toString(defaultVal));
		}
		public Optional<Integer> getValue() {
			try {
				return Optional.of(Integer.parseInt(this.text));
			}
			catch (NumberFormatException e) {
				return Optional.empty();
			}
		}
		public void setValue(int val) {
			this.text = "" + val;
		}
		
	}
	
}
