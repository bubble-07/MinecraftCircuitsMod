package com.circuits.circuitsmod.tester;

import java.util.Optional;

import net.minecraft.block.Block;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;

import com.circuits.circuitsmod.circuit.SpecializedCircuitInfo;
import com.circuits.circuitsmod.frameblock.StartupCommonFrame;
import com.circuits.circuitsmod.testblock.TileEntityTesting;

public class PuzzleBlockTester extends Tester<TileEntityTesting> {
	
	private static double SEARCH_WIDTH = 64;
	private static double SEARCH_HEIGHT = 4;
	
	public PuzzleBlockTester(TileEntityTesting parent, SpecializedCircuitInfo circuit, TestConfig config) {
		super(null, parent, circuit, config);
	}
	@Override
	public void successAction() {
		parent.spawnTeleCleaner();
	}
	@Override
	public void stateUpdateAction() {
		
	}
	@Override
	public Optional<AxisAlignedBB> getTestingBox() {
		AxisAlignedBB result = new AxisAlignedBB(parent.getPos(), parent.getPos());
		result = result.expand(SEARCH_WIDTH, SEARCH_HEIGHT, SEARCH_WIDTH);
		return Optional.of(result);
	}
	@Override
	public void failureAction() {
		MinecraftServer server = parent.getWorld().getMinecraftServer();
		PlayerList list = server.getPlayerList();
		list.sendChatMsg(new TextComponentTranslation("Test Failed!"));
		
	}
}