package com.circuits.circuitsmod.network;

import java.io.Serializable;

import com.circuits.circuitsmod.common.SerialUtils;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * A generic implementation of IMessage for Minecraft network communication
 * which automagically communicates serializable objects over the channel
 * @author bubble-07
 *
 */
public class TypedMessage implements IMessage {
	TaggedObject<?> wrappedObject;
	
	private static class TaggedObject<T extends Serializable> implements Serializable {
		private static final long serialVersionUID = 1L;
		T wrapped;
		Class<T> clazz;
		@SuppressWarnings("unchecked")
		public TaggedObject(T val) {
			this.wrapped = val;
			this.clazz = (Class<T>) val.getClass();
		}
	}
	
	public TypedMessage () { }
	
	public <T extends Serializable> TypedMessage(T object) {
		wrappedObject = new TaggedObject<T>(object);
	}
	
	@Override
	public void fromBytes(ByteBuf in) {
		wrappedObject = (TaggedObject<?>) SerialUtils.fromBytes(in);
	}
	public void toBytes(ByteBuf in) {
		SerialUtils.toBytes(in, wrappedObject);
	}
	
	public TaggedObject<?> asTaggedObject() {
		return this.wrappedObject;
	}
	
	public Class<?> getWrappedClass() {
		return wrappedObject.clazz;
	}
	public Object getWrappedObject() {
		return wrappedObject.wrapped;
	}

}
