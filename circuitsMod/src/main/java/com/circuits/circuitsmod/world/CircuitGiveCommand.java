package com.circuits.circuitsmod.world;

import java.util.List;
import java.util.Optional;

import javax.annotation.Nonnull;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

import com.circuits.circuitsmod.CircuitsMod;
import com.circuits.circuitsmod.circuit.CircuitConfigOptions;
import com.circuits.circuitsmod.circuit.CircuitInfoProvider;
import com.circuits.circuitsmod.circuit.CircuitUID;
import com.circuits.circuitsmod.circuit.SpecializedCircuitInfo;
import com.circuits.circuitsmod.circuit.SpecializedCircuitUID;
import com.circuits.circuitsmod.circuitblock.CircuitItem;
import com.google.common.collect.Lists;

public class CircuitGiveCommand extends CommandBase {
	
	private String name = "givecircuit";

    public CircuitGiveCommand(){
        aliases = Lists.newArrayList(CircuitsMod.MODID, name, name.toUpperCase());
    }

    private final List<String> aliases;

    @Override
    @Nonnull
    public String getCommandName() {
        return name;
    }

    @Override
    @Nonnull
    public String getCommandUsage(@Nonnull ICommandSender sender) {
        return name + " <id>";
    }

    @Override
    @Nonnull
    public List<String> getCommandAliases() {
        return aliases;
    }

    @Override
    public void execute(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args) throws CommandException {
        if (args.length < 1) {
            return;
        }
        String majorId = args[0];
        CircuitInfoProvider.loadUIDMapFromFile();
        CircuitInfoProvider.ensureServerModelInit();
        
        Optional<CircuitUID> uid = CircuitInfoProvider.getUIDFromFolderName(majorId);
        if (!uid.isPresent()) {
    		uid = CircuitUID.fromString(majorId);
        }
        if (uid.isPresent()) {
        	if (!CircuitInfoProvider.hasInfoOn(uid.get())) {
        		uid = Optional.empty();
        	}
        }
        if (!uid.isPresent()) {
            sender.addChatMessage(new TextComponentString(TextFormatting.RED + "Invalid circuit UID!"));
        }
        
        int[] circuitOptions = new int[args.length - 1];
        
        try {
        	for (int i = 1; i < args.length; i++) {
        		circuitOptions[i - 1] = Integer.parseInt(args[i]);
        	}
        }
        catch (NumberFormatException e) {
        	sender.addChatMessage(new TextComponentString(TextFormatting.RED + "Malformed circuit options!"));
        }
        CircuitConfigOptions configs = new CircuitConfigOptions(circuitOptions);
        
        SpecializedCircuitUID finalUid = new SpecializedCircuitUID(uid.get(), configs);
        
        Optional<SpecializedCircuitInfo> info = CircuitInfoProvider.getSpecializedInfoFor(finalUid);
        if (!info.isPresent()) {
        	sender.addChatMessage(new TextComponentString(TextFormatting.RED + "A circuit with that name and set of config options doesn't exist!"));
        }
        else {
            if (sender instanceof EntityPlayer) {
                ((EntityPlayer) sender).inventory.addItemStackToInventory(CircuitItem.getStackFromUID(finalUid));
            }
        }

    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return true;
    }
}
