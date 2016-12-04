package com.circuits.circuitsmod.controlblock.tester.net;

import java.io.Serializable;
import java.util.Optional;
import java.util.UUID;

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
	UUID player;
	
	SpecializedCircuitUID circuitUID;
	
	public CraftingRequest(UUID player, BlockPos pos, int numCrafted, SpecializedCircuitUID uid) {
		this.player = player;
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
		
		//The server should never have the crafting cell set for too long
		entity.get().setCraftingCell(in.player, in.circuitUID);
		entity.get().craftingSlotPickedUp(in.numCrafted);
		entity.get().unsetCraftingCell();
	}
	
	public static class Message implements IMessage {
		public CraftingRequest message = null;
		public Message() { }
		public Message(UUID player, BlockPos pos, int numCrafted, SpecializedCircuitUID circuitUid) {
			message = new CraftingRequest(player, pos, numCrafted, circuitUid);
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
