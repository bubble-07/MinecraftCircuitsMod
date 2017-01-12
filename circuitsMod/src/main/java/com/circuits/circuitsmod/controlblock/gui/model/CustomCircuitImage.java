package com.circuits.circuitsmod.controlblock.gui.model;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.imageio.ImageIO;

import net.minecraft.client.gui.Gui;

import com.circuits.circuitsmod.common.Pair;

public class CustomCircuitImage implements Serializable {
	private static final long serialVersionUID = 1L;
	private BufferedImage image;
	
	public CustomCircuitImage() {
		image = new BufferedImage(16, 16, BufferedImage.TYPE_BYTE_BINARY);
		fillRect(0, 0, 15, 15, Color.WHITE);
	}
	
	public BufferedImage toBufferedImage() {
		return this.image;
	}
	
	public static enum Color {
		WHITE,
		BLACK
	};
	
	/**
	 * Sets the pixel at the given x, y position (if possible).
	 * @param x
	 * @param y
	 * @param val
	 */
	public void setPixel(int x, int y, Color val) {
		if (boundsCheck(x, y)) {
			if (val.equals(Color.BLACK)) {
				image.setRGB(x, y, 0);
			}
			else {
				image.setRGB(x, y, -1);
			}
		}
	}
	
	public static boolean boundsCheck(int x, int y) {
		return x >= 0 && x < 16 && y >= 0 && y < 16;
	}
	
	public Color getPixel(int x, int y) {
		if (boundsCheck(x, y)) {
			if (image.getRGB(x, y) == -16777216) {
				return Color.BLACK;
			}
		}
		return Color.WHITE;
	}
	
	/**
	 * Implementation of Bresenham's line algorithm for the vertical-ish case
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @param val
	 */
	private void drawLineVert(int x1, int x2, int y1, int y2, Color val,
			                  boolean incrX, boolean incrY) {
		float dx = x2 - x1;
		float dy = y2 - y1;
		float derr = Math.abs(((float) dx) / ((float) dy));
		float error = derr - 0.5f;
		int x = incrX ? x1 : x2;
		for (int y = incrY ? y1 : y2; y != (incrY ? y2 + 1 : y1 - 1); y += (incrY ? 1 : -1)) {
			setPixel(x, y, val);
			error += derr;
			if (error >= 0.5f) {
				x += incrX ? 1 : -1;
				error -= 1.0f;
			}
		}
	}
	/**
	 * Implementation of Bresenham's line algorithm for the horizontal-ish case
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @param val
	 */
	private void drawLineHoriz(int x1, int x2, int y1, int y2, Color val,
			                   boolean incrX, boolean incrY) {
		float dx = x2 - x1;
		float dy = y2 - y1;
		float derr = Math.abs(((float) dy) / ((float) dx));
		float error = derr - 0.5f;
		int y = incrY ? y1 : y2;
		for (int x = incrX ? x1 : x2; x != (incrX ? x2 + 1 : x1 - 1); x += (incrX ? 1 : -1)) {
			setPixel(x, y, val);
			error += derr;
			if (error >= 0.5f) {
				y += incrY ? 1 : -1;
				error -= 1.0f;
			}
		}
	}
	
	public void drawLine(int x10, int y10, int x20, int y20, Color val) {
		int x1 = Math.min(x10, x20);
		int x2 = Math.max(x10, x20);
		int y1 = Math.min(y10, y20);
		int y2 = Math.max(y10, y20);
		boolean incrX = (x20 - x10) > 0;
		boolean incrY = (y20 - y10) > 0;
		if (x2 - x1 > y2 - y1) {
			drawLineHoriz(x1, x2, y1, y2, val, incrX, incrY);
		}
		else if (y2 - y1 >= x2 - x1 && y2 - y1 > 0) {
			drawLineVert(x1, x2, y1, y2, val, incrX, incrY);
		}
		else {
			setPixel(x1, x2, val);
		}
	}
	
	public void fillRect(int x10, int y10, int x20, int y20, Color val) {
		int x1 = Math.min(x10, x20);
		int x2 = Math.max(x10, x20);
		int y1 = Math.min(y10, y20);
		int y2 = Math.max(y10, y20);
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				setPixel(x, y, val);
			}
		}
	}
	
	public static int toGUIColor(Color color) {
		return color.equals(Color.BLACK) ? 0 : -1;

	}
	
	public void drawInGUI(int startX, int startY, int width, int height, int hoverX, int hoverY) {
		int pixWidth = width / 16;
		int pixHeight = height / 16;
		
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				int colorToDraw = toGUIColor(getPixel(i, j));
				
				int left = startX + pixWidth * i;
				int right = startX + pixWidth * (i + 1);
				int top = startY + pixHeight * j;
				int bottom = startY + pixHeight * (j + 1);
				
				if (i == hoverX && j == hoverY) {
					Gui.drawRect(left, top, right, bottom, java.awt.Color.GREEN.getRGB());
					Gui.drawRect(left + 1, top + 1, right - 1, bottom - 1, colorToDraw);
					//Draw a green border around the pixel
				}
				else {
					//Just draw the pixel
					Gui.drawRect(left, top, right, bottom, colorToDraw);
				}
			}
		}
	}
	
	public Pair<Integer, Integer> fromGUICoords(int startX, int startY, int width, int height, int clickX, int clickY) {
		int pixWidth = width / 16;
		int pixHeight = height / 16;
		int pixX = (clickX - startX) / pixWidth;
		int pixY = (clickY - startY) / pixHeight;
		return Pair.of(pixX, pixY);
	}
	
	private void writeObject(ObjectOutputStream out) throws IOException {
		if (image != null) {
			ImageIO.write(image, "png", out);		
		}
	}
	
	private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
		try {
			image = ImageIO.read(in);
		}
		catch (IOException e) {
			image = null;
		}
	}
}
