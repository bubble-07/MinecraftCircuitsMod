package com.circuits.circuitsmod.circuit;

import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.circuitblock.WireDirectionMapper;
import com.circuits.circuitsmod.circuitblock.WireDirectionMapper.WireDirectionGenerator;
import com.circuits.circuitsmod.common.FileUtils;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.MapUtils;
import com.circuits.circuitsmod.network.TypedMessage;
import com.circuits.circuitsmod.reflective.ChipImpl;
import com.circuits.circuitsmod.reflective.ChipInvoker;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class CircuitInfoProvider {
	
	//These will be populated on both the client and the server
	private static HashMap<CircuitUID, CircuitInfo> infoMap;
	
	//These will only be populated on the server
	private static HashMap<CircuitUID, ChipImpl> implMap;
	
	//This will only be populated on the client
    private static HashMap<CircuitUID, ResourceLocation> texMap = new HashMap<>();
    
    //Information about what circuit is in what directory is stored in the following map,
    //which is also maintained in a file in the configs directory for the whole mod,
    //which will keep track of the folder name/circuitUID associations
    private static HashMap<String, CircuitUID> folderToUIDMap = new HashMap<>();
    
    public static class ModelRequestFromClient implements Serializable {
		private static final long serialVersionUID = 1L; 
		public static void handle(ModelRequestFromClient req, World worldIn) {
			ensureServerModelInit();
			CircuitsMod.network.sendToAll(new TypedMessage(infoMap));
		}
    }
    
    public static class ModelResponseFromServer implements Serializable {
		private static final long serialVersionUID = 1L;
		private HashMap<CircuitUID, CircuitInfo> infoMap;
    	public ModelResponseFromServer(HashMap<CircuitUID, CircuitInfo> infoMap) {
    		this.infoMap = infoMap;
    	}
		public static void handle(ModelResponseFromServer response) {
			CircuitInfoProvider.infoMap = response.infoMap;
		}
    }
    
    private static File getUIDMapFile() {
    	return new File(FileUtils.getConfigRootDir().toString() + "/uidmap");
    }
    
    public static void loadUIDMapFromFile() {
    	Optional<Object> uidMap = FileUtils.objectFromFile(getUIDMapFile());
    	if (uidMap.isPresent()) {
    		folderToUIDMap = (HashMap<String, CircuitUID>) uidMap.get();
    	}
    	//Otherwise, the file must not yet exist or something, so we just keep things the way they are,
    	//since this must be the first run (in a non-error state) or something sketchy (in an error state)
    }
    
    public static void saveUIDMapToFile() {
    	FileUtils.objectToFile(getUIDMapFile(), folderToUIDMap);
    }

	public static void ensureClientModelInit() {
    	CircuitsMod.network.sendToServer(new TypedMessage(new ModelRequestFromClient()));
	}
	
	public static boolean isClientModelInit() {
		return infoMap != null;
	}
	
	private static CircuitUID getUIDForDir(File dir) {
		CircuitUID result = folderToUIDMap.get(dir.getName());
		if (result == null) {
			//In this case, we need to generate a new UID for the given folder name!
			result = CircuitUID.getNextUID();
			folderToUIDMap.put(dir.getName(), result);
		}
		return result;
	}
    
	public static void ensureServerModelInit() { 
		if (implMap != null) {
			return;
		}
		File circuitsDir = FileUtils.getCircuitDefinitionsDir();
		
		if (!circuitsDir.exists()) {
			circuitsDir.mkdirs();
		}
		
		implMap = new HashMap<>();
		infoMap = new HashMap<>();
		
		for (File subDir : circuitsDir.listFiles()) {
			if (!subDir.getName().startsWith(".")) {
				
				CircuitUID uid = getUIDForDir(subDir);
				Optional<CircuitInfo> entry = CircuitInfo.fromFolder(subDir);
				Optional<ChipImpl> impl = ChipImpl.fromCircuitDirectory(subDir);
				if (!entry.isPresent() || !impl.isPresent()) {
					Log.userError("Circuit in directory " + subDir + " is either formatted incorrectly, or is underspecified!");
					continue;
				}
				infoMap.put(uid, entry.get());
				implMap.put(uid, impl.get());
			}
		}
	}
	
	public static boolean isServerModelInit() {
		return implMap != null;
	}
	
	public static ResourceLocation getTexture(CircuitUID uid) {
    	if (texMap.containsKey(uid)) {
    		return texMap.get(uid);
    	}
    	if (!isClientModelInit()) {
    		ensureClientModelInit();
    		return null;
    	}
		
		CircuitInfo info = infoMap.get(uid);
		
	   	TextureManager texMan = Minecraft.getMinecraft().getTextureManager();
		ResourceLocation loc = texMan.getDynamicTextureLocation(uid.toString(), new DynamicTexture(info.getImage()));
		texMap.put(uid, loc);
		return loc;
	}
	
	public static ChipInvoker getInvoker(CircuitUID uid) {
		return implMap.get(uid).getInvoker();
	}
	public static WireDirectionGenerator getWireDirectionGenerator(CircuitUID uid) {
		return infoMap.get(uid).getWireDirectionGenerator();
	}
	
	public static String getDisplayName(CircuitUID uid) {
		return infoMap.get(uid).getName();
	}
}
