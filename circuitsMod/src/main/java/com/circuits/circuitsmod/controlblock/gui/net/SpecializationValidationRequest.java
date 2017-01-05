package com.circuits.circuitsmod.controlblock.gui.net;

import java.util.Optional;
import java.util.UUID;

import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.SpecializedCircuitInfo;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.controlblock.tester.net.ControlTileEntityClientRequest;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpecializationValidationRequest extends ControlTileEntityClientRequest {
	private static final long serialVersionUID = 1L;
	SpecializedCircuitUID uid;
	public SpecializationValidationRequest(UUID player, BlockPos pos, SpecializedCircuitUID uid) {
		super(player, pos);
		this.uid = uid;
	}
	public SpecializedCircuitUID getUID() {
		return uid;
	}
	public static void handle(SpecializationValidationRequest in, World worldIn) {
		in.performOnControlTE(worldIn, (entity) -> {
			Optional<SpecializedCircuitInfo> info = CircuitInfoProvider.getSpecializedInfoFor(in.uid);
			Boolean isSlowable = info.map((i) -> i.isTestSlowable()).orElse(true);
			String specialName = info.flatMap((i) -> Optional.of(i.getFullDisplayName())).orElse(null);
			entity.postGuiMessage(in.getPlayerID(), 
					new ServerGuiMessage(ServerGuiMessage.GuiMessageKind.GUI_SPECIALIZATON_INFO, new ServerGuiMessage.SpecializationInfo(specialName, isSlowable)));
		});
	}

}
