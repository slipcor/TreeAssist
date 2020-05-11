package me.itsatacoshop247.TreeAssist.core;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.*;

public class TreeCalculator {

    private static void debug(String test) {
        System.out.println(test);
    }

    private static boolean debugContain(List<Material> list, Material needle) {
        return list.contains(needle);

        /*

        if (needle == null) {
            System.out.println("NULL");
            return false;
        }
        for (Material m : list) {
            if (m != null) {
                boolean found = (needle.equals(m));
                System.out.println(m + "; " + m.name() + "; " + needle + "; " + needle.name() + " => " + found);
                if (found) {
                    return found;
                }
            }
        }
        return false;

        */
    }

    public static void debugPrint(List<Material> source) {
        for (Material m : source) {
            if (m != null) {
                System.out.print(m.name() + ",");
            }
        }
        System.out.println();
    }

    public static Block validate(Block block, TreeConfig config) {
        List<Material> trunkBlocks = config.getMaterials(TreeConfig.CFG.TRUNK_MATERIALS);

        // System.out.print("Trunk mats: ");
        // debugPrint(trunkBlocks);

        List<Material> extraBlocks = config.getMaterials(TreeConfig.CFG.BLOCKS_MATERIALS);

        // System.out.print("Block mats: ");
        // debugPrint(extraBlocks);

        List<Material> naturalBlocks = config.getMaterials(TreeConfig.CFG.NATURAL_BLOCKS);

        // System.out.print("Natural mats: ");
        // debugPrint(naturalBlocks);

        List<Material> groundBlocks = config.getMaterials(TreeConfig.CFG.GROUND_BLOCKS);

        // System.out.print("Ground mats: ");
        // debugPrint(groundBlocks);

        Block checkBlock = block;

        boolean trunkDiagonally = config.getBoolean(TreeConfig.CFG.TRUNK_DIAGONAL);

        while (debugContain(trunkBlocks, checkBlock.getType())) {
            checkBlock = checkBlock.getRelative(BlockFace.DOWN);

            if (trunkDiagonally && !debugContain(trunkBlocks, checkBlock.getType())) {
                Material checkMaterial = checkBlock.getType();

                // debug("No more trunk going down at " + block.getLocation() + " - type: " + checkMaterial);

                if (!Utils.isAir(checkBlock.getType())) {
                    if (groundBlocks.contains(checkMaterial)){
                        // debug("It's a ground block!");
                        return checkBlock.getRelative(BlockFace.UP);
                    } else if (
                            !extraBlocks.contains(checkMaterial) &&
                                    !naturalBlocks.contains(checkMaterial) &&
                                    !TreeStructure.allTrunks.contains(checkMaterial) &&
                                    !TreeStructure.allExtras.contains(checkMaterial)) {
                        // debug("Unexpected block! Not a valid tree!");
                        return null;
                    }
                }

                blockLoop: for (int x = -1; x < 2; x++) {
                    for (int z= -1; z < 2; z++) {
                        Material innerCheck = checkBlock.getRelative(x, 0, z).getType();
                        if (trunkBlocks.contains(innerCheck)) {
                            checkBlock = checkBlock.getRelative(x, 0, z);
                            break blockLoop;
                        }
                        // debug("Checking diagonal at " + checkBlock.getRelative(x, 0, z).getLocation() + " - type: " + innerCheck);
                        if (!Utils.isAir(innerCheck)) {
                            if (!groundBlocks.contains(innerCheck) &&
                                    !extraBlocks.contains(innerCheck) &&
                                    !naturalBlocks.contains(innerCheck) &&
                                    !TreeStructure.allTrunks.contains(checkMaterial) &&
                                    !TreeStructure.allExtras.contains(checkMaterial)) {
                                // debug("Unexpected block! Not a valid tree!");
                                return null;
                            }
                        }
                    }
                }
            }
        }

        if (groundBlocks.contains(checkBlock.getType())) {
            // debug("We hit the ground!");

            // we hit the ground and no problems
            return checkBlock.getRelative(BlockFace.UP);
        }

        debug("We did not find a ground block. not a valid tree!");

        return null;


    }
}
