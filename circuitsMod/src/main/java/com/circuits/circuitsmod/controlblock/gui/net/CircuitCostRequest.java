package com.circuits.circuitsmod.controlblock.gui.net;

import io.netty.buffer.ByteBuf;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.CircuitUID;
import com.circuits.circuitsmod.circuit.SpecializedCircuitInfo;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.SerialUtils;
import com.circuits.circuitsmod.controlblock.ControlBlock;
import com.circuits.circuitsmod.controlblock.ControlTileEntity;
import com.circuits.circuitsmod.recipes.RecipeUtils;

//Request from the client to the server to send the cost of a circuit
public class CircuitCostRequest implements Serializable {
	private static final long serialVersionUID = 1L;
	UUID player;
	CircuitUID uid;
	private Long pos;
	public CircuitCostRequest(UUID player, BlockPos pos, CircuitUID uid) {
		this.pos = pos.toLong();
		this.player = player;
		this.uid = uid;
	}
	public BlockPos getPos() {
		return BlockPos.fromLong(pos);
	}
	public CircuitUID getUID() {
		return uid;
	}
	
	public static void handleCircuitCostRequest(CircuitCostRequest in, World worldIn) {
		Optional<ControlTileEntity> entity = ControlBlock.getControlTileEntityAt(worldIn, in.getPos());
		if (!entity.isPresent()) {
			Log.internalError("Attempting to get circuit cost for " + in.getPos() +  " but no control TE present!");
			return;
		}
		Optional<List<ItemStack>> stack = RecipeUtils.getRecipeFor(worldIn, in.player, in.uid);
		
		CircuitCosts costs = new CircuitCosts(stack);
		
		entity.get().postGuiMessage(in.player, 
				new ServerGuiMessage(ServerGuiMessage.GuiMessageKind.GUI_CIRCUIT_COSTS, costs));
	}
	
	public static class Message implements IMessage {
		public CircuitCostRequest message = null;
		public Message() { }
		public Message(UUID player, BlockPos pos, CircuitUID uid) {
			message = new CircuitCostRequest(player, pos, uid);
		}
		@Override
		public void fromBytes(ByteBuf in) {
			message = (CircuitCostRequest) SerialUtils.fromBytes(in);
		}
		public void toBytes(ByteBuf in) {
			SerialUtils.toBytes(in, message);
		}
	}
}
