package com.circuits.circuitsmod.compiler;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;
import java.util.UUID;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

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
		
		JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
		if (compiler != null) {
			int code = compiler.run(null, null, null, javaFile.getPath());
			if (code != 0) {
				Log.internalError("Failed to compile circuit in " + circuitDir);
			}
		}
		else {
			Log.internalError("Are you sure the JDK is installed?");
		}
		
		File implClass = new File(circuitDir, "Implementation.class");
		if (implClass.exists()) {
			//Successful compilation! Refresh things!
			CircuitInfoProvider.refreshServerInfoAndSendToClient();
		}
		//In any case, return the result to the waiting Control GUI
		origin.postGuiMessage(player, 
				new ServerGuiMessage(ServerGuiMessage.GuiMessageKind.GUI_COMPILATION_RESULT, 
						             new ServerGuiMessage.CompilationResult(implClass.exists())));
		
		
		return implClass.exists() ? Optional.of(circuitDir) : Optional.empty();
	}

}
