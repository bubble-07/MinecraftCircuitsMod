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
	
	public void fillRect(int x1, int y1, int x2, int y2, Color val) {
		x1 = Math.min(x1, x2);
		x2 = Math.max(x1, x2);
		y1 = Math.min(y1, y2);
		y2 = Math.max(y1, y2);
		for (int x = x1; x <= x2; x++) {
			for (int y = y1; y <= y2; y++) {
				setPixel(x, y, val);
			}
		}
	}
	
	public static int toGUIColor(Color color) {
		return color.equals(Color.BLACK) ? 0 : -1;

	}
	
	public void drawInGUI(int startX, int startY, int width, int height) {
		int pixWidth = width / 16;
		int pixHeight = height / 16;
		
		for (int i = 0; i < 16; i++) {
			for (int j = 0; j < 16; j++) {
				int colorToDraw = toGUIColor(getPixel(i, j));
				Gui.drawRect(startX + pixWidth * i, startY + pixHeight * j, 
						        startX + pixWidth * (i + 1), startY + pixHeight * (j + 1), colorToDraw);
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
