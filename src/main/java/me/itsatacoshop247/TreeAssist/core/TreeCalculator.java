package me.itsatacoshop247.TreeAssist.core;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;

public class TreeCalculator {
    public static List<Material> allTrunks = new ArrayList<>();
    public static List<Material> allExtras = new ArrayList<>();

    private static void debug(String test) {
        System.out.println(test);
    }

    public static List<Material> getMaterials(TreeConfig config, TreeConfig.CFG node) {
        List<String> list = config.getStringList(node, new ArrayList<>());

        List<Material> matList = new ArrayList<>();

        for (String matName : list) {
            matList.add(Material.matchMaterial(matName));
        }

        return matList;
    }

    public static void debugPrint(List<Material> source) {
        for (Material m : source) {
            if (m != null) {
                System.out.print(m.name() + ",");
            }
        }
        System.out.println();
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

    public static Block validate(Block block, TreeConfig config) {
        List<Material> trunkBlocks = getMaterials(config, TreeConfig.CFG.TRUNK_MATERIALS);

        // System.out.print("Trunk mats: ");
        // debugPrint(trunkBlocks);

        List<Material> extraBlocks = getMaterials(config, TreeConfig.CFG.BLOCKS_MATERIALS);

        // System.out.print("Block mats: ");
        // debugPrint(extraBlocks);

        List<Material> naturalBlocks = getMaterials(config, TreeConfig.CFG.NATURAL_BLOCKS);

        // System.out.print("Natural mats: ");
        // debugPrint(naturalBlocks);

        List<Material> groundBlocks = getMaterials(config, TreeConfig.CFG.GROUND_BLOCKS);

        // System.out.print("Ground mats: ");
        // debugPrint(groundBlocks);

        Block checkBlock = block;

        boolean trunkDiagonally = config.getBoolean(TreeConfig.CFG.TRUNK_DIAGONAL);

        while (debugContain(trunkBlocks, checkBlock.getType())) {
            checkBlock = checkBlock.getRelative(BlockFace.DOWN);

            if (trunkDiagonally && !debugContain(trunkBlocks, checkBlock.getType())) {
                Material checkMaterial = checkBlock.getType();

                // debug("No more trunk going down at " + block.getLocation() + " - type: " + checkMaterial);

                if (checkBlock.getType() != Material.AIR) {
                    if (groundBlocks.contains(checkMaterial)){
                        // debug("It's a ground block!");
                        return checkBlock.getRelative(BlockFace.UP);
                    } else if (extraBlocks.contains(checkMaterial)) {
                        // debug("It's an extra block!");
                    } else if (naturalBlocks.contains(checkMaterial)){
                        // debug("It's a natural block!");
                    } else if (!allTrunks.contains(checkMaterial) && !allExtras.contains(checkMaterial)) {
                        // debug("Unexpected block! Not a valid tree!");
                        return null;
                    }
                }

                outer: for (int x = -1; x < 2; x++) {
                    inner: for (int z= -1; z < 2; z++) {
                        Material innerCheck = checkBlock.getRelative(x, 0, z).getType();
                        if (trunkBlocks.contains(innerCheck)) {
                            checkBlock = checkBlock.getRelative(x, 0, z);
                            break outer;
                        }
                        // debug("Checking diagonal at " + checkBlock.getRelative(x, 0, z).getLocation() + " - type: " + innerCheck);
                        if (innerCheck != Material.AIR) {
                            if (groundBlocks.contains(innerCheck)){
                                // debug("It's a ground block!");
                            } else if (extraBlocks.contains(innerCheck)) {
                                // debug("It's an extra block!");
                            } else if (naturalBlocks.contains(innerCheck)){
                                // debug("It's a natural block!");
                            } else if (!allTrunks.contains(checkMaterial) && !allExtras.contains(checkMaterial)) {
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

    public static boolean verifyShape(TreeConfig config, Block bottom) {
        List<Material> trunkBlocks = getMaterials(config, TreeConfig.CFG.TRUNK_MATERIALS);

        // System.out.print("Trunk mats: ");
        // debugPrint(trunkBlocks);

        List<Material> extraBlocks = getMaterials(config, TreeConfig.CFG.BLOCKS_MATERIALS);

        // System.out.print("Block mats: ");
        // debugPrint(extraBlocks);

        List<Material> naturalBlocks = getMaterials(config, TreeConfig.CFG.NATURAL_BLOCKS);

        // System.out.print("Natural mats: ");
        // debugPrint(naturalBlocks);

        List<Material> groundBlocks = getMaterials(config, TreeConfig.CFG.GROUND_BLOCKS);

        // System.out.print("Ground mats: ");
        // debugPrint(groundBlocks);

        int thickness = config.getInt(TreeConfig.CFG.TRUNK_THICKNESS);
        boolean branches = config.getBoolean(TreeConfig.CFG.TRUNK_BRANCH);
        boolean diagonal = config.getBoolean(TreeConfig.CFG.TRUNK_DIAGONAL);

        if (thickness == 1) {
            // check for other trunks around
            Material westMat = bottom.getRelative(BlockFace.WEST).getType();
            Material eastMat = bottom.getRelative(BlockFace.EAST).getType();
            Material northMat = bottom.getRelative(BlockFace.NORTH).getType();
            Material southMat = bottom.getRelative(BlockFace.SOUTH).getType();
            if (trunkBlocks.contains(westMat) || trunkBlocks.contains(eastMat) || trunkBlocks.contains(northMat) || trunkBlocks.contains(southMat)) {
                //TODO: implement
                System.out.println("We ignore thick ones / farms for now!");
                return false;
            }

            // single trunk, that should be easy, right?
            List<Block> trunk = findTrunk(bottom, trunkBlocks, extraBlocks, naturalBlocks, diagonal);

            if (trunk == null) {

            } else {
                debug("Tree of size " + trunk.size() + " found!");

                for (Block tree : trunk) {
                    tree.breakNaturally();
                }

                return true;
            }

        }

        //TODO: implement
        System.out.println("We ignore thick ones for now!");
        return false;



        /*
        GROUND_BLOCKS("Ground Blocks", new ArrayList<>()), // the allowed blocks below the tree trunk

        BLOCKS_MATERIALS("Blocks.Materials", new ArrayList<>()), // the expected blocks part of the tree, next to the trunk

        BLOCKS_CAP_HEIGHT("Blocks.Cap.Height", 2), // Branch Topping Leaves Height
        BLOCKS_CAP_RADIUS("Blocks.Cap.Radius", 3), // Branch Topping Leaves Radius

        BLOCKS_MIDDLE_AIR("Blocks.Middle.Air", false), // allow air pockets?
        BLOCKS_MIDDLE_EDGES("Blocks.Middle.Edges", false), // would edges be populated?
        BLOCKS_MIDDLE_RADIUS("Blocks.Middle.Radius", 2), // the tree middle leaf radius (radius starts away from trunk!)

        BLOCKS_TOP_AIR("Blocks.Top.Air", false), // allow air pockets?
        BLOCKS_TOP_EDGES("Blocks.Top.Edges", false), // would edges be populated?
        BLOCKS_TOP_RADIUS("Blocks.Top.Radius", 3), // the tree top leaf radius
        BLOCKS_TOP_HEIGHT("Blocks.Top.Height", 3),

        TRUNK_BRANCH("Trunk.Branch", false),
        TRUNK_DIAGONAL("Trunk.Diagonal", false), // Trunk can move diagonally even (Acacia)
        TRUNK_RADIUS("Trunk.Radius", 1),
        TRUNK_EDGES("Trunk.Edges", false), // Trunk can have extra on the edges (Dark Oak)
        TRUNK_MINIMUM_HEIGHT("Trunk.Minimum Height", 4),
        TRUNK_MAXIMUM_HEIGHT("Trunk.Maximum Height", 30),
        TRUNK_MATERIALS("Trunk.Materials", new ArrayList<>()), // the expected materials part of the tree trunk
        TRUNK_THICKNESS("Trunk.Thickness", 1), // This value is also used for radius calculation!
        TRUNK_UNEVEN_BOTTOM("Trunk.Uneven Bottom", false), // Can saplings/lowest trunks be on different Y?

        TOOL_LIST("Tool List", new ArrayList<>()),
        NATURAL_BLOCKS("Natural Blocks", new ArrayList<>()) // blocks that are okay to have around trees

         */
    }

    private static List<Block> findTrunk(Block bottom, List<Material> trunkBlocks, List<Material> extraBlocks, List<Material> naturalBlocks, boolean trunkDiagonally) {
        List<Block> result = new ArrayList<>();

        Block checkBlock = bottom;

        while (debugContain(trunkBlocks, checkBlock.getType())) {
            result.add(checkBlock);

            checkBlock = checkBlock.getRelative(BlockFace.UP);

            if (trunkDiagonally && !debugContain(trunkBlocks, checkBlock.getType())) {
                Material checkMaterial = checkBlock.getType();

                debug("No more trunk going up at " + checkBlock.getLocation() + " - type: " + checkMaterial);

                if (checkBlock.getType() != Material.AIR) {
                    if (extraBlocks.contains(checkMaterial)) {
                        debug("It's an extra block!");
                    } else if (naturalBlocks.contains(checkMaterial)){
                        debug("It's a natural block!");
                    } else if (!allTrunks.contains(checkMaterial) && !allExtras.contains(checkMaterial)) {
                        debug("Unexpected block! Not a valid tree!");
                        return null;
                    }
                }

                outer: for (int x = -1; x < 2; x++) {
                    inner: for (int z= -1; z < 2; z++) {
                        Material innerCheck = checkBlock.getRelative(x, 0, z).getType();
                        if (trunkBlocks.contains(innerCheck)) {
                            checkBlock = checkBlock.getRelative(x, 0, z);
                            break outer;
                        }
                        debug("Checking diagonal at " + checkBlock.getRelative(x, 0, z).getLocation() + " - type: " + innerCheck);
                        if (innerCheck != Material.AIR) {
                            if (extraBlocks.contains(innerCheck)) {
                                debug("It's an extra block!");
                            } else if (naturalBlocks.contains(innerCheck)){
                                debug("It's a natural block!");
                            } else if (!allTrunks.contains(checkMaterial) && !allExtras.contains(checkMaterial)) {
                                debug("Unexpected block! Not a valid tree!");
                                return null;
                            }
                        }
                    }
                }
            }
        }

        if (allExtras.contains(checkBlock.getType())) {
            debug("We hit the roof!");

            // we hit the ground and no problems
            return result;
        }

        debug("We did not find a roof block. not a valid tree!");

        return null;
    }
}
