package com.circuits.circuitsmod.common;

import io.netty.buffer.ByteBuf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SerialUtils {
	public static Object fromByteArray(byte[] bytes) {
		Object ret = null;
		try {
			ByteArrayInputStream instream = new ByteArrayInputStream(bytes);
			ObjectInputStream objstream = new ObjectInputStream(instream);
			ret = objstream.readObject();
			instream.close();
			objstream.close();
		}
		catch (Exception e) { System.err.println(e);}
		return ret;
	}
	public static Object fromBytes(ByteBuf buf) {
		byte[] bytes = new byte[buf.readableBytes()];
		buf.readBytes(bytes);
		return fromByteArray(bytes);
	}
	
	public static byte[] toByteArray(Object o) {
		try {
			ByteArrayOutputStream outstream = new ByteArrayOutputStream();
			ObjectOutputStream objstream = new ObjectOutputStream(outstream);
			objstream.writeObject(o);
			objstream.close();
			outstream.close();
			return outstream.toByteArray();
		}
		catch (Exception e) { System.err.println(e); }
		return null;
	}
	public static void toBytes(ByteBuf buf, Object o) {
		buf.writeBytes(toByteArray(o));
	}
}
