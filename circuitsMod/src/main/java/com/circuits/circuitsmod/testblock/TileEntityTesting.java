package com.circuits.circuitsmod.testblock;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

import com.circuits.circuitsmod.reflective.TestGeneratorInvoker;

import net.minecraft.nbt.*;

@SuppressWarnings("unused")
public class TileEntityTesting extends TileEntity implements ITickable {
	
	private TestGeneratorInvoker testInvoker;
	
	private NBTTagString andTag = new NBTTagString("AND");
	private NBTTagString orTag = new NBTTagString("OR");
	private NBTTagString xorTag = new NBTTagString("XOR");
	private NBTTagString demuxTag = new NBTTagString("DEMUX");
	private NBTTagString muxTag = new NBTTagString("MUX");
	private NBTTagString adcTag = new NBTTagString("ADC");
	private NBTTagString fullAddTag = new NBTTagString("FULLADD");
	private NBTTagString halfAddTag = new NBTTagString("HALFADD");
	private NBTTagString clockTag = new NBTTagString("CLOCK");
	private NBTTagString dacTag = new NBTTagString("DAC");
	private NBTTagString notTag = new NBTTagString("NOT");
	private NBTTagString impliesTag = new NBTTagString("IMPLIES");
	private NBTTagString dlatchTag = new NBTTagString("DLATCH");
	private NBTTagString nandTag = new NBTTagString("NAND");
	private NBTTagString norTag = new NBTTagString("NOR");
	private NBTTagString pulseLengthenTag = new NBTTagString("PULSELENGTHEN");
	private NBTTagString risingTag = new NBTTagString("RISINGEDGEDETECTOR");
	private NBTTagString abbaTag = new NBTTagString("ABBA");
			
	@Override
	public void update() {
		//determine the circuit under test 
		//then invoke the test method on it.
	}
	
	//the method that will test a circuit and return success or failure.
	public boolean test() {
		return true;
	}
}
