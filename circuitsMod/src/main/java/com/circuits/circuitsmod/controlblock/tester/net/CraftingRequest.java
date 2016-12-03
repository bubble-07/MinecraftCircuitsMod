package com.circuits.circuitsmod.controlblock.tester.net;

import java.io.Serializable;
import java.util.Optional;

import com.circuits.circuitsmod.circuit.CircuitInfo;
import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.SpecializedCircuitInfo;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.SerialUtils;
import com.circuits.circuitsmod.controlblock.ControlBlock;
import com.circuits.circuitsmod.controlblock.frompoc.ControlTileEntity;
import com.circuits.circuitsmod.controlblock.frompoc.Microchips;

import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

//Request to be used when client requests a circuit be crafted
public class CraftingRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	private Long pos;
	int numCrafted;
	
	SpecializedCircuitUID circuitUID;
	
	public CraftingRequest(BlockPos pos, int numCrafted, SpecializedCircuitUID uid) {
		this.pos = pos.toLong();
		this.numCrafted = numCrafted;
		this.circuitUID = uid;
	}
	
	public BlockPos getPos() {
		return BlockPos.fromLong(pos);
	}
	
	public static void handleCraftingRequest(CraftingRequest in, World worldIn) {
		
		Optional<ControlTileEntity> entity = ControlBlock.getControlTileEntityAt(worldIn, in.getPos());
		
		if (!entity.isPresent()) {
			Log.internalError("Crafting request failed. No control TE at " + in.getPos());
		}
		//Handling the crafting request happens on the server, so we're perfectly justified in just looking
		//at specialized circuit info without needing to wait on some kind of initialization/response
		Optional<SpecializedCircuitInfo> circuit = CircuitInfoProvider.getSpecializedInfoFor(in.circuitUID);

		if (!circuit.isPresent()) {
			Log.internalError("No circuit present on crafting request: " + in.circuitUID + " in " + Microchips.mainModel.items.toString());
			return;
		}
		
		//The server should never have the crafting cell set for too long
		entity.get().setCraftingCell(circuit.get());
		entity.get().craftingSlotPickedUp(in.numCrafted);
		entity.get().setCraftingCell(null);
	}
	
	public static class Message implements IMessage {
		public CraftingRequest message = null;
		public Message() { }
		public Message(BlockPos pos, int numCrafted, SpecializedCircuitUID circuitUid) {
			message = new CraftingRequest(pos, numCrafted, circuitUid);
		}
		@Override
		public void fromBytes(ByteBuf in) {
			message = (CraftingRequest) SerialUtils.fromBytes(in);
		}
		public void toBytes(ByteBuf in) {
			SerialUtils.toBytes(in, message);
		}
	}
}
