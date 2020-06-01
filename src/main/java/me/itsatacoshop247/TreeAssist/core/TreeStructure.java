package me.itsatacoshop247.TreeAssist.core;

import me.itsatacoshop247.TreeAssist.TreeAssistProtect;
import me.itsatacoshop247.TreeAssist.TreeAssistReplant;
import me.itsatacoshop247.TreeAssist.events.TASaplingReplaceEvent;
import me.itsatacoshop247.TreeAssist.events.TATreeBrokenEvent;
import me.itsatacoshop247.TreeAssist.externals.JobsHook;
import me.itsatacoshop247.TreeAssist.externals.mcMMOHook;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class TreeStructure {
    public static List<Material> allTrunks = new ArrayList<>();
    public static Debugger debug;
    static List<Material> allExtras = new ArrayList<>();

    public Block bottom;
    public List<Block> trunk;
    private List<Material> trunkBlocks;
    private List<Material> extraBlocks;
    private List<Material> naturalBlocks;
    private List<Material> groundBlocks;
    private List<Block> otherTrunks = new ArrayList<>();
    private Map<Block, List<Block>> branchMap;
    private List<Block> extras;
    private List<Block> roofs = new ArrayList<>();

    private final List<Block> checkedBlocks = new ArrayList<>();

    private boolean trunkDiagonally;
    private boolean valid = true;

    private TreeConfig config;
    private Block northWestBlock;
    private Block northEastBlock;
    private Block southWestBlock;
    private Block southEastBlock;

    public boolean isValid() {
        return valid;
    }

    private static void debug(String test) {
        //System.out.println(test);
        //TODO: remove
    }

    public TreeStructure(TreeConfig config, Block bottom, Boolean onlyTrunk) {
        this.config = config;
        this.bottom = bottom;
        trunkBlocks = config.getMaterials(TreeConfig.CFG.TRUNK_MATERIALS);

        // System.out.print("Trunk mats: ");
        // debugPrint(trunkBlocks);

        extraBlocks = config.getMaterials(TreeConfig.CFG.BLOCKS_MATERIALS);

        // System.out.print("Block mats: ");
        // debugPrint(extraBlocks);

        naturalBlocks = config.getMaterials(TreeConfig.CFG.NATURAL_BLOCKS);

        // System.out.print("Natural mats: ");
        // debugPrint(naturalBlocks);

        groundBlocks = config.getMaterials(TreeConfig.CFG.GROUND_BLOCKS);

        // System.out.print("Ground mats: ");
        // debugPrint(groundBlocks);

        trunkDiagonally = config.getBoolean(TreeConfig.CFG.TRUNK_DIAGONAL);

        int thickness = config.getInt(TreeConfig.CFG.TRUNK_THICKNESS);
        boolean branches = config.getBoolean(TreeConfig.CFG.TRUNK_BRANCH);

        if (thickness == 1) {

            // single trunk, that should be easy, right?
            trunk = findTrunk();

            if (trunk == null) {
                // no tree found, nothing to remove!
                valid = false;
                return;
            }
            debug("Tree of size " + trunk.size() + " found!");

            if (onlyTrunk) {
                // this will be used by other trees being broken checking OUR trunk for leaf distance and alike
                return;
            }

            if (trunk.size() > config.getInt(TreeConfig.CFG.TRUNK_MAXIMUM_HEIGHT)) {
                Utils.plugin.getLogger().warning("Higher than maximum!");
            } else if (trunk.size() < config.getInt(TreeConfig.CFG.TRUNK_MINIMUM_HEIGHT)) {
                Utils.plugin.getLogger().warning("Lower than minimum!");
            }

            otherTrunks = new ArrayList<>();

            branchMap = new HashMap<>();

            if (branches) {
                getBranches();

                debug.i("branch blocks: " + countBranches());
            }

            if (otherTrunks.isEmpty()) {
                debug.i("No other trunks found, checking again!");
                findOtherTrunks();
                if (!otherTrunks.isEmpty()) {
                    debug.i("Found other trunks: " + otherTrunks.size());
                }
            }

            getExtras();

            if (extras == null || (extras.size() < 10 && !isClose(otherTrunks) )) {
                valid = false;
                return;
            }
/*
            for (Block tree : trunk) {
                if (tree.getType().name().contains("GLASS") && trunkBlocks.contains(Material.GLASS)) {
                    tree.breakNaturally();
                } else {
                    tree.setType(Material.GLASS);

                }
            }

            for (Block leaf : extras) {
                if (leaf.getType().name().contains("GLASS") && trunkBlocks.contains(Material.GLASS)) {
                    leaf.breakNaturally();
                } else {
                    leaf.setType(Material.LIME_STAINED_GLASS);

                }
            }
            for (List<Block> blocks : branchMap.values()) {
                if (blocks != null) {
                    for (Block b : blocks) {
                        if (b.getType().name().contains("GLASS") && trunkBlocks.contains(Material.GLASS)) {
                            b.breakNaturally();
                        } else {
                            b.setType(Material.BROWN_STAINED_GLASS);
                        }
                    }
                }
            }

            bottom.setType(config.getMaterial(TreeConfig.CFG.REPLANTING_MATERIAL));
*/
            return;

        }

        if (thickness > 2) {

            // we have a big amount of blocks, so we need to be really edgy

            // west << X >> east

        /*
          north
          ^^^^^
            z
          vvvvv
          south

         */


            // check for other trunks around
            Material westMat = bottom.getRelative(BlockFace.WEST).getType();
            Material eastMat = bottom.getRelative(BlockFace.EAST).getType();
            Material northMat = bottom.getRelative(BlockFace.NORTH).getType();
            Material southMat = bottom.getRelative(BlockFace.SOUTH).getType();
            if (trunkBlocks.contains(westMat) || trunkBlocks.contains(eastMat) || trunkBlocks.contains(northMat) || trunkBlocks.contains(southMat)) {
                int found = 0;
                if (trunkBlocks.contains(westMat)) {
                    found++;
                }
                if (trunkBlocks.contains(eastMat)) {
                    found++;
                }
                if (trunkBlocks.contains(northMat)) {
                    found++;
                }
                if (trunkBlocks.contains(southMat)) {
                    found++;
                }

                if (found > 4) {

                    valid = false;
                    return;
                }
            }
            return;
        }
        // thickness 2

        Block checkBlock = config.getBoolean(TreeConfig.CFG.TRUNK_UNEVEN_BOTTOM) ? bottom.getRelative(BlockFace.UP, 2) : bottom;

        BlockFace[] faces = {BlockFace.NORTH,BlockFace.EAST,BlockFace.SOUTH,BlockFace.WEST,
                BlockFace.NORTH_EAST,BlockFace.SOUTH_EAST,BlockFace.NORTH_WEST,BlockFace.SOUTH_WEST};

        List<Block> trunks = new ArrayList<>();

        trunks.add(bottom);

        for (BlockFace face : faces) {
            Block nextBlock = checkBlock.getRelative(face);
            while (trunkBlocks.contains(nextBlock.getType())) {
                nextBlock = nextBlock.getRelative(BlockFace.DOWN);
            }
            if (groundBlocks.contains(nextBlock.getType())) {
                trunks.add(nextBlock.getRelative(BlockFace.UP));
            }
        }

        if (trunks.size() != 4) {
            debug.i("We do not have 4 trunks, we found " + trunks.size() + "!");
            valid = false;
            return;
        }

        trunk = findTrunks(trunks);

        if (trunk == null) {
            // no tree found, nothing to remove!
            valid = false;
            return;
        }
        debug("Tree of size " + trunk.size() + " found!");

        if (onlyTrunk) {
            // this will be used by other trees being broken checking OUR trunk for leaf distance and alike
            return;
        }

        if (trunk.size() > config.getInt(TreeConfig.CFG.TRUNK_MAXIMUM_HEIGHT)) {
            Utils.plugin.getLogger().warning("Higher than maximum!");
        } else if (trunk.size() < config.getInt(TreeConfig.CFG.TRUNK_MINIMUM_HEIGHT)) {
            Utils.plugin.getLogger().warning("Lower than minimum!");
        }

        otherTrunks = new ArrayList<>();

        branchMap = new HashMap<>();

        if (branches) {
            getBranches(trunks);

            debug.i("branch blocks: " + countBranches());
        }

        if (otherTrunks.isEmpty()) {
            debug.i("No other trunks found, checking again!");
            findOtherTrunks();
            if (!otherTrunks.isEmpty()) {
                debug.i("Found other trunks: " + otherTrunks.size());
            }
        }

        getExtras(trunks);

        if (extras == null || (extras.size() < 10 && !isClose(otherTrunks) )) {
            valid = false;
            return;
        }

        for (Block tree : trunk) {
            //tree.setType(Material.GLASS);
            tree.breakNaturally();
        }

        for (Block leaf : extras) {
            //leaf.setType(Material.LIME_STAINED_GLASS);
            leaf.breakNaturally();
        }
        for (List<Block> blocks : branchMap.values()) {
            if (blocks != null) {
                for (Block b : blocks) {
                    //b.setType(Material.BROWN_STAINED_GLASS);
                    b.breakNaturally();
                }
            }
        }

        for (Block block : trunks) {
            block.setType(config.getMaterial(TreeConfig.CFG.REPLANTING_MATERIAL));
        }

        return;
    }

    private boolean isClose(List<Block> otherTrunks) {
        int radius = config.getInt(TreeConfig.CFG.BLOCKS_MIDDLE_RADIUS);
        for (Block block : otherTrunks) {
            for (Block myBlock : trunk) {
                if (block.getLocation().distance(myBlock.getLocation()) <= radius) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<Block> getTrunk() {
        return trunk;
    }

    private List<Block> findTrunk() {
        List<Block> result = new ArrayList<>();

        Block checkBlock = bottom;

        while (debugContain(trunkBlocks, checkBlock.getType())) {
            result.add(checkBlock);

            checkBlock = checkBlock.getRelative(BlockFace.UP);

            if (trunkDiagonally && !debugContain(trunkBlocks, checkBlock.getType())) {
                Material checkMaterial = checkBlock.getType();

                debug("No more trunk going up at " + checkBlock.getLocation() + " - type: " + checkMaterial);

                if (!Utils.isAir(checkBlock.getType())) {
                    if (extraBlocks.contains(checkMaterial)) {
                        debug("It's an extra block!");
                    } else if (naturalBlocks.contains(checkMaterial)){
                        debug("It's a natural block!");
                    } else if (!allTrunks.contains(checkMaterial) && !allExtras.contains(checkMaterial)) {
                        debug("Unexpected block! Not a valid tree!");
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
                        debug("Checking diagonal at " + checkBlock.getRelative(x, 0, z).getLocation() + " - type: " + innerCheck);
                        if (!Utils.isAir(innerCheck)) {
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

    private List<Block> findTrunks(List<Block> trunks) {
        List<Block> result = new ArrayList<>();

        for (Block checkBlock : trunks) {

            while (debugContain(trunkBlocks, checkBlock.getType())) {
                result.add(checkBlock);

                checkBlock = checkBlock.getRelative(BlockFace.UP);
            }

            if (allExtras.contains(checkBlock.getType()) || Utils.isAir(checkBlock.getType())) {
                debug("We hit the roof!");

                // we hit the roof and no problems
                roofs.add(checkBlock);
                continue;
            }
            debug("We did not find a roof block. not a valid tree!");
            return null;
        }

        return result;
    }

    private boolean debugContain(List<Material> list, Material needle) {
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

    /**
     * Try to find other trunks that might have leaves interfering
     */
    private void findOtherTrunks() {
        int radius = config.getInt(TreeConfig.CFG.BLOCKS_MIDDLE_RADIUS) * 2;

        int totalChecks = 0;

        for (int x=-radius; x<=radius; x++) {
            blocks: for (int z=-radius; z<= radius; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }

                Block checkBlock = bottom.getWorld().getHighestBlockAt(bottom.getX() + x, bottom.getZ() + z);
                int y = checkBlock.getY();
                while (y > 1) {
                    totalChecks++;
                    checkBlock = checkBlock.getRelative(BlockFace.DOWN);

                    if (trunkBlocks.contains(checkBlock.getType())) {
                        Block block = TreeCalculator.validate(checkBlock, config);
                        if (block != null && !block.equals(bottom)) {
                            TreeStructure trunk = new TreeStructure(config, block, true);
                            if (trunk.isValid()) {
                                otherTrunks.addAll(trunk.getTrunk());
                                debug.i("Found another tree at " + block.getLocation());
                                continue blocks;
                            }
                        }
                    } else if (groundBlocks.contains(checkBlock.getType())) {
                        continue blocks;
                    }
                    y--;
                }
            }
        }
        debug.i("total checks: " + totalChecks);
    }

    private int countBranches() {
        int result = 0;
        for (List<Block> list : branchMap.values()) {
            if (list != null) {
                result += list.size();
            }
        }
        return result;
    }

    private void getBranches(List<Block> trunks) {

        boolean first = true;

        calculateBottoms(trunks);

        List<Block> checkRoofs = new ArrayList<>();
        checkRoofs.addAll(roofs);

        for (Block block : checkRoofs) {
            BlockFace[] directions = getFaces(block);
            for (BlockFace face : directions) {
                Block checkBlock = block.getRelative(face);

                List<Block> branch = new ArrayList<>();
                if (!invalidBranch(checkBlock, branch, face)) {
                    branchMap.put(checkBlock, branch);
                } else {
                    checkBlock = checkBlock.getRelative(BlockFace.UP);
                    if (!invalidBranch(checkBlock, branch, face)) {
                        branchMap.put(checkBlock, branch);
                    }
                }
            }
        }

        for (Block block : trunk) {
            if (first) {
                first = false;
                continue;
            }

            BlockFace[] directions = getFaces(block);

            for (BlockFace face : directions) {
                Block checkBlock = block.getRelative(face);
                if (trunkBlocks.contains(checkBlock.getType())) {
                    List<Block> branch = new ArrayList<>();
                    if (invalidBranch(checkBlock, branch, face)) {
                        trunk.clear();
                        branchMap.clear();
                        return;
                    }
                    branchMap.put(checkBlock, branch);
                } else if (!allExtras.contains(checkBlock.getType())
                        && !allTrunks.contains(checkBlock.getType())
                        && !naturalBlocks.contains(checkBlock.getType())) {
                    debug.i("invalid block 2: " + checkBlock.getType());
                    trunk.clear();
                    branchMap.clear();
                    return;
                } else {
                    // this branch ends here
                    roofs.add(checkBlock);
                }
            }
        }
    }

    static Map<BlockFace, BlockFace[]> continuations = new EnumMap<>(BlockFace.class);

    static {
        continuations.put(BlockFace.EAST, new BlockFace[]{BlockFace.EAST});
        continuations.put(BlockFace.NORTH, new BlockFace[]{BlockFace.NORTH});
        continuations.put(BlockFace.SOUTH, new BlockFace[]{BlockFace.SOUTH});
        continuations.put(BlockFace.WEST, new BlockFace[]{BlockFace.WEST});

        continuations.put(BlockFace.NORTH_EAST, new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.NORTH_EAST});
        continuations.put(BlockFace.NORTH_WEST, new BlockFace[]{BlockFace.NORTH, BlockFace.WEST, BlockFace.NORTH_WEST});
        continuations.put(BlockFace.SOUTH_EAST, new BlockFace[]{BlockFace.SOUTH, BlockFace.EAST, BlockFace.SOUTH_EAST});
        continuations.put(BlockFace.SOUTH_WEST, new BlockFace[]{BlockFace.SOUTH, BlockFace.WEST, BlockFace.SOUTH_WEST});
    }

    private boolean calculateBottoms(List<Block> bottoms) {

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;

        for (Block block : bottoms) {
            if (block.getX() <= minX) {
                if (block.getZ() <= minZ) {
                    northWestBlock = block;
                    minX = block.getX();
                    minZ = block.getZ();
                }
                if (block.getZ() >= maxZ) {
                    southWestBlock = block;
                    minX = block.getX();
                    maxZ = block.getZ();
                }
            }
            if (block.getX() >= maxX) {
                if (block.getZ() <= minZ) {
                    northEastBlock = block;
                    maxX = block.getX();
                    minZ = block.getZ();
                }
                if (block.getZ() >= maxZ) {
                    southEastBlock = block;
                    maxX = block.getX();
                    maxZ = block.getZ();
                }
            }
        }

        Set<Block> set = new HashSet<>();

        set.add(northWestBlock);
        set.add(southWestBlock);
        set.add(northEastBlock);
        set.add(southEastBlock);

        return set.size() == 4;
    }

    private BlockFace[] getFaces(Block checkBlock) {
        if (
                checkBlock.getX() == northWestBlock.getX() &&
                checkBlock.getZ() == northWestBlock.getZ()) {
            return continuations.get(BlockFace.NORTH_WEST);
        }
        if (
                checkBlock.getX() == northEastBlock.getX() &&
                checkBlock.getZ() == northEastBlock.getZ()) {
            return continuations.get(BlockFace.NORTH_EAST);
        }
        if (
                checkBlock.getX() == southWestBlock.getX() &&
                checkBlock.getZ() == southWestBlock.getZ()) {
            return continuations.get(BlockFace.SOUTH_WEST);
        }
        if (
                checkBlock.getX() == southEastBlock.getX() &&
                checkBlock.getZ() == southEastBlock.getZ()) {
            return continuations.get(BlockFace.SOUTH_EAST);
        }

        return new BlockFace[0];
    }

    private void getBranches() {

        boolean first = true;

        BlockFace[] directions = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH_WEST, BlockFace.SOUTH_EAST, BlockFace.NORTH_EAST, BlockFace.SOUTH_WEST};


        for (Block block : trunk) {
            if (first) {
                first = false;
                continue;
            }

            for (BlockFace face : directions) {
                Block checkBlock = block.getRelative(face);
                if (trunkBlocks.contains(checkBlock.getType())) {
                    List<Block> branch = new ArrayList<>();
                    for (BlockFace innerface : continuations.get(face)) {
                        if (invalidBranch(checkBlock, branch, innerface)) {
                            trunk.clear();
                            branchMap.clear();
                            return;
                        }
                    }
                    branchMap.put(checkBlock, branch);
                } else if (!allExtras.contains(checkBlock.getType())
                        && !allTrunks.contains(checkBlock.getType())
                        && !naturalBlocks.contains(checkBlock.getType())) {
                    debug.i("invalid block 2: " + checkBlock.getType());
                    trunk.clear();
                    branchMap.clear();
                    return;
                }
            }
        }
    }

    /**
     * Check a given block whether it is part of a branch
     * @param checkBlock the block to start checking
     * @param result the resulting branch List
     * @param direction the direction the branch would start
     * @return whether something went wrong
     */
    private boolean invalidBranch(Block checkBlock, List<Block> result, BlockFace direction) {
        if (checkedBlocks.contains(checkBlock)) {
            return false;
        }
        checkedBlocks.add(checkBlock);
        BlockFace[] diagonals = validDiagonals.get(direction);

        if (diagonals == null) {
            diagonals = new BlockFace[]{direction};
        }

        Material mat = checkBlock.getType();
        if (trunkBlocks.contains(mat)) {
            // check for other trunks
            Block otherBlock = TreeCalculator.validate(checkBlock, config);

            if (otherBlock != null) {
                TreeStructure anotherTree = new TreeStructure(config, otherBlock, true);
                if (anotherTree.isValid() && !anotherTree.bottom.equals(bottom)) {
                    // this is another trunk! check somewhere else!
                    otherTrunks.addAll(anotherTree.getTrunk());
                    debug.i("We hit a neighbor tree! Our bottom block " + bottom.getLocation() + " is not the same as " + anotherTree.bottom.getLocation());
                    return false;
                }
            }
            result.add(checkBlock);
            for (BlockFace face : diagonals) {
                debug.i("Continuing branch " + direction + " to " + face);
                if (
                        invalidBranch(checkBlock.getRelative(face), result, direction) ||
                                invalidBranch(checkBlock.getRelative(face).getRelative(BlockFace.UP), result, direction) ||
                                invalidBranch(checkBlock.getRelative(face).getRelative(BlockFace.DOWN), result, direction)) {
                    result.clear();
                    return true;
                }
            }
            if (invalidBranch(checkBlock.getRelative(BlockFace.UP), result, direction)) {
                result.clear();
                return true;
            }
        } else if (extraBlocks.contains(mat)) {
            debug.i("This branch ends now.");
            roofs.add(checkBlock);
            //we found our end!
            return false;
        } else if (
                !naturalBlocks.contains(mat) &&
                        !(allTrunks.contains(mat) || allExtras.contains(mat))) {
            debug.i("invalid block 3: " + mat);
            return true;
        }
        return false;
    }

    private final static Map<BlockFace, BlockFace[]> validDiagonals = new EnumMap<>(BlockFace.class);

    static {
        validDiagonals.put(BlockFace.NORTH,
                new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST});
        validDiagonals.put(BlockFace.SOUTH,
                new BlockFace[] {BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST});

        validDiagonals.put(BlockFace.EAST,
                new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.NORTH_EAST, BlockFace.SOUTH_EAST, BlockFace.EAST});
        validDiagonals.put(BlockFace.WEST,
                new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.NORTH_WEST, BlockFace.SOUTH_WEST, BlockFace.WEST});

        validDiagonals.put(BlockFace.NORTH_EAST,
                new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.NORTH_EAST});
        validDiagonals.put(BlockFace.NORTH_WEST,
                new BlockFace[] {BlockFace.NORTH, BlockFace.WEST, BlockFace.NORTH_WEST});
        validDiagonals.put(BlockFace.SOUTH_EAST,
                new BlockFace[] {BlockFace.SOUTH, BlockFace.EAST, BlockFace.SOUTH_EAST});
        validDiagonals.put(BlockFace.SOUTH_WEST,
                new BlockFace[] {BlockFace.SOUTH, BlockFace.WEST, BlockFace.SOUTH_WEST});
    }

    /**
     * Checking only in trunk facing directions
     */
    private void getExtras(List<Block> trunks) {
        extras = new ArrayList<>();

        int radiusM = config.getInt(TreeConfig.CFG.BLOCKS_MIDDLE_RADIUS);

        boolean edgesM = config.getBoolean(TreeConfig.CFG.BLOCKS_MIDDLE_EDGES);
        boolean airM = config.getBoolean(TreeConfig.CFG.BLOCKS_MIDDLE_AIR);

        BlockFace[] neighbors = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

        if (roofs.size() <= 0) {
            debug("No more blocks found!");
            valid = false;
            return;
        }

        int radiusT = config.getInt(TreeConfig.CFG.BLOCKS_TOP_RADIUS);
        int heightT = config.getInt(TreeConfig.CFG.BLOCKS_TOP_HEIGHT);
        boolean airT = config.getBoolean(TreeConfig.CFG.BLOCKS_TOP_AIR);

        boolean edgesT = config.getBoolean(TreeConfig.CFG.BLOCKS_TOP_EDGES);

        for (Block roof : roofs) {

            for (int y = 0; y <= heightT; y++) {
                Block checkBlock = roof.getRelative(0, y, 0);
                Material checkMaterial = checkBlock.getType();

                if (!Utils.isAir(checkMaterial)) {
                    if (extraBlocks.contains(checkMaterial)) {
                        extras.add(checkBlock);

                        for (BlockFace face : neighbors) {
                            if (isInvalid(checkBlock.getRelative(face), face, radiusT, 1, true, edgesT, airT, radiusT)) {
                                valid = false;
                                return;
                            }
                        }
                    } else if (
                            !naturalBlocks.contains(checkMaterial) &&
                                    !trunkBlocks.contains(checkMaterial) &&
                                    !(allTrunks.contains(checkMaterial) || allExtras.contains(checkMaterial))) {
                        debug("Invalid block found: " + checkMaterial);
                        valid = false;
                        return;
                    }
                }
            }
        }
    }

    /**
     * This only works for 1x1 trunks, it checks in all directions !!
     */
    private void getExtras() {
        extras = new ArrayList<>();

        int radiusM = config.getInt(TreeConfig.CFG.BLOCKS_MIDDLE_RADIUS);

        boolean edgesM = config.getBoolean(TreeConfig.CFG.BLOCKS_MIDDLE_EDGES);
        boolean airM = config.getBoolean(TreeConfig.CFG.BLOCKS_MIDDLE_AIR);

        Block roof = null;

        BlockFace[] neighbors = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

        for (Block block : trunk) {
            roof = block;

            for (BlockFace face : neighbors) {
                if (isInvalid(block.getRelative(face), face, radiusM, 1, true, edgesM, airM, radiusM)) {
                    valid = false;
                    return;
                }
            }
        }

        if (roof == null) {
            debug("No more blocks found!");
            valid = false;
            return;
        }

        int radiusT = config.getInt(TreeConfig.CFG.BLOCKS_TOP_RADIUS);
        int heightT = config.getInt(TreeConfig.CFG.BLOCKS_TOP_HEIGHT);
        boolean airT = config.getBoolean(TreeConfig.CFG.BLOCKS_TOP_AIR);

        boolean edgesT = config.getBoolean(TreeConfig.CFG.BLOCKS_TOP_EDGES);

        for (int y=0; y<=heightT; y++) {
            Block checkBlock = roof.getRelative(0, y, 0);
            Material checkMaterial = checkBlock.getType();

            if (!Utils.isAir(checkMaterial)) {
                if (extraBlocks.contains(checkMaterial)) {
                    extras.add(checkBlock);

                    for (BlockFace face : neighbors) {
                        if (isInvalid(checkBlock.getRelative(face), face, radiusT, 1, true, edgesT, airT, radiusT)) {
                            valid = false;
                            return;
                        }
                    }
                } else if (
                        !naturalBlocks.contains(checkMaterial) &&
                                !trunkBlocks.contains(checkMaterial) &&
                                !(allTrunks.contains(checkMaterial) || allExtras.contains(checkMaterial))) {
                    debug("Invalid block found: " + checkMaterial);
                    valid = false;
                    return;
                }
            }
        }
    }

    /**
     * Check for invalid going into a certain direction
     * @param checkBlock the currently checked block
     * @param direction the direction to go
     * @param expansion the total distance we have to travel
     * @param progress the progress travelling
     * @param first is this the initial direction?
     * @param edges are we expecting edge blocks?
     * @param air are we allowing air?
     * @param radius what is the radius of similar trees to check for?
     * @return whether everything is fine. False if an invalid block was found
     */
    private boolean isInvalid(Block checkBlock, BlockFace direction,
                                     int expansion, int progress, boolean first, boolean edges, boolean air, int radius) {
        for (Block otherBlock : otherTrunks) {
            if (otherBlock.getLocation().distance(checkBlock.getLocation()) <= radius) {
                // we are not invalid but we want to NOT add this block and not continue searching for extras
                return false;
            }
        }
        Material checkMaterial = checkBlock.getType();

        if (air || !Utils.isAir(checkMaterial)) {
            boolean found = air;
            if (extraBlocks.contains(checkMaterial)) {
                extras.add(checkBlock);
                found = true;
            }

            if (found) {
                if (first) {
                    boolean shorter = !edges && (progress == expansion);
                    if (direction == BlockFace.EAST || direction == BlockFace.WEST) {
                        // expand to north/south direction
                        if (isInvalid(checkBlock.getRelative(BlockFace.SOUTH), BlockFace.SOUTH, shorter ? progress - 1 : progress, 1, false, edges, air, radius
                                ) ||
                                isInvalid(checkBlock.getRelative(BlockFace.NORTH), BlockFace.NORTH, progress, 1, false, edges, air, radius
                                        )){
                            return true;
                        }
                    } else {
                        // expand to east/west direction
                        if (isInvalid(checkBlock.getRelative(BlockFace.EAST), BlockFace.EAST, progress, 1, false, edges, air, radius
                                ) ||
                                isInvalid(checkBlock.getRelative(BlockFace.WEST), BlockFace.WEST, progress, 1, false, edges, air, radius
                                        )){
                            return true;
                        }
                    }
                }
                // expand, if we can
                if (progress < expansion) {
                    return isInvalid(checkBlock.getRelative(direction), direction, expansion, progress + 1, first, edges, air, radius
                            );
                }
            } else if (
                    !naturalBlocks.contains(checkMaterial) &&
                            !trunkBlocks.contains(checkMaterial) &&
                            !(allTrunks.contains(checkMaterial) || allExtras.contains(checkMaterial))) {
                debug("Invalid block found 1: " + checkMaterial);
                return true;
            }
        }
        return false;
    }

    public void maybeReplant(Player player, Block block) {
        if (!config.getBoolean(TreeConfig.CFG.REPLANTING_ACTIVE)) {
            debug.i("replanting is disabled!");
            return;
        }

        Config globalConfig = Utils.plugin.getTreeAssistConfig();

        if (!config.getBoolean(TreeConfig.CFG.REPLANTING_ENFORCE)) {

            if (Utils.plugin.getListener().isNoReplace(player.getName())) {
                debug.i("Player is NoReplace!");
                return;
            }

            if (globalConfig.getBoolean(Config.CFG.MAIN_USE_PERMISSIONS) &&
                !player.hasPermission("treeassist.replant")) {
                debug.i("Player has no replant perms!");
                return;
            }

            if (config.getBoolean(TreeConfig.CFG.REPLANTING_REQUIRES_TOOLS)) {
                if (!Utils.isRequiredTool(player.getInventory().getItemInMainHand(), config)) {
                    debug.i("Player does not have the tool!!");
                    return;
                }
            }
        }
        if (!(block.equals(bottom) ||
            block.equals(northWestBlock) ||
            block.equals(southWestBlock) ||
            block.equals(northEastBlock) ||
            block.equals(southEastBlock)
            )) {
            if (globalConfig.getBoolean(Config.CFG.SAPLING_REPLANT_BOTTOM_BLOCK_HAS_TO_BE_BROKEN_FIRST)) {
                debug.i("We did not break the bottom!");
                return;
            }
        }

        debug.i("we are replacing now!");

        int delay = Math.max(1, config.getInt(TreeConfig.CFG.REPLANTUNG_DELAY));

        Material saplingMat = config.getMaterial(TreeConfig.CFG.REPLANTING_MATERIAL);

        List<Block> bottoms = new ArrayList<>();

        if (bottom != null) {
            bottoms.add(bottom);
        }
        if (northWestBlock != null) {
            bottoms.add(northWestBlock);
        }
        if (southWestBlock != null) {
            bottoms.add(southWestBlock);
        }
        if (northEastBlock != null) {
            bottoms.add(northEastBlock);
        }
        if (southEastBlock != null) {
            bottoms.add(southEastBlock);
        }

        for (Block saplingBlock : bottoms) {

            TASaplingReplaceEvent replaceEvent = new TASaplingReplaceEvent(saplingBlock, saplingMat.name());
            Utils.plugin.getServer().getPluginManager().callEvent(replaceEvent);

            if (replaceEvent.isCancelled()) {
                debug.i("Sapling replace was cancelled by a plugin!");
                continue;
            }

            Runnable b = new TreeAssistReplant(Utils.plugin, saplingBlock, saplingMat);
            Utils.plugin.getServer().getScheduler()
                    .scheduleSyncDelayedTask(Utils.plugin, b, 20 * delay);
            int timeToProtect = globalConfig.getInt(Config.CFG.SAPLING_REPLANT_TIME_TO_PROTECT_SAPLING);
            if (timeToProtect > 0) {
                Utils.plugin.saplingLocationList.add(saplingBlock.getLocation());
                Runnable X = new TreeAssistProtect(Utils.plugin, saplingBlock.getLocation());

                Utils.plugin
                        .getServer()
                        .getScheduler()
                        .scheduleSyncDelayedTask(
                                Utils.plugin,
                                X,20 * timeToProtect);
            }
        }
    }

    public void removeLater(Player player, ItemStack playerTool) {
        boolean damage = config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_APPLY_FULL_TOOL_DAMAGE);

        debug.i("Removing The Tree!");


        final int delay = config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_INITIAL_DELAY) ? config.getInt(
                TreeConfig.CFG.AUTOMATIC_DESTRUCTION_INITIAL_DELAY_TIME) * 20 : 0;
        final int offset = config.getInt(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_DELAY);

        debug.i("delay: " + delay +"; offset: " + offset);

        final ItemStack tool = (damage && player != null && player.getGameMode() != GameMode.CREATIVE) ? playerTool
                : null;

        Material sapling = config.getMaterial(TreeConfig.CFG.REPLANTING_MATERIAL);

        if (player != null) {
            Utils.plugin.setCoolDown(player, config, trunk);
        }

        final boolean statPickup = config.getBoolean(TreeConfig.CFG.BLOCK_STATISTICS_PICKUP);
        final boolean statMineBlock = config.getBoolean(TreeConfig.CFG.BLOCK_STATISTICS_MINE_BLOCK);

        debug.i("pickup: " + statPickup + "; mine: " + statMineBlock);

        final List<Block> removeBlocks = new ArrayList<>();

        if (config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_REMOVE_LEAVES)) {
            for (Block block : extras) {
                Utils.plugin.getListener().breakRadiusLeaves(block);
                break;
            }
            removeBlocks.addAll(extras);
        }
        removeBlocks.addAll(trunk);

        class InstantRunner extends BukkitRunnable {
            boolean fastDecaying = false;
            @Override
            public void run() {
                if (offset < 0) {
                    for (Block block : removeBlocks) {
                        if (sapling.equals(block.getType())) {
                            debug.i("InstantRunner: skipping breaking a sapling");
                            continue;
                        }
                        if (tool == null) {

                            debug.i("tool null 1");
                            TATreeBrokenEvent event = new TATreeBrokenEvent(block, player, tool);
                            Utils.plugin.getServer().getPluginManager().callEvent(event);
                            if (!event.isCancelled()) {
                                Utils.plugin.blockList.logBreak(block, player);

                                callExternals(block, player);

                                if (Utils.isLog(block.getType())
                                        && config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_AUTO_ADD_TO_INVENTORY)) {
                                    player.getInventory().addItem(block.getState().getData().toItemStack(1));
                                    block.setType(Material.AIR);

                                    if (config.getBoolean(TreeConfig.CFG.BLOCK_STATISTICS_MINE_BLOCK)) {
                                        player.incrementStatistic(Statistic.MINE_BLOCK, block.getType());
                                    }

                                    debug.i("added to inventory");
                                } else {
                                    Utils.breakBlock(player, block);

                                    debug.i("breaking!");
                                }
                                player.sendBlockChange(block.getLocation(), Material.AIR.createBlockData());
                            }
                        } else {
                            debug.i("InstantRunner: 1");
                            breakBlock(block, tool, player, statPickup, statMineBlock);
                            if (tool.getType().getMaxDurability() > 0 && tool.getDurability() == tool.getType().getMaxDurability()) {

                                debug.i("removing item: " + player.getItemInHand().getType().name() +
                                        " (durability " + tool.getDurability() + "==" + tool.getType().getMaxDurability());
                                player.getInventory().remove(tool);
                                this.cancel();
                            }
                        }
                    }
                    removeBlocks.clear();
                } else {
                    for (Block block : removeBlocks) {
                        if (sapling.equals(block.getType())) {
                            debug.i("InstantRunner: skipping breaking a sapling");
                            continue;
                        }
                        if (block.getType() == Material.AIR) {
                            debug.i("InstantRunner: 2a " + Debugger.parse(block.getLocation()));
                        } else {
                            if (tool == null) {

                                debug.i("tool null 2");

                                TATreeBrokenEvent event = new TATreeBrokenEvent(block, player, tool);
                                Utils.plugin.getServer().getPluginManager().callEvent(event);
                                if (!event.isCancelled()) {

                                    callExternals(block, player);

                                    Utils.plugin.blockList.logBreak(block, player);
                                    if (Utils.isLog(block.getType())
                                            && config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_AUTO_ADD_TO_INVENTORY)) {
                                        player.getInventory().addItem(block.getState().getData().toItemStack(1));
                                        block.setType(Material.AIR);
                                    } else {
                                        Utils.breakBlock(player, block);
                                    }
                                    player.sendBlockChange(block.getLocation(), Material.AIR.createBlockData());
                                }
                            } else {
                                debug.i("InstantRunner: 2b");
                                breakBlock(block, tool, player, statPickup, statMineBlock);
                                if (tool.getType().getMaxDurability() > 0 && tool.getDurability() == tool.getType().getMaxDurability()) {
                                    debug.i("removing item: " + player.getItemInHand().getType().name() +
                                            " (durability " + tool.getDurability() + "==" + tool.getType().getMaxDurability());
                                    player.getInventory().remove(tool);
                                    this.cancel();
                                }
                            }
                        }
                        removeBlocks.remove(block);
                        return;
                    }
                }
                try {
                    this.cancel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        if (player != null) {
            (new InstantRunner()).runTaskTimer(Utils.plugin, delay, offset);
        }

        class CleanRunner extends BukkitRunnable {
            private final TreeStructure me;
            private final List<Block> totalBlocks = new ArrayList<>();

            CleanRunner(TreeStructure tree) {
                me = tree;
                if (config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_REMOVE_LEAVES)) {
                    for (Block block : extras) {
                        Utils.plugin.getListener().breakRadiusLeaves(block);
                        break;
                    }
                    totalBlocks.addAll(extras);
                }
                totalBlocks.addAll(trunk);
            }

            @Override
            public void run() {
                if (offset < 0) {
                    for (Block block : totalBlocks) {
                        if (sapling.equals(block.getType())) {
                            debug.i("CleanRunner: skipping breaking a sapling");
                            continue;
                        }
                        debug.i("CleanRunner: 1");
                        Utils.breakBlock(block);
                    }
                    removeBlocks.clear();
                } else {
                    for (Block block : totalBlocks) {
                        if (sapling.equals(block.getType())) {
                            debug.i("CleanRunner: skipping breaking a sapling");
                            continue;
                        }
                        debug.i("CleanRunner: 2");
                        Utils.breakBlock(block);
                        totalBlocks.remove(block);
                        return;
                    }
                }

                me.valid = false;
                try {
                    this.cancel();
                } catch (Exception e) {

                }
            }

        }

        int cleanDelay = config.getInt(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_CLEANUP_DELAY_TIME);

        (new CleanRunner(this)).runTaskTimer(Utils.plugin, cleanDelay *20L, offset);
    }

    private void breakBlock(Block block, ItemStack tool, Player player, boolean statPickup, boolean statMineBlock) {

        if ((tool != null) && (tool.getDurability() > tool.getType().getMaxDurability())) return;

        TATreeBrokenEvent event = new TATreeBrokenEvent(block, player, tool);
        Utils.plugin.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) return;

        boolean leaf = allExtras.contains(block.getType());
        Material maat = block.getType();
        byte data = block.getState().getData().getData();

        debug.i("breaking " + block.getType() +". leaf: " + leaf);

        callExternals(block, player);


        if (leaf) {
            int chance = config.getYamlConfiguration().getInt(TreeConfig.CFG.CUSTOM_DROP_CHANCE + "." + tool.getType().name(), 0);


            if (chance > 99 || (new Random()).nextInt(100) < chance) {
                debug.i("dropping custom drop!");
                ConfigurationSection cs = config.getYamlConfiguration().getConfigurationSection(TreeConfig.CFG.CUSTOM_DROPS.getNode());

                debug.i("custom drop count: " + cs.getKeys(false).size());

                for (String key : cs.getKeys(false)) {
                    int customChance = (int) (cs.getDouble(key, 0.0d) * 100000d);

                    if ((new Random()).nextInt(100000) < customChance) {
                        debug.i("dropping: " + key);

                        try {
                            Material mat = Material.matchMaterial(key.toUpperCase());
                            debug.i(">2 : " + mat.name());
                            block.getWorld()
                                    .dropItemNaturally(block.getLocation(),
                                            new ItemStack(mat));
                        } catch (Exception e) {
                            Utils.plugin.getLogger().warning(
                                    "Invalid config value: Custom Drops."
                                            + key
                                            + " is not a valid Material!");
                        }

                    }
                }
            }
        } else {
            debug.i("mat: " + maat.name());
            debug.i("data: " + data);
        }
        Utils.plugin.blockList.logBreak(block, player);

        if (player != null && statMineBlock) {
            player.incrementStatistic(Statistic.MINE_BLOCK, block.getType());
        }

        if (player != null && Utils.isLog(block.getType())
                && config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_AUTO_ADD_TO_INVENTORY)) {
            if (statPickup) {
                player.incrementStatistic(Statistic.PICKUP, block.getType());
            }
            player.getInventory().addItem(block.getState().getData().toItemStack(1));
            block.setType(Material.AIR);
        } else {
            if (tool != null && tool.hasItemMeta() && tool.getItemMeta().getEnchants().containsKey(Enchantment.SILK_TOUCH)
                    && Utils.isMushroom(block.getType())) {
                Material mat = block.getType();
                block.setType(Material.AIR);
                block.getWorld().dropItemNaturally(
                        block.getLocation(),
                        new ItemStack(mat, 1));
                if (config.getBoolean(TreeConfig.CFG.BLOCK_STATISTICS_MINE_BLOCK)) {
                    player.incrementStatistic(Statistic.MINE_BLOCK, block.getType());
                }
            } else {
                Utils.breakBlock(player, block, tool);
            }
        }
        if (player != null) {
            player.sendBlockChange(block.getLocation(), Material.AIR.createBlockData());
        }

        if (!leaf && tool != null && player != null) {
            if (tool.containsEnchantment(Enchantment.DURABILITY)) {
                int damageChance = (int) (100d / ((double) tool
                        .getEnchantmentLevel(Enchantment.DURABILITY) + 1d));

                int random = new Random().nextInt(100);

                if (random >= damageChance) {
                    return; // nodamage -> out!
                }
            }

            int ench = 100;

            if (tool.getEnchantments().containsKey(Enchantment.DURABILITY)) {
                ench = 100 / (tool.getEnchantmentLevel(Enchantment.DURABILITY) + 1);
            }

            if ((new Random()).nextInt(100) > ench) {
                return; // no damage
            }

            if (config.getMaterials(TreeConfig.CFG.TOOL_LIST).contains(tool.getType())) {
                ItemMeta meta = tool.getItemMeta();
                if (meta != null) {
                    ((Damageable)meta).setDamage(((Damageable)meta).getDamage() + 1);
                    tool.setItemMeta(meta);
                }
            } else if (Utils.isVanillaTool(tool)) {
                ItemMeta meta = tool.getItemMeta();
                if (meta != null) {
                    ((Damageable)meta).setDamage(((Damageable)meta).getDamage() + 2);
                    tool.setItemMeta(meta);
                }
            }
        }
    }

    public static void callExternals(Block block, Player player) {

        boolean leaf = TreeStructure.allExtras.contains(block.getType());

        if (!leaf && player != null) {
            if (Utils.plugin.mcMMO) {
                TreeStructure.debug.i("Adding mcMMO EXP!");
                mcMMOHook.mcMMOaddExp(player, block);
            }

            if (Utils.plugin.jobs) {
                debug.i("Adding Jobs EXP!");
                JobsHook.addJobsExp(player, block);
            }
        } else {
            debug.i("mcMMO: " + Utils.plugin.mcMMO);
            debug.i("jobs: " + Utils.plugin.jobs);
            debug.i("player: " + String.valueOf(player));
        }
    }

    public void clearUpTo(Block block) {
        List<Block> removals = new ArrayList<>();
        for (Block b : trunk) {
            if (b.getY() <= block.getY()) {
                removals.add(b);
            }
        }
        trunk.removeAll(removals);
        removals.clear();
        for (Block b : extras) {
            if (b.getY() <= block.getY()) {
                removals.add(b);
            }
        }
        extras.removeAll(removals);
    }
}
