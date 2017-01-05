package com.circuits.circuitsmod.controlblock.tester.net;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.circuits.circuitsmod.common.FileUtils;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.compiler.CompilationUtils;
import com.circuits.circuitsmod.controlblock.ControlTileEntity;
import com.circuits.circuitsmod.controlblock.gui.model.CustomCircuitInfo;
import com.circuits.circuitsmod.controlblock.gui.net.ServerGuiMessage;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CompileRecordingRequest extends ControlTileEntityClientRequest {
	private static final long serialVersionUID = 1L;
	
	private CustomCircuitInfo info;

	public CompileRecordingRequest(UUID playerId, BlockPos pos, CustomCircuitInfo info) {
		super(playerId, pos);
		this.info = info;
	}
	
	public static void notifyFailure(UUID playerId, ControlTileEntity entity) {
		entity.postGuiMessage(playerId, 
				new ServerGuiMessage(ServerGuiMessage.GuiMessageKind.GUI_COMPILATION_RESULT, 
						             new ServerGuiMessage.CompilationResult(false)));
	}
	
	public static void handle(CompileRecordingRequest in, World worldIn) {
		in.performOnControlTE(worldIn, (entity) -> {

			//First, get or create the player-specific custom circuits directory
			List<EntityPlayer> optPlayer = worldIn.getEntities(EntityPlayer.class, (e) -> e.getUniqueID().equals(in.getPlayerID()));
			if (optPlayer.size() != 1) {
				Log.internalError("Somehow, there isn't exactly one player with UUID " + in.getPlayerID());
				notifyFailure(in.getPlayerID(), entity);
				return;
			}
			String playerName = optPlayer.get(0).getName();
			
			File userDir = new File(FileUtils.getWorldCustomCircuitsDir(), playerName);
			if (!userDir.exists()) {
				userDir.mkdirs();
			}
			Optional<File> writtenDirectory = in.info.writeIntoDirectory(userDir);
			if (!writtenDirectory.isPresent()) {
				notifyFailure(in.getPlayerID(), entity);
				return;
			}
			
			CompilationUtils.compileJavaImplementationInDir(writtenDirectory.get(), entity, in.getPlayerID());
		});
	}

}
