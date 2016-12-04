package com.circuits.circuitsmod.controlblock.gui.model;

import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.circuits.circuitsmod.circuit.CircuitInfo;
import com.circuits.circuitsmod.circuit.CircuitUID;
import com.circuits.circuitsmod.common.SerialUtils;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

//TODO: Move me to the gui.model package
public class CircuitListModel implements IMessage {
	private ArrayList<CircuitCell> items = new ArrayList<CircuitCell>();
	
	public CircuitListModel(Map<CircuitUID, CircuitInfo> infoMap) {
		for (Map.Entry<CircuitUID, CircuitInfo> entry : infoMap.entrySet()) {
			CircuitUID uid = entry.getKey();
			CircuitInfo val = entry.getValue();
			items.add(new CircuitCell(uid, val));
		}
		//TODO: Should the GUI support custom sorting?
		items.sort((c1, c2) -> c1.getName().compareTo(c2.getName()));
	}
	
	
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
