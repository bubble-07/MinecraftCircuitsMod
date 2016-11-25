package com.circuits.circuitsmod.busblock;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.circuits.circuitsmod.common.BlockFace;
import com.circuits.circuitsmod.common.PosUtils;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public class IncrementalConnectedComponents {
	
	public static Set<BlockFace> unifyOnAdd(BlockPos newlyAdded, Predicate<BlockPos> safe, Predicate<BlockFace> success) {
		Set<BlockFace> result = new HashSet<>();
		
		//This is an unneccessarily exhaustive search, but for now, correctness matters more than speed.
		Stack<BlockPos> toVisit = new Stack<>();
		Set<BlockPos> visited = new HashSet<>();
		toVisit.push(newlyAdded);
		while (!toVisit.isEmpty()) {
			BlockPos current = toVisit.pop();
			if (visited.contains(current)) {
				continue;
			}
			visited.add(current);
			//From this block, search all of the adjacent faces of blocks next to it, and if successful, add to the success set
			PosUtils.adjacentFaces(current).filter(success).forEachOrdered((f) -> result.add(f));
			PosUtils.neighbors(current).filter(safe).forEachOrdered((p) -> toVisit.push(p));
		}
		return result;
	}
	
	public static Set<Set<BlockFace>> separateOnDelete(BlockPos newlyRemoved, Predicate<BlockPos> safe, Predicate<BlockFace> success) {
		Set<BlockFace> directlyAdjacent = PosUtils.adjacentFaces(newlyRemoved).filter(success).collect(Collectors.toSet());
		Predicate<BlockPos> safeMinusRemoved = (p) -> (!p.equals(newlyRemoved) && safe.test(p));
		Set<Set<BlockFace>> indirect = PosUtils.neighbors(newlyRemoved).filter(safeMinusRemoved)
		        .map((neighborPos) -> unifyOnAdd(neighborPos, safeMinusRemoved, success))
		        .distinct().collect(Collectors.toSet());
		Set<Set<BlockFace>> result = new HashSet<>();
		for (Set<BlockFace> indir : indirect) {
			result.add(indir);
		}
		for (BlockFace direct : directlyAdjacent) {
			HashSet<BlockFace> singletonSet = new HashSet<>();
			singletonSet.add(direct);
			result.add(singletonSet);
		}
		return result;
	}
	
	/*
	public static Set<BlockFace> unifyOnAdd(BlockPos newlyAdded, Predicate<BlockPos> safe, Predicate<BlockFace> success) {
		Set<Set<BlockFace>> searchResults = search(newlyAdded, safe, success, false);
		return searchResults.stream().filter((s) -> !s.isEmpty())
				                     .map((s) -> s.iterator().next()).collect(Collectors.toSet());
	}
	
	public static Set<Set<BlockFace>> separateOnDelete(BlockPos toDelete, Predicate<BlockPos> safe, Predicate<BlockFace> success) {
		return search(toDelete, safe, success, true);
	}*/
	
	/*
	 * Stuff that was too clever (possible factor-of-6 speedup) based on sending out six "probes" from the start in every direction,
	 * but turned out to be too complicated
	public static Set<Set<BlockFace>> search(BlockPos newlyAdded, Predicate<BlockPos> safe, Predicate<BlockFace> success, boolean exhaustive) {
		List<EnumFacing> searchDirs = Stream.of(EnumFacing.VALUES).filter((f) -> safe.test(newlyAdded.offset(f))).collect(Collectors.toList());
		if (searchDirs.size() < 2 && 
		   !searchDirs.stream().map(f -> new BlockFace(newlyAdded, f).otherSide()).anyMatch(success)) {
			
			return new HashSet<>();
		}
		Map<BlockPos, EnumFacing> foundIds = new HashMap<>();
		Set<Set<BlockFace>> foundResults = new HashSet<>();
		
		for (EnumFacing probeId : searchDirs) {
			Set<BlockFace> frontier = new HashSet<>();
			frontier.add(new BlockFace(newlyAdded.offset(probeId), probeId.getOpposite()));
			Stack<BlockFace> searchStack = new Stack<>();
			Set<BlockFace> foundResult = new HashSet<>();
			
			while (!searchStack.isEmpty()) {
				BlockFace current = searchStack.pop();
				
				EnumFacing prevProbe = foundIds.get(current.getPos());
				
				if (prevProbe != null) {
					if (!prevProbe.equals(probeId)) {
						//We must've visited this same exact position before in a different probe.
						//Since this is written to be sequential, this means
						//that our current search is useless, so give up.
						searchStack.clear();
					}
					else {
						//Must be that we've already visited this block within this probe.
						//Just ignore that this ever happened.
					}
				}
				else {
					if (success.test(current)) {
						//Terminal, successful node, which in our context
						//may have multiple successful faces that we're still in the process
						//of searching for. In this case, we should __not__ add to "foundIds",
						//as we may find multiple different successful faces of the same block
						foundResult.add(current);
						if (!exhaustive) {
							searchStack.clear();
						}
					}
					else if (safe.test(current.getPos())) {
						PosUtils.adjacentFaces(current.getPos())
						.filter((bf) -> !bf.otherSide().getPos().equals(current.getPos()))
						.forEachOrdered((f) -> {
							searchStack.push(f);
						});
						foundIds.put(current.getPos(), probeId);
					}
				}
			}
			if (!foundResult.isEmpty()) {
				foundResults.add(foundResult);
			}
		}
		return foundResults;
	}
	*/
	
}
