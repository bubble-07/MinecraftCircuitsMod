package com.circuits.circuitsmod.common;

import java.util.Optional;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class EntityUtils {
	public static Optional<EntityPlayer> getPlayerFromUID(World worldIn, UUID playerID) {
		return worldIn.getEntities(EntityPlayer.class, (p) -> 
	    p.getUniqueID().equals(playerID)).stream().findAny();
	}

}
