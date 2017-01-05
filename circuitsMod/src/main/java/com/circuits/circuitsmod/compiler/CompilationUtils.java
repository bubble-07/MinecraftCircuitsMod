package com.circuits.circuitsmod.compiler;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import com.circuits.circuitsmod.TickEvents;
import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.controlblock.ControlTileEntity;
import com.circuits.circuitsmod.controlblock.gui.net.ServerGuiMessage;

public class CompilationUtils {

	//TODO: Do we want to auto-compile .java files on server start-up, for convenience?
	//or is the utility of this method limited to here?
	public static Optional<File> compileJavaImplementationInDir(File circuitDir, ControlTileEntity origin, UUID player) {
		File javaFile = new File(circuitDir, "Implementation.java");
		if (!javaFile.exists()) {
			return Optional.empty();
		}
		
		File javaBinDirectory = new File(new File(System.getProperty("java.home")), "bin");
		File javaBin;
		
		if (System.getProperty("os.name").startsWith("Win")) {
		    javaBin = new File(javaBinDirectory, "java.exe");
		} else {
			javaBin = new File(javaBinDirectory, "java");
		}
		
		String[] cmd = new String[]{javaBin.getAbsolutePath(), "Implementation.java"};
		
		try {
			Process compilationProc = Runtime.getRuntime().exec(cmd, null, circuitDir);
			CompilationUtils.compilationCheckAndRefresh(compilationProc, circuitDir, origin, player);
		}
		catch (IOException e) {
			Log.internalError("Failed to compile circuit in " + circuitDir);
			return Optional.empty();
		}
		
		return Optional.of(circuitDir);
	}

	public static Runnable compilationCheckAndRefresh(Process compilationProc, File circuitDir, ControlTileEntity origin, UUID player) {
		return () -> {
			if (compilationProc.isAlive()) {
				TickEvents.instance().addDelayedAction(compilationCheckAndRefresh(compilationProc, circuitDir, origin, player));
			}
			else {
				File implClass = new File(circuitDir, "Implementation.class");
				if (implClass.exists()) {
					//Successful compilation! Refresh things!
					TickEvents.instance().addImmediateAction(CircuitInfoProvider::refreshServerInfoAndSendToClient);
				}
				//In any case, return the result to the waiting Control GUI
				origin.postGuiMessage(player, 
						new ServerGuiMessage(ServerGuiMessage.GuiMessageKind.GUI_COMPILATION_RESULT, 
								             new ServerGuiMessage.CompilationResult(implClass.exists())));
			}
		};
	}

}
