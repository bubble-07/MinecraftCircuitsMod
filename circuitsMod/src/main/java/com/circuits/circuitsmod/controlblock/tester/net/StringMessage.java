package com.circuits.circuitsmod.controlblock.tester.net;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

public class StringMessage implements IMessage {
	public String text;
	
	public StringMessage() { }
	public StringMessage(String text) {
		this.text = text;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		text = ByteBufUtils.readUTF8String(buf);
	}
	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeUTF8String(buf, text);
	}
}
