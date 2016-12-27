package com.circuits.circuitsmod.common;

import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.collect.Lists;

/**
 * Represents data traveling down a bus
 * @author bubble-07
 *
 */
public class BusData implements Serializable {
	private static final long serialVersionUID = 1L;
	private static final int[] allowed = {1, 2, 4, 8, 16, 32, 64};
	
	public static HashMap<Integer, Long> maskMap = new HashMap<Integer, Long>();
	static {
		addMasks();
	}
	
	private static void addMasks() {
		maskMap.put(1, (long) 0b0000000000000000000000000000000000000000000000000000000000000001);
		maskMap.put(2, (long) 0b0000000000000000000000000000000000000000000000000000000000000011);
		maskMap.put(4, (long) 0b0000000000000000000000000000000000000000000000000000000000001111);
		maskMap.put(8, (long) 0b0000000000000000000000000000000000000000000000000000000011111111);
		maskMap.put(16, (long) 0b0000000000000000000000000000000000000000000000001111111111111111);
		maskMap.put(32, (long) 0b0000000000000000000000000000000011111111111111111111111111111111);
		maskMap.put(64, (long) ~0);
	}
	
	
	private final long data;
	private final int width;
	//private HashMap<Integer, Long> maskMap;
	
	public BusData(int width, long data) {
		this.width = width;
		assert(com.circuits.circuitsmod.common.ArrayUtils.inArray(this.width, allowed));
		
		this.data = maskMap.get(width) & data;

	}
	
	public BusData copy() {
		return new BusData(width, data);
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
	
	public byte[] toBytes() {
		return ByteBuffer.allocate(12).putInt(width).putLong(data).array();
	}
	public static Optional<BusData> fromBytes(byte[] bytes) {
		if (bytes.length != 12) {
			return Optional.empty();
		}
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		int width = buf.getInt();
		long data = buf.getLong();
		return Optional.of(new BusData(width, data));
	}
	
	public static Optional<List<BusData>> listFromBytes(byte[] bytes) {
		if (bytes.length % 12 != 0) {
			return Optional.empty();
		}
		List<byte[]> chunked = Lists.newArrayList();
		ByteBuffer buf = ByteBuffer.wrap(bytes);
		while (buf.hasRemaining()) {
			byte[] toAdd = new byte[12];
			buf = buf.get(toAdd);
			chunked.add(toAdd);
		}
		return Optional.of(chunked.stream().map(BusData::fromBytes).map(Optional::get).collect(Collectors.toList()));
	}
	
	public static byte[] listToBytes(List<BusData> in) {
		Byte[] result = in.stream().flatMap(b -> Stream.of(ArrayUtils.toObject(b.toBytes()))).toArray(Byte[]::new);
		return ArrayUtils.toPrimitive(result);
	}
	
	public String toString() {
		return getData() + " [" + getWidth() + "-bit]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (data ^ (data >>> 32));
		result = prime * result + width;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BusData other = (BusData) obj;
		if (data != other.data)
			return false;
		if (width != other.width)
			return false;
		return true;
	}
	
	
}
