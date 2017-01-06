package com.circuits.circuitsmod.controlblock.gui.widgets;

import java.util.List;

import com.circuits.circuitsmod.controlblock.gui.ControlGui;
import com.circuits.circuitsmod.controlblock.gui.ControlGuiPage;

/**
 * A full-page scrollable text display for the control gui
 * @author bubble-07
 *
 */
public class ScrollableTextDisplay extends UIElement {
	
	
	private static final int MAX_DESCRIP_LINES = 6;
	private static final int SCROLL_INCREMENT = 3;
	
	private int scrollY = 0;
	
	private String text;
	
	public void handleScrollUp() {
		this.scrollY = Math.max(0, this.scrollY - SCROLL_INCREMENT);
	}
	
	public void handleScrollDown() {
		this.scrollY += SCROLL_INCREMENT;
	}

	public ScrollableTextDisplay(ControlGui parent, String toDisp) {
		super(parent, ControlGuiPage.screenX, ControlGuiPage.screenY + 16, ControlGuiPage.screenWidth, ControlGuiPage.screenHeight - 2 * ControlGuiPage.shortLabelHeight);
		this.text = toDisp;
	}
	
	
	private String truncateToLines(String str, int begin, int end) {
		List<String> lines = parent.getFontRenderer().listFormattedStringToWidth(str, ControlGuiPage.screenWidth);
		String result = "";
		if (end >= lines.size()) {
			end = lines.size() - 1;
		}
		if (begin < 0) {
			begin = 0;
		}
		for (int i = begin; i <= end; i++) {
			result += lines.get(i) + "\n";
		}
		return result;
	}

	@Override
	public void draw() {
		String descrip = truncateToLines(text, scrollY, scrollY + MAX_DESCRIP_LINES);

		parent.getFontRenderer().drawSplitString(descrip, 
				ControlGuiPage.screenX, ControlGuiPage.screenY + 16, ControlGuiPage.screenWidth, ControlGuiPage.elementColor);
	}

}
