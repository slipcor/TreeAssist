package me.itsatacoshop247.TreeAssist.core;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.*;

public class TreeStructure {
    public static List<Material> allTrunks = new ArrayList<>();
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
        System.out.println(test);
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

                System.out.println("branch blocks: " + countBranches());
            }

            if (otherTrunks.isEmpty()) {
                System.out.println("No other trunks found, checking again!");
                findOtherTrunks();
                if (!otherTrunks.isEmpty()) {
                    System.out.println("Found other trunks: " + otherTrunks.size());
                }
            }

            getExtras();

            if (extras == null || (extras.size() < 10 && !isClose(otherTrunks) )) {
                valid = false;
                return;
            }

            for (Block tree : trunk) {
                tree.breakNaturally();
            }

            for (Block leaf : extras) {
                leaf.breakNaturally();
            }
            for (List<Block> blocks : branchMap.values()) {
                if (blocks != null) {
                    for (Block b : blocks) {
                        b.breakNaturally();
                    }
                }
            }

            bottom.setType(Material.matchMaterial(config.getString(TreeConfig.CFG.REPLANT)));

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

                if (found > 4)


                    System.out.println("We ignore thick ones / farms for now!");
                valid = false;
                return;
            }
            return;
        }
        // thickness 2

        Block checkBlock = config.getBoolean(TreeConfig.CFG.TRUNK_UNEVEN_BOTTOM) ? bottom.getRelative(BlockFace.UP) : bottom;

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
            System.out.println("We do not have 4 trunks, we found " + trunks.size() + "!");
            valid = false;
        }

        // single trunk, that should be easy, right?
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

            System.out.println("branch blocks: " + countBranches());
        }

        if (otherTrunks.isEmpty()) {
            System.out.println("No other trunks found, checking again!");
            findOtherTrunks();
            if (!otherTrunks.isEmpty()) {
                System.out.println("Found other trunks: " + otherTrunks.size());
            }
        }

        getExtras(trunks);

        if (extras == null || (extras.size() < 10 && !isClose(otherTrunks) )) {
            valid = false;
            return;
        }

        for (Block tree : trunk) {
            tree.breakNaturally();
        }

        for (Block leaf : extras) {
            leaf.breakNaturally();
        }
        for (List<Block> blocks : branchMap.values()) {
            if (blocks != null) {
                for (Block b : blocks) {
                    b.breakNaturally();
                }
            }
        }

        for (Block block : trunks) {
            block.setType(Material.matchMaterial(config.getString(TreeConfig.CFG.REPLANT)));
        }

        return;


        /*

        BLOCKS_CAP_HEIGHT("Blocks.Cap.Height", 2), // Branch Topping Leaves Height
        BLOCKS_CAP_RADIUS("Blocks.Cap.Radius", 3), // Branch Topping Leaves Radius

        TRUNK_RADIUS("Trunk.Radius", 1),
        TRUNK_EDGES("Trunk.Edges", false), // Trunk can have extra on the edges (Dark Oak)
        TRUNK_THICKNESS("Trunk.Thickness", 1), // This value is also used for radius calculation!
        TRUNK_UNEVEN_BOTTOM("Trunk.Uneven Bottom", false), // Can saplings/lowest trunks be on different Y?

        TOOL_LIST("Tool List", new ArrayList<>()),
        NATURAL_BLOCKS("Natural Blocks", new ArrayList<>()) // blocks that are okay to have around trees

         */
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

                blockLoop: for (int x = -1; x < 2; x++) {
                    for (int z= -1; z < 2; z++) {
                        Material innerCheck = checkBlock.getRelative(x, 0, z).getType();
                        if (trunkBlocks.contains(innerCheck)) {
                            checkBlock = checkBlock.getRelative(x, 0, z);
                            break blockLoop;
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

    private List<Block> findTrunks(List<Block> trunks) {
        List<Block> result = new ArrayList<>();

        for (Block checkBlock : trunks) {

            while (debugContain(trunkBlocks, checkBlock.getType())) {
                result.add(checkBlock);

                checkBlock = checkBlock.getRelative(BlockFace.UP);
            }

            if (allExtras.contains(checkBlock.getType())) {
                debug("We hit the roof!");

                // we hit the roof and no problems
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

        System.out.println("radius is " + radius);

        // TODO this returns an invalid shape :(

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
                                System.out.println("Found another tree at " + block.getLocation());
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
        System.out.println("total checks: " + totalChecks);
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
                    System.out.println("invalid block 2: " + checkBlock.getType());
                    trunk.clear();
                    branchMap.clear();
                    return;
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

        continuations.put(BlockFace.NORTH_EAST, new BlockFace[]{BlockFace.NORTH, BlockFace.EAST});
        continuations.put(BlockFace.NORTH_WEST, new BlockFace[]{BlockFace.NORTH, BlockFace.WEST});
        continuations.put(BlockFace.SOUTH_EAST, new BlockFace[]{BlockFace.SOUTH, BlockFace.EAST});
        continuations.put(BlockFace.SOUTH_WEST, new BlockFace[]{BlockFace.SOUTH, BlockFace.WEST});
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

        BlockFace[] directions = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

        for (Block block : trunk) {
            if (first) {
                first = false;
                continue;
            }

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
                    System.out.println("invalid block 2: " + checkBlock.getType());
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

        Material mat = checkBlock.getType();
        if (trunkBlocks.contains(mat)) {
            // check for other trunks
            Block otherBlock = TreeCalculator.validate(checkBlock, config);

            if (otherBlock != null) {
                TreeStructure anotherTree = new TreeStructure(config, otherBlock, true);
                if (anotherTree.isValid() && !anotherTree.bottom.equals(bottom)) {
                    // this is another trunk! check somewhere else!
                    otherTrunks.addAll(anotherTree.getTrunk());
                    System.out.println("We hit a neighbor tree! Our bottom block " + bottom.getLocation() + " is not the same as " + anotherTree.bottom.getLocation());
                    return false;
                }
            }
            result.add(checkBlock);
            for (BlockFace face : diagonals) {
                System.out.println("Continuing branch " + direction + " to " + face);
                if (
                        invalidBranch(checkBlock.getRelative(face), result, direction) ||
                                invalidBranch(checkBlock.getRelative(face).getRelative(BlockFace.UP), result, direction) ||
                                invalidBranch(checkBlock.getRelative(face).getRelative(BlockFace.DOWN), result, direction)) {
                    result.clear();
                    return true;
                }
            }
        } else if (extraBlocks.contains(mat)) {
            System.out.println("This branch ends now.");
            //we found our end!
            return false;
        } else if (
                !naturalBlocks.contains(mat) &&
                        !(allTrunks.contains(mat) || allExtras.contains(mat))) {
            System.out.println("invalid block 3: " + mat);
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
    }

    /**
     * Checking only in trunk facing directions
     */
    private void getExtras(List<Block> trunks) {
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

        for (int y=1; y<=heightT; y++) {
            Block checkBlock = roof.getRelative(0, y, 0);
            Material checkMaterial = checkBlock.getType();

            if (checkMaterial != Material.AIR) {
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

        for (int y=1; y<=heightT; y++) {
            Block checkBlock = roof.getRelative(0, y, 0);
            Material checkMaterial = checkBlock.getType();

            if (checkMaterial != Material.AIR) {
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

        if (air || checkMaterial != Material.AIR) {
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
}
