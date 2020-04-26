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

    public static TreeStructure calculateShape(TreeConfig config, Block bottom, Boolean onlyTrunk) {
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
                return null;
            }

            // single trunk, that should be easy, right?
            List<Block> trunk = findTrunk(bottom, trunkBlocks, extraBlocks, naturalBlocks, diagonal);

            if (trunk == null) {
                // no tree found, nothing to remove!
            } else {
                debug("Tree of size " + trunk.size() + " found!");

                if (onlyTrunk) {
                    // this will be used by other trees being broken checking OUR trunk for leaf distance and alike
                    return new TreeStructure(trunk);
                }

                if (trunk.size() > config.getInt(TreeConfig.CFG.TRUNK_MAXIMUM_HEIGHT)) {
                    Utils.plugin.getLogger().warning("Higher than maximum!");
                } else if (trunk.size() < config.getInt(TreeConfig.CFG.TRUNK_MINIMUM_HEIGHT)) {
                    Utils.plugin.getLogger().warning("Lower than minimum!");
                }

                List<Block> extras = getExtras(trunk, trunkBlocks, extraBlocks, naturalBlocks, config);

                if (extras == null || extras.size() < 10) {
                    return null;
                }

                /*
                if (branches) {
                    List<Block> branch = getBranches(trunk, trunkBlocks, extraBlocks, naturalBlocks);
                    trunk.addAll(branch);


                }*/

                for (Block tree : trunk) {
                    tree.breakNaturally();
                }

                for (Block leaf : extras) {
                    leaf.breakNaturally();
                }

                bottom.setType(Material.matchMaterial(config.getString(TreeConfig.CFG.REPLANT)));

                return new TreeStructure(trunk, extras);
            }

        }

        //TODO: implement
        System.out.println("We ignore thick ones for now!");
        return null;



        /*

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
        TRUNK_THICKNESS("Trunk.Thickness", 1), // This value is also used for radius calculation!
        TRUNK_UNEVEN_BOTTOM("Trunk.Uneven Bottom", false), // Can saplings/lowest trunks be on different Y?

        TOOL_LIST("Tool List", new ArrayList<>()),
        NATURAL_BLOCKS("Natural Blocks", new ArrayList<>()) // blocks that are okay to have around trees

         */
    }


    /**
     * This only works for 1x1 trunks, it checks in all directions !!
     * @param trunk
     * @param trunkBlocks
     * @param extraBlocks
     * @param naturalBlocks
     * @return
     */
    private static List<Block> getExtras(List<Block> trunk,
                                         List<Material> trunkBlocks, List<Material> extraBlocks, List<Material> naturalBlocks,
                                         TreeConfig config) {
        List<Block> result = new ArrayList<>();

        int radiusM = config.getInt(TreeConfig.CFG.BLOCKS_MIDDLE_RADIUS);

        boolean edgesM = config.getBoolean(TreeConfig.CFG.BLOCKS_MIDDLE_EDGES);
        boolean airM = config.getBoolean(TreeConfig.CFG.BLOCKS_MIDDLE_AIR);

        Block roof = null;

        BlockFace[] neighbors = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

        for (Block block : trunk) {
            roof = block;

            for (BlockFace face : neighbors) {
                if (isInvalid(result, block.getRelative(face), face, radiusM, 1, true, edgesM, airM, trunkBlocks, extraBlocks, naturalBlocks)) {
                    return null;
                }
            }
        }

        //TODO: check extras for proximity of other trunks?
        int radiusT = config.getInt(TreeConfig.CFG.BLOCKS_TOP_RADIUS);
        int heightT = config.getInt(TreeConfig.CFG.BLOCKS_TOP_HEIGHT);
        boolean airT = config.getBoolean(TreeConfig.CFG.BLOCKS_TOP_AIR);

        boolean edgesT = config.getBoolean(TreeConfig.CFG.BLOCKS_TOP_EDGES);

        for (int y=1; y<=heightT; y++) {
            Block checkBlock = roof.getRelative(0, y, 0);
            Material checkMaterial = checkBlock.getType();

            if (checkMaterial != Material.AIR) {
                if (extraBlocks.contains(checkMaterial)) {
                    result.add(checkBlock);

                    for (BlockFace face : neighbors) {
                        if (isInvalid(result, checkBlock.getRelative(face), face, radiusT, 1, true, edgesT, airT, trunkBlocks, extraBlocks, naturalBlocks)) {
                            return null;
                        }
                    }
                } else if (
                        !naturalBlocks.contains(checkMaterial) &&
                                !trunkBlocks.contains(checkMaterial) &&
                                !(allTrunks.contains(checkMaterial) || allExtras.contains(checkMaterial))) {
                    debug("Invalid block found: " + checkMaterial);
                    return null;
                }
            }
        }

        return result;
    }

    /**
     * Check for invalid going into a certain direction
     * @param list the list to add checked extra blocks to
     * @param checkBlock the currently checked block
     * @param direction the direction to go
     * @param expansion the total distance we have to travel
     * @param progress the progress travelling
     * @param trunkBlocks valid trunk block materials
     * @param extraBlocks vaild extra block materials
     * @param naturalBlocks valid natural block materials
     * @return if everything is fine. False if an invalid block was found
     */
    private static boolean isInvalid(List<Block> list, Block checkBlock, BlockFace direction,
                                     int expansion, int progress, boolean first, boolean edges, boolean air,
                                     List<Material> trunkBlocks, List<Material> extraBlocks, List<Material> naturalBlocks) {
        Material checkMaterial = checkBlock.getType();

        if (air || checkMaterial != Material.AIR) {
            boolean found = air;
            if (extraBlocks.contains(checkMaterial)) {
                list.add(checkBlock);
                found = true;
            }

            if (found) {
                if (first) {
                    boolean shorter = !edges && (progress == expansion);
                    if (direction == BlockFace.EAST || direction == BlockFace.WEST) {
                        // expand to north/south direction
                        if (isInvalid(list, checkBlock.getRelative(BlockFace.SOUTH), BlockFace.SOUTH, shorter ? progress - 1 : progress, 1, false, edges, air,
                                trunkBlocks, extraBlocks, naturalBlocks) ||
                                isInvalid(list, checkBlock.getRelative(BlockFace.NORTH), BlockFace.NORTH, progress, 1, false, edges, air,
                                        trunkBlocks, extraBlocks, naturalBlocks)){
                            return true;
                        }
                    } else {
                        // expand to east/west direction
                        if (isInvalid(list, checkBlock.getRelative(BlockFace.EAST), BlockFace.EAST, progress, 1, false, edges, air,
                                trunkBlocks, extraBlocks, naturalBlocks) ||
                                isInvalid(list, checkBlock.getRelative(BlockFace.WEST), BlockFace.WEST, progress, 1, false, edges, air,
                                        trunkBlocks, extraBlocks, naturalBlocks)){
                            return true;
                        }
                    }
                }
                // expand, if we can
                if (progress < expansion) {
                    if (isInvalid(list, checkBlock.getRelative(direction), direction, expansion, progress + 1, first, edges, air,
                            trunkBlocks, extraBlocks, naturalBlocks)) {
                        return true;
                    }
                }
            } else if (
                    !naturalBlocks.contains(checkMaterial) &&
                            !trunkBlocks.contains(checkMaterial) &&
                            !(allTrunks.contains(checkMaterial) || allExtras.contains(checkMaterial))) {
                debug("Invalid block found: " + checkMaterial);
                return true;
            }
        }
        return false;
    }

    private static List<Block> findTrunk(
            Block bottom,
            List<Material> trunkBlocks, List<Material> extraBlocks, List<Material> naturalBlocks,
            boolean trunkDiagonally) {
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

            // we hit the roof and no problems
            return result;
        }

        debug("We did not find a roof block. not a valid tree!");

        return null;
    }
}
