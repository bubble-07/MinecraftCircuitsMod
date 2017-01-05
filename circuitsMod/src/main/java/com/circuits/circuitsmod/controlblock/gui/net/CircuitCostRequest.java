package com.circuits.circuitsmod.controlblock.gui.net;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import com.circuits.circuitsmod.circuit.CircuitUID;
import com.circuits.circuitsmod.controlblock.tester.net.ControlTileEntityClientRequest;
import com.circuits.circuitsmod.recipes.RecipeDeterminer;

//Request from the client to the server to send the cost of a circuit
public class CircuitCostRequest extends ControlTileEntityClientRequest {
	private static final long serialVersionUID = 1L;
	CircuitUID uid;
	public CircuitCostRequest(UUID player, BlockPos pos, CircuitUID uid) {
		super(player, pos);
		this.uid = uid;
	}
	public CircuitUID getUID() {
		return uid;
	}
	
	public static void handle(CircuitCostRequest in, World worldIn) {
		in.performOnControlTE(worldIn, (entity) -> {
			Optional<List<ItemStack>> stack = RecipeDeterminer.getRecipeFor(worldIn, in.getPlayerID(), in.uid);
			
			CircuitCosts costs = new CircuitCosts(stack);
			
			entity.postGuiMessage(in.getPlayerID(), 
					new ServerGuiMessage(ServerGuiMessage.GuiMessageKind.GUI_CIRCUIT_COSTS, costs));
		});
		
	}
}
