package com.circuits.circuitsmod.controlblock.frompoc;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;

import com.circuits.circuitsmod.circuit.CircuitInfo;
import com.circuits.circuitsmod.common.SerialUtils;
import com.circuits.circuitsmod.controlblock.gui.model.CircuitCell;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

//TODO: Move me to the gui.model package
public class CircuitListModel implements IMessage {
	public ArrayList<CircuitCell> items = new ArrayList<CircuitCell>();
	
	public CircuitListModel() {}
	
	
	public int numEntries() {
		return items.size();
	}
	public CircuitCell getCell(int index) {
		return items.get(index);
	}
	
	
	//Silly network code, Trix are for kids!
	//In all seriousness, this is kinda a hack
	
	@Override
	public void fromBytes(ByteBuf buf) {
		items = (ArrayList<CircuitCell>) SerialUtils.fromBytes(buf);
	}
	@Override
	public void toBytes(ByteBuf buf) {
		SerialUtils.toBytes(buf, items);
	}

}
