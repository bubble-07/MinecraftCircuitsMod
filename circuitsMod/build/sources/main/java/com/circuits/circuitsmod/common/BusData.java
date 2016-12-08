package com.circuits.circuitsmod.common;

/**
 * Represents data traveling down a bus
 * @author bubble-07
 *
 */
public class BusData {
	private final int[] allowed = {1, 2, 4, 8, 16, 32, 64};
	private final long data;
	private final int width;
	public BusData(int width, long data) {
		this.data = data;
		this.width = width;
		assert(ArrayUtils.inArray(this.width, allowed));
	}
	
	public int getWidth() {
		return width;
	}
	public long getData() {
		return data;
	}
	
	public BusData truncate(int w) {
		return new BusData(Math.min(w, this.width), data);
	}
	
	public BusData or(BusData other) {
		assert(this.width == other.width);
		return new BusData(this.width, this.data | other.data);
	}
	
	public Pair<BusData, BusData> split() {
		assert(width != 1);
		
		int halfwidth = width >> 1;
		long highbits = data >> halfwidth;
		long lowbits = data - (highbits << halfwidth);
		return Pair.of(new BusData(halfwidth, highbits), new BusData(halfwidth, lowbits));
	}
	
	public BusData combine(BusData other) {
		assert(this.width == other.width);
		assert(this.width != 64);
		return new BusData(width * 2, (this.data << this.width) + other.data);
	}
	
	public String toString() {
		return getData() + " [" + getWidth() + "-bit]";
	}
	
}
