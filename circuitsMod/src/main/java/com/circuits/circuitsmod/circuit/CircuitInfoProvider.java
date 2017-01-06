package com.circuits.circuitsmod.circuit;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.TickEvents;
import com.circuits.circuitsmod.circuitblock.WireDirectionMapper.WireDirectionGenerator;
import com.circuits.circuitsmod.common.FileUtils;
import com.circuits.circuitsmod.common.Log;
import com.circuits.circuitsmod.common.Pair;
import com.circuits.circuitsmod.common.StringUtils;
import com.circuits.circuitsmod.controlblock.gui.model.CircuitCell;
import com.circuits.circuitsmod.controlblock.gui.model.CircuitDirectory;
import com.circuits.circuitsmod.controlblock.gui.model.CircuitTreeModel;
import com.circuits.circuitsmod.network.TypedMessage;
import com.circuits.circuitsmod.reflective.ChipImpl;
import com.circuits.circuitsmod.reflective.ChipInvoker;
import com.circuits.circuitsmod.reflective.SpecializedChipImpl;
import com.google.common.collect.Lists;
import com.google.gson.Gson;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class CircuitInfoProvider {
	
	//These will be populated on both the client and the server
	private static HashMap<CircuitUID, CircuitInfo> infoMap;
	private static CircuitTreeModel circuitTree;
	
	//These will only be populated on the server
	private static HashMap<CircuitUID, ChipImpl> implMap;
	
	//This will only be populated on the client
    private static HashMap<CircuitUID, ResourceLocation> texMap = new HashMap<>();    
    private static HashMap<SpecializedCircuitUID, SpecializedCircuitInfo> infoCache = new HashMap<>();
    //TODO: Make the above intelligently forget older entries! Maybe a WeakHashMap?

    
    //Information about what circuit is in what directory is stored in the following map,
    //which is also maintained in a file in the configs directory for the whole mod,
    //which will keep track of the folder name/circuitUID associations
    private static HashMap<String, CircuitUID> folderToUIDMap;
    
    public static void clearState() {
    	infoCache = new HashMap<>();
    	texMap = new HashMap<>();
    	infoMap = null;
    	circuitTree = null;
    	folderToUIDMap = null;
    	implMap = null;
    	CircuitUID.clearState();
    }
    
    
    public static class SpecializedInfoRequestFromClient implements Serializable {
		private static final long serialVersionUID = 1L; 
		private final SpecializedCircuitUID uid;
		
		public SpecializedInfoRequestFromClient(SpecializedCircuitUID uid) {
			this.uid = uid;
		}
		
		public static void handle(SpecializedInfoRequestFromClient req, World worldIn) {
			ensureServerModelInit();
			createSpecializedInfoFor(req.uid);
			Pair<SpecializedCircuitUID, SpecializedCircuitInfo> infoToSend = Pair.of(req.uid, infoCache.get(req.uid));
			CircuitsMod.network.sendToAll(new TypedMessage(new SpecializedInfoResponseFromServer(infoToSend)));
		}
    }
    
    public static class SpecializedInfoResponseFromServer implements Serializable {
		private static final long serialVersionUID = 1L;
		private final Pair<SpecializedCircuitUID, SpecializedCircuitInfo> mapping;
		
		public SpecializedInfoResponseFromServer(Pair<SpecializedCircuitUID, SpecializedCircuitInfo> mapping) {
			this.mapping = mapping;
		}
		
		public static void handle(SpecializedInfoResponseFromServer req, World worldIn) {
			CircuitInfoProvider.infoCache.put(req.mapping.first(), req.mapping.second());
		}
    	
    }
    
    public static class ClientModelContainer implements Serializable {
		private static final long serialVersionUID = 1L;
		
		public HashMap<CircuitUID, CircuitInfo> infoMap;
    	public CircuitTreeModel circuitTree;
    	
    	public ClientModelContainer(HashMap<CircuitUID, CircuitInfo> infoMap, CircuitTreeModel circuitTree) {
    		this.infoMap = infoMap;
    		this.circuitTree = circuitTree;
    	}
    }
    
    public static class ModelRequestFromClient implements Serializable {
		private static final long serialVersionUID = 1L; 
		public static void handle(ModelRequestFromClient req, World worldIn) {
			ensureServerModelInit();
			CircuitsMod.network.sendToAll(new TypedMessage(new ClientModelContainer(infoMap, circuitTree)));
		}
    }
    
    public static class ModelResponseFromServer implements Serializable {
		private static final long serialVersionUID = 1L;
		private ClientModelContainer modelContainer;
    	public ModelResponseFromServer(ClientModelContainer modelContainer) {
    		this.modelContainer = modelContainer;
    	}
		public static void handle(ModelResponseFromServer response, World worldIn) {
			CircuitInfoProvider.clearState();
			CircuitInfoProvider.infoMap = response.modelContainer.infoMap;
			CircuitInfoProvider.circuitTree = response.modelContainer.circuitTree;
		}
    }
    
    private static File getUIDMapFile() {
    	return new File(FileUtils.getWorldSaveDir().getPath() + "/uidmap.nbt");
    }
    
    public static Optional<CircuitUID> getUIDFromFolderName(String name) {
    	for (Map.Entry<String, CircuitUID> entry : folderToUIDMap.entrySet()) {
    		if (entry.getKey().equalsIgnoreCase(name)) {
    			return Optional.of(entry.getValue());
    		}
    	}
    	return Optional.empty();
    }
    
    private static void loadUIDMapDefaults() {
    	/*
    	 * And: 0
    	 * Splitter : 2
    	 * Combiner : 3
    	 * Emitter: 7
    	 * PulseLengthener : 9
    	 * AnalogToDigital : 11
    	 * Inverter : 13
    	 * Demultiplexer : 15
    	 * NBitDLatch : 17
    	 * ABBA : 19
    	 * Input : 21
    	 * Or : 23
    	 */
    	String[] toRegister = {"And", "Xor", "Splitter", "Combiner", "Nor", "Nand",
    			               "HalfAdder", "Emitter", "RisingEdgeDetector", "PulseLengthener",
    			               "DigitalToAnalog", "AnalogToDigital", "Clock", "Inverter",
    			               "Multiplexer", "Demultiplexer", "FullAdder", "NBitDLatch", "Implies", "ABBA", "Dummy",
    			               "Input", "Output", "Or", "InputBitSelect", "OutputBitSelect", "Delay"};
    	
    	for (int i = 0; i < toRegister.length; i++) {
    		folderToUIDMap.put(toRegister[i], CircuitUID.fromInteger(i));
    	}
    }
    
    public static CircuitTreeModel getCircuitListModel() {
    	return circuitTree;
    }
    
    private static Optional<HashMap<String, CircuitUID>> uidMapFromFile(File file) {
    	try {
    		NBTTagCompound cpd = CompressedStreamTools.read(file);
    		HashMap<String, CircuitUID> result = new HashMap<>();
    		for (String key : cpd.getKeySet()) {
    			Integer value = cpd.getInteger(key);
    			result.put(key, CircuitUID.fromInteger(value));
    		}
    		return Optional.of(result);
    	}
    	catch (IOException e) {
    		return Optional.empty();
    	}
    }
    
    private static void uidMapToFile(File file, HashMap<String, CircuitUID> uidMap) {
    	try {
    		NBTTagCompound cpd = new NBTTagCompound();
    		for (String key : uidMap.keySet()) {
    			cpd.setInteger(key, uidMap.get(key).toInteger());
    		}
    		CompressedStreamTools.write(cpd, file);
    	}
    	catch (IOException e) {
    		Log.internalError("Failed to write uidMap!");
    	}
    }
    
    public static void loadUIDMapFromFile() {
    	folderToUIDMap = new HashMap<String, CircuitUID>();
    	Optional<HashMap<String, CircuitUID>> uidMap = Optional.empty();
    	if (getUIDMapFile().exists()) {
    		uidMap = uidMapFromFile(getUIDMapFile());
    	}
    	if (uidMap.isPresent()) {
    		folderToUIDMap = uidMap.get();
    	}
    	//No matter what, make sure that the defaults are always in the right place
    	loadUIDMapDefaults();
    	//After loading it into the map, make sure that lastID of circuitUID is set right
    	OptionalInt maxId = folderToUIDMap.values().stream().mapToInt((uid) -> uid.toInteger()).max();
    	CircuitUID.bumpLastUID(maxId.getAsInt());
    	saveUIDMapToFile();
    }
    
    public static void saveUIDMapToFile() {
    	uidMapToFile(getUIDMapFile(), folderToUIDMap);
    }
    
    private static Runnable clientModelInitRunnable = () -> {
    	CircuitsMod.network.sendToServer(new TypedMessage(new ModelRequestFromClient()));
    };

	public static void ensureClientModelInit() {
		if (isClientModelInit()) {
			return;
		}
		TickEvents.instance().addImmediateAction(clientModelInitRunnable);
	}
	
	public static boolean isClientModelInit() {
		return infoMap != null;
	}
	
	private static class SpecializedInfoRequestRunnable implements Runnable {
		private SpecializedCircuitUID uid;
		public SpecializedInfoRequestRunnable(SpecializedCircuitUID uid) {
			this.uid = uid;
		}
		@Override
		public void run() {
			CircuitsMod.network.sendToServer(new TypedMessage(uid));
		}
		@Override
		public int hashCode() {
			return uid.hashCode();
		}
		@Override
		public boolean equals(Object other) {
			if (other instanceof SpecializedInfoRequestRunnable) {
				return this.uid.equals(((SpecializedInfoRequestRunnable) other).uid);
			}
			return false;
		}
		
	}
	
	public static void requestSpecializedClientInfoFor(SpecializedCircuitUID uid) {
		if (uid != null) {
			TickEvents.instance().addImmediateAction(new SpecializedInfoRequestRunnable(uid));
		}
	}
	
	private static CircuitUID getUIDForDir(File dir) {
		try{
			if (folderToUIDMap == null) {
				loadUIDMapFromFile();
			}
			
			CircuitUID result = folderToUIDMap.get(dir.getName());
			if (result == null) {
				//In this case, we need to generate a new UID for the given folder name!
				result = CircuitUID.getNextUID();
				folderToUIDMap.put(dir.getName(), result);
				saveUIDMapToFile();
			}
			return result;
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Copies the default circuits from the mod jar to the mod-global config/circuits folder
	 */
	public static void copyDefaultCircuitsFromJar() {
		ResourceLocation jarZip = new ResourceLocation(CircuitsMod.MODID, "circuits/defaults.zip");
		File defaultCircuitsZip = new File(FileUtils.getConfigRootDir().toString() + "/defaultCircuits.zip");
		FileUtils.copyResourceToFile(jarZip, defaultCircuitsZip);
		FileUtils.unzip(defaultCircuitsZip, FileUtils.getGlobalCircuitDefinitionsDir());
	}
    
	public static void ensureServerModelInit() { 
		if (implMap != null) {
			return;
		}
		File globalCircuitsDir = FileUtils.getGlobalCircuitDefinitionsDir();
		
		if (!globalCircuitsDir.exists()) {
			copyDefaultCircuitsFromJar();
			if (!globalCircuitsDir.exists()) {
				//Fallback, leave 'em with an empty directory
				globalCircuitsDir.mkdirs();
			}
		}
		//Okay, the above was just for initializing the mod-global circuit definitions directory.
		//Now, we copy its contents into the world-specific circuit definitions directory,
		//with the config-global settings taking precedence
		File circuitsDir = FileUtils.getWorldCircuitDefinitionsDir();
		
		if (!FileUtils.getWorldCustomCircuitsDir().exists()) {
			FileUtils.getWorldCustomCircuitsDir().mkdirs();
		}
		
		try {
			org.apache.commons.io.FileUtils.copyDirectory(globalCircuitsDir, circuitsDir, (f) -> !f.getName().startsWith("."));
		}
		catch (IOException e) {
			Log.internalError("Failed to copy global circuit configs to world configs " + e.getMessage());
		}
		
		
		implMap = new HashMap<>();
		infoMap = new HashMap<>();
		
		CircuitDirectory rootDir = new CircuitDirectory("circuits");
		
		ensureServerModelInitHelper(rootDir, circuitsDir);
		
		rootDir.populateSearchIndex();
		
		circuitTree = new CircuitTreeModel(rootDir);
	}
	
	private static void ensureServerModelInitHelper(CircuitDirectory parentNode, File dir) {
		for (File subDir : dir.listFiles()) {
			if (!subDir.isDirectory()) {
				continue;
			}
			if (subDir.getName().startsWith(".")) {
				continue;
			}
			
			if (isCircuitDirectory(subDir)) {
				CircuitUID uid = getUIDForDir(subDir);
				Optional<CircuitInfo> entry = CircuitInfo.fromFolder(subDir);
				Optional<ChipImpl> impl = ChipImpl.fromCircuitDirectory(subDir);
				if (!entry.isPresent() || !impl.isPresent()) {
					Log.userError("Circuit in directory " + subDir + " is either formatted incorrectly, or is underspecified!");
					continue;
				}
				entry.get().fillImplInfo(impl.get());
				
				if (infoMap.containsKey(uid)) {
					Log.info("Duplicate circuit in directory " + subDir + ". Ignored.");
					continue;
				}
				
				infoMap.put(uid, entry.get());
				implMap.put(uid, impl.get());
				
				parentNode.addChild(new CircuitCell(parentNode, uid, entry.get()));
			}
			else {
				CircuitDirectory child = new CircuitDirectory(parentNode, subDir.getName());
				ensureServerModelInitHelper(child, subDir);
				parentNode.addChild(child);
			}
		}
	}
	
	public static void refreshServerInfoAndSendToClient() {
		CircuitInfoProvider.clearState();
		CircuitInfoProvider.ensureServerModelInit();
		CircuitsMod.network.sendToAll(new TypedMessage(new ClientModelContainer(infoMap, circuitTree)));
	}
	
	/**
	 * @return True if the File is a directory, and contains a circuit implementation
	 */
	public static boolean isCircuitDirectory(File dir) {
		if (!dir.isDirectory()) {
			return false;
		}
		return Stream.of(dir.listFiles()).anyMatch((f) -> f.getName().equalsIgnoreCase("Implementation.class"));
	}
	
	public static boolean isServerModelInit() {
		return implMap != null;
	}
	
	public static ResourceLocation getTexture(CircuitUID uid) {
    	if (texMap.containsKey(uid)) {
    		return texMap.get(uid);
    	}
    	if (!isClientModelInit() || !CircuitInfoProvider.hasInfoOn(uid)) {
    		ensureClientModelInit();
    		return null;
    	}
		
		CircuitInfo info = infoMap.get(uid);
		
	   	TextureManager texMan = Minecraft.getMinecraft().getTextureManager();
		ResourceLocation loc = texMan.getDynamicTextureLocation(uid.toString(), new DynamicTexture(info.getImage()));
		texMap.put(uid, loc);
		return loc;
	}
	
	public static boolean hasInfoOn(CircuitUID uid) {
		return infoMap.containsKey(uid);
	}
	
	public static Optional<CircuitCell> getCellFor(CircuitUID uid) {
		return circuitTree.getRootDirectory().locateCellForUID(uid);
	}
	
	public static List<CircuitCell> getCellsWithName(String circuitName) {
		String sanitized = StringUtils.sanitizeAlphaNumeric(circuitName);
		List<CircuitCell> dups = Lists.newArrayList();
		circuitTree.getRootDirectory().applyToCellsRecursively((cell) -> {
			if (cell.getName().equalsIgnoreCase(sanitized)) {
				dups.add(cell);
			}
		});
		return dups;
	}
	
	public static boolean nameAlreadyTaken(String circuitName) {
		return !getCellsWithName(circuitName).isEmpty();
	}
	
	/**
	 * To be called from the client to determine if specialized circuit info has been sent over.
	 * @param uid
	 * @return
	 */
	public static boolean hasSpecializedInfoOn(SpecializedCircuitUID uid) {
		return infoCache.containsKey(uid);
	}
	
	public static boolean hasImplOn(CircuitUID uid) {
		return implMap.containsKey(uid);
	}
	
	public static int getNumInputs(SpecializedCircuitUID uid) {
		return getInputWidths(uid).length;
	}
	public static int getNumOutputs(SpecializedCircuitUID uid) {
		return getOutputWidths(uid).length;
	}
	
	/**
	 * Has to work on both the client and the server
	 * @param uid
	 * @return
	 */
	public static int[] getInputWidths(SpecializedCircuitUID uid) {
		return infoCache.get(uid).getInputWidths();
	}
	/**
	 * Has to work on both the client and on the server
	 * @param uid
	 * @return
	 */
	public static int[] getOutputWidths(SpecializedCircuitUID uid) {
		return infoCache.get(uid).getOutputWidths();
	}
	
	public static boolean[] getAnalogInputs(SpecializedCircuitUID uid) {
		return infoCache.get(uid).getAnalogInputs();
	}
	public static boolean[] getAnalogOutputs(SpecializedCircuitUID uid) {
		return infoCache.get(uid).getAnalogOutputs();
	}
	
	/**
	 * To be called on the server
	 * @param uid
	 */
	public static void createSpecializedInfoFor(SpecializedCircuitUID uid) {
		if (!infoCache.containsKey(uid)) {
			Optional<SpecializedChipImpl> impl = SpecializedChipImpl.of(implMap.get(uid.getUID()), uid.getOptions());
			if (impl.isPresent()) {
				SpecializedCircuitInfo info = new SpecializedCircuitInfo(uid, infoMap.get(uid.getUID()), impl.get());
				infoCache.put(uid, info);
			}
		}
	}
	
	/**
	 * Meant to be called from the server only
	 */
	public static Optional<SpecializedCircuitInfo> getSpecializedInfoFor(SpecializedCircuitUID uid) {
		createSpecializedInfoFor(uid);
		if (infoCache.containsKey(uid)) {
			return Optional.of(infoCache.get(uid));
		}
		return Optional.empty();
	}
	
	/**
	 * Meant to be called from the server only
	 */
	public static Optional<SpecializedChipImpl> getSpecializedImpl(SpecializedCircuitUID uid) {
		createSpecializedInfoFor(uid);
		
		ChipImpl impl = implMap.get(uid.getUID());
		
		Optional<SpecializedChipImpl> specialized = SpecializedChipImpl.of(impl, uid.getOptions());
		if (!specialized.isPresent()) {
			Log.userError("Failed to instantiate specialized circuit for " + uid);
		}
		return specialized;
	}
	
	/**
	 * Meant to be called from the server only
	 * @param uid
	 * @return
	 */
	public static Optional<ChipInvoker> getInvoker(SpecializedCircuitUID uid) {
		return getSpecializedImpl(uid).map(impl -> impl.getInvoker());
	}
	public static WireDirectionGenerator getWireDirectionGenerator(CircuitUID uid) {
		return infoMap.get(uid).getWireDirectionGenerator();
	}
	
	public static String getDisplayName(SpecializedCircuitUID uid) {
		if (infoCache.containsKey(uid)) {
			return infoCache.get(uid).getFullDisplayName();
		}
		return infoMap.get(uid.getUID()).getName();
	}
}
