package net.slipcor.treeassist.discovery;

import net.slipcor.core.CoreDebugger;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.events.TASaplingPlaceEvent;
import net.slipcor.treeassist.events.TATreeBrokenEvent;
import net.slipcor.treeassist.listeners.TreeAssistPlayerListener;
import net.slipcor.treeassist.runnables.CleanRunner;
import net.slipcor.treeassist.runnables.TreeAssistReplant;
import net.slipcor.treeassist.runnables.TreeAssistReplantDelay;
import net.slipcor.treeassist.utils.BlockUtils;
import net.slipcor.treeassist.utils.MaterialUtils;
import net.slipcor.treeassist.utils.ToolUtils;
import net.slipcor.treeassist.yml.MainConfig;
import net.slipcor.treeassist.yml.TreeConfig;
import net.slipcor.treeassist.yml.TreeConfigUpdater;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;

public class TreeStructure {
    public static Set<Material> allTrunks = new HashSet<>();     // Valid trunk materials of all configs
    public static Set<Material> allExtras = new HashSet<>();     // Valid tree block materials of all configs
    public static Set<Material> allNaturals = new HashSet<>();      // Valid naturally occurring materials of all configs
    static Map<BlockFace, BlockFace[]> continuations = new EnumMap<>(BlockFace.class); // Followup directions based on a given direction

    private final static Map<BlockFace, BlockFace[]> diagonalContinuations              // Followup diagonal directions based on a given direction
            = new EnumMap<>(BlockFace.class);
    public static CoreDebugger debug;

    public Block bottom;
    public List<Block> trunk;
    private final Set<Material> trunkBlocks;
    private final Set<Material> extraBlocks;
    private final Set<Material> naturalBlocks;
    private final Set<Material> groundBlocks;
    private List<Block> neighborTrunks = new ArrayList<>();
    public final List<TreeAssistReplantDelay> saplings = new ArrayList<>();
    private Map<Block, List<Block>> branchMap;
    protected Set<Block> extras;
    private final List<Block> roofs = new ArrayList<>();

    private final List<Block> checkedBlocks = new ArrayList<>();

    private final boolean trunkDiagonally;
    private boolean valid = true;
    public FailReason failReason = null;
    public Material failMaterial = null;

    private final TreeConfig config;
    private Block northWestBlock;
    private Block northEastBlock;
    private Block southWestBlock;
    private Block southEastBlock;


    static {
        continuations.put(BlockFace.EAST, new BlockFace[]{BlockFace.EAST});
        continuations.put(BlockFace.NORTH, new BlockFace[]{BlockFace.NORTH});
        continuations.put(BlockFace.SOUTH, new BlockFace[]{BlockFace.SOUTH});
        continuations.put(BlockFace.WEST, new BlockFace[]{BlockFace.WEST});

        continuations.put(BlockFace.NORTH_EAST, new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.NORTH_EAST});
        continuations.put(BlockFace.NORTH_WEST, new BlockFace[]{BlockFace.NORTH, BlockFace.WEST, BlockFace.NORTH_WEST});
        continuations.put(BlockFace.SOUTH_EAST, new BlockFace[]{BlockFace.SOUTH, BlockFace.EAST, BlockFace.SOUTH_EAST});
        continuations.put(BlockFace.SOUTH_WEST, new BlockFace[]{BlockFace.SOUTH, BlockFace.WEST, BlockFace.SOUTH_WEST});

        diagonalContinuations.put(BlockFace.NORTH,
                new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH_EAST, BlockFace.NORTH_WEST});
        diagonalContinuations.put(BlockFace.SOUTH,
                new BlockFace[] {BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_WEST});

        diagonalContinuations.put(BlockFace.EAST,
                new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.NORTH_EAST, BlockFace.SOUTH_EAST, BlockFace.EAST});
        diagonalContinuations.put(BlockFace.WEST,
                new BlockFace[] {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.NORTH_WEST, BlockFace.SOUTH_WEST, BlockFace.WEST});

        diagonalContinuations.put(BlockFace.NORTH_EAST,
                new BlockFace[] {BlockFace.NORTH, BlockFace.EAST, BlockFace.NORTH_EAST});
        diagonalContinuations.put(BlockFace.NORTH_WEST,
                new BlockFace[] {BlockFace.NORTH, BlockFace.WEST, BlockFace.NORTH_WEST});
        diagonalContinuations.put(BlockFace.SOUTH_EAST,
                new BlockFace[] {BlockFace.SOUTH, BlockFace.EAST, BlockFace.SOUTH_EAST});
        diagonalContinuations.put(BlockFace.SOUTH_WEST,
                new BlockFace[] {BlockFace.SOUTH, BlockFace.WEST, BlockFace.SOUTH_WEST});
    }

    /**
     * Try to discover a tree structure
     *
     * @param config the TreeConfig to compare to
     * @param bottom the bottom block to start checking
     * @param onlyTrunk end the check when we found one valid trunk
     */
    public TreeStructure(TreeConfig config, Block bottom, boolean onlyTrunk) {
        this.config = config;
        this.bottom = bottom;
        trunkBlocks = new LinkedHashSet<>(config.getMaterials(TreeConfig.CFG.TRUNK_MATERIALS));
        extraBlocks = new LinkedHashSet<>(config.getMaterials(TreeConfig.CFG.BLOCKS_MATERIALS));
        naturalBlocks = new LinkedHashSet<>(config.getMaterials(TreeConfig.CFG.NATURAL_BLOCKS));
        groundBlocks = new LinkedHashSet<>(config.getMaterials(TreeConfig.CFG.GROUND_BLOCKS));
        trunkDiagonally = config.getBoolean(TreeConfig.CFG.TRUNK_DIAGONAL);

        int thickness = config.getInt(TreeConfig.CFG.TRUNK_THICKNESS);
        boolean branches = config.getBoolean(TreeConfig.CFG.TRUNK_BRANCH);
        debug.i("Thickness: " + thickness);

        if (thickness == 1) {
            // Single trunk, that should be easy, right?
            trunk = findTrunk();

            if (trunk == null) {
                // No tree found, nothing to remove!
                valid = false;
                failReason = FailReason.NO_TRUNK;
                return;
            }
            debug.i("Tree of size " + trunk.size() + " found!");

            if (onlyTrunk) {
                // This will be used by other trees being broken checking OUR trunk for leaf distance and alike
                return;
            }

            int min = config.getInt(TreeConfig.CFG.TRUNK_MINIMUM_HEIGHT);
            int height = trunk.size();

            if (height < min) {
                debug.i("Lower than minimum: " + height + " < " + min + " for " + trunk.get(0).getType());
                valid = false;
                failReason = FailReason.TOO_SMALL;
                return;
            }

            neighborTrunks = new ArrayList<>();
            branchMap = new HashMap<>();

            if (branches) {
                getAllBranches();
                if (failReason != null) {
                    valid = false;
                    return;
                }

                debug.i("branch blocks: " + countBranches());
            }

            if (neighborTrunks.isEmpty()) {
                debug.i("No other trunks found, checking again!");
                findOtherTrunks();
                if (!neighborTrunks.isEmpty()) {
                    debug.i("Found other trunks: " + neighborTrunks.size());
                }
            }

            getAllExtras();

            if (extras == null || (extras.size() < config.getInt(TreeConfig.CFG.BLOCKS_REQUIRED, 10) && hasDistanceTo(neighborTrunks))) {
                if (extras != null) {
                    debug.i("Not enough extra blocks found: " + extras.size());
                }
                failReason = FailReason.NOT_ENOUGH_LEAVES;
                valid = false;
            }
            return;
        }

        if (thickness > 2) {

            // check for other trunks around
            branchMap = new HashMap<>();

            List<Block> bottoms = findThickBottoms();
            if (bottoms.size() < 3) {
                debug.i("Not enough trunks found: " + bottoms.size());
                valid = false;
                failReason = FailReason.NOT_ENOUGH_TRUNKS;
                return;
            }
            debug.i("Trunks: " + bottoms.size());
            trunk = findTrunks(bottoms);

            if (trunk == null) {
                // No tree found, nothing to remove!
                valid = false;
                failReason = FailReason.NO_TRUNK;
                return;
            }

            if (onlyTrunk) {
                // This will be used by other trees being broken checking OUR trunk for leaf distance and alike
                return;
            }

            getAllExtras();

            if (neighborTrunks.isEmpty()) {
                debug.i("No other trunks found, checking again!");
                findOtherTrunks();
                if (!neighborTrunks.isEmpty()) {
                    debug.i("Found other trunks: " + neighborTrunks.size());
                }
            }
            return;
        }
        // Thickness 2

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
                // check that is not just a jungle bush
                if (trunkBlocks.contains(nextBlock.getRelative(BlockFace.UP, 2).getType())) {
                    trunks.add(nextBlock.getRelative(BlockFace.UP));
                }
            }
        }

        if (trunks.size() != 4) {
            debug.i("We do not have 4 trunks, we found " + trunks.size() + "!");
            valid = false;
            failReason = FailReason.NOT_ENOUGH_TRUNKS;
            return;
        }

        trunk = findTrunks(trunks);

        if (trunk == null) {
            // No tree found, nothing to remove!
            valid = false;
            failReason = FailReason.NO_TRUNK;
            return;
        }
        debug.i("Tree of size " + trunk.size() + " found!");

        if (onlyTrunk) {
            // This will be used by other trees being broken checking OUR trunk for leaf distance and alike
            return;
        }

        int min = config.getInt(TreeConfig.CFG.TRUNK_MINIMUM_HEIGHT);
        int height = trunk.size() / trunks.size();

        if (height < min) {
            debug.i("Lower than thick minimum: " + height + " < " + min + " for " + trunk.get(0).getType());
            valid = false;
            failReason = FailReason.TOO_SMALL;
            return;
        }

        neighborTrunks = new ArrayList<>();

        branchMap = new HashMap<>();

        setSpecificBottoms(trunks);

        if (branches) {
            getDirectionalBranches();

            if (failReason != null) {
                valid = false;
                return;
            }

            debug.i("branch blocks: " + countBranches());
        }

        if (neighborTrunks.isEmpty()) {
            debug.i("No other trunks found, checking again!");
            findOtherTrunks();
            if (!neighborTrunks.isEmpty()) {
                debug.i("Found other trunks: " + neighborTrunks.size());
            }
        }

        getDirectionalExtras();

        if (extras == null || (extras.size() < 10 && hasDistanceTo(neighborTrunks))) {
            if (extras != null) {
                debug.i("Not enough extra blocks found: " + extras.size());
            }
            valid = false;
            failReason = FailReason.NOT_ENOUGH_LEAVES;
        }
    }

    /**
     * Create an empty tree structure
     */
    protected TreeStructure(TreeConfig config) {
        // this is only to be used by special inheriting classes
        this.config = config;
        trunkBlocks = new HashSet<>();
        extraBlocks = new HashSet<>();
        naturalBlocks = new HashSet<>();
        groundBlocks = new HashSet<>();
        trunkDiagonally = config.getBoolean(TreeConfig.CFG.TRUNK_DIAGONAL);
    }

    /**
     * Try to find a tree's bottom block based on a given block and a given tree config, traversing down
     * trying to find a valid ground block and NOT finding unexpected blocks nearby.
     *
     * @param block the block being checked
     * @param config the tree config to take to check against
     * @return the lowest trunk block if valid tree, null otherwise
     */
    public static Block findBottomBlock(final Block block, final TreeConfig config) {
        final List<Material> trunkBlocks = config.getMaterials(TreeConfig.CFG.TRUNK_MATERIALS);
        final List<Material> extraBlocks = config.getMaterials(TreeConfig.CFG.BLOCKS_MATERIALS);
        final List<Material> naturalBlocks = config.getMaterials(TreeConfig.CFG.NATURAL_BLOCKS);
        final List<Material> groundBlocks = config.getMaterials(TreeConfig.CFG.GROUND_BLOCKS);

        Block checkBlock = block;

        boolean diagonalTrunk = config.getBoolean(TreeConfig.CFG.TRUNK_DIAGONAL);

        int overflowCheck = 0;

        while (trunkBlocks.contains(checkBlock.getType())) {
            if (++overflowCheck > 256) {
                debug.i("Overflow! Not a valid tree!");
                System.out.println("overflow block: " + BlockUtils.printBlock(block));
                return null;
            }
            // As long as find more logs, keep going!
            checkBlock = checkBlock.getRelative(BlockFace.DOWN);

            if (diagonalTrunk && !trunkBlocks.contains(checkBlock.getType())) {
                Material checkMaterial = checkBlock.getType();

                debug.i("No more trunk going down at " + block.getLocation() + " - type: " + checkMaterial);

                if (!MaterialUtils.isAir(checkBlock.getType())) {
                    if (groundBlocks.contains(checkMaterial)){
                        debug.i("It's a ground block!");
                        return checkBlock.getRelative(BlockFace.UP);
                    } else if (
                            !extraBlocks.contains(checkMaterial) &&
                                    !naturalBlocks.contains(checkMaterial) &&
                                    !allTrunks.contains(checkMaterial) &&
                                    !allExtras.contains(checkMaterial)) {
                        debug.i("Unexpected block! Not a valid tree!");
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
                        debug.i("Checking diagonal at " + checkBlock.getRelative(x, 0, z).getLocation() + " - type: " + innerCheck);
                        if (!MaterialUtils.isAir(innerCheck)) {
                            if (!groundBlocks.contains(innerCheck) &&
                                    !extraBlocks.contains(innerCheck) &&
                                    !naturalBlocks.contains(innerCheck) &&
                                    !allTrunks.contains(checkMaterial) &&
                                    !allExtras.contains(checkMaterial)) {
                                debug.i("Unexpected block! Not a valid tree!");
                                return null;
                            }
                        }
                    }
                }
            }
        }

        if (groundBlocks.contains(checkBlock.getType())) {
            debug.i("We hit the ground!");

            // we hit the ground and no problems
            return checkBlock.getRelative(BlockFace.UP);
        }
        debug.i("We did not find a ground block ("+ checkBlock.getType() + ") not a valid tree.");
        return null;

    }

    /**
     * @return the loaded TreeConfig
     */
    public TreeConfig getConfig() {
        return config;
    }

    /**
     * Reload the tree configs
     */
    public static void reloadTreeDefinitions() {
        allTrunks.clear();
        allExtras.clear();
        TreeAssist.treeConfigs.clear();

        File folder = new File(TreeAssist.instance.getDataFolder().getPath(), "trees");

        Map<String, TreeConfig> processing = new HashMap<>();

        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                for (File subFile : file.listFiles()) {
                    if (subFile.getName().toLowerCase().endsWith(".yml")) {
                        String subNode = subFile.getName().toLowerCase().replace(".yml", "");
                        TreeConfig subTree = new TreeConfig(TreeAssist.instance, subFile);

                        processing.put(subNode, subTree);
                    }
                }
            } else if (file.getName().toLowerCase().endsWith(".yml")){
                String node = file.getName().toLowerCase().replace(".yml", "");
                TreeConfig tree = new TreeConfig(TreeAssist.instance, file);

                processing.put(node, tree);
            }
        }

        // Check for updates before we even try to load
        for (String key : processing.keySet()) {
            TreeConfig treeConfig = processing.get(key);
            TreeConfigUpdater.check(treeConfig, key);
            if (key.equals("default")) {
                treeConfig.clearMaps();
                treeConfig.load();
            } else {
                treeConfig.preLoad();
            }
        }

        int attempts = 100;

        while (--attempts > 0 && processing.size() > 0) {
            for (String key : processing.keySet()) {
                if (TreeAssist.treeConfigs.containsKey(key)) {
                    // We already pre-loaded it completely!
                    processing.remove(key);
                    break;
                }
                TreeConfig treeConfig = processing.get(key);
                String parentKey = treeConfig.getYamlConfiguration().getString("Parent", null);
                if (parentKey == null) {
                    // We are THE parent. We do not need to look for others before us
                    TreeAssist.treeConfigs.put(key, treeConfig);
                    processing.remove(key);
                    break;
                }
                if (TreeAssist.treeConfigs.containsKey(parentKey)) {
                    // we can now read the parent and apply defaults!
                    treeConfig.clearMaps();
                    treeConfig.loadDefaults(TreeAssist.treeConfigs.get(parentKey));
                    treeConfig.load();
                    TreeAssist.treeConfigs.put(key, treeConfig);
                    processing.remove(key);
                    break;
                }
                // Otherwise we skip around until we find a required parent, hopefully...
            }
        }

        for (String key : processing.keySet()) {
            TreeAssist.instance.getLogger().severe("Parent file not found for: " + key);
        }

        List<String> keys = new ArrayList<>(TreeAssist.treeConfigs.keySet());
        Collections.reverse(keys);
        Map<String, TreeConfig> reverseMap = new LinkedHashMap<>();
        for (String s : keys) {
            reverseMap.put(s, TreeAssist.treeConfigs.get(s));
        }
        TreeAssist.treeConfigs = reverseMap;
    }

    ///////////////////
    //               //
    // INFORMATIONAL //
    //               //
    ///////////////////

    /**
     * Check whether we are far enough away from other trunks
     *
     * @param otherTrunks other near trunk blocks we found
     * @return whether all of them are far away so they do not overlap with our tree
     */
    private boolean hasDistanceTo(List<Block> otherTrunks) {
        int radius = config.getInt(TreeConfig.CFG.BLOCKS_MIDDLE_RADIUS);
        for (Block block : otherTrunks) {
            for (Block myBlock : trunk) {
                if (block.getLocation().distance(myBlock.getLocation()) <= radius) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * @return whether the tree determination has returned a completely valid tree
     */
    public boolean isValid() {
        return valid;
    }

    ///////////////////
    //               //
    // DETERMINATION //
    //               //
    ///////////////////

    /**
     * Check whether a given block belongs to us
     *
     * @param block the block to find
     * @return whether it is part of us
     */
    public boolean containsBlock(Block block) {
        return trunk.contains(block) || extras.contains(block);
    }

    /**
     * @return the amount of branch blocks
     */
    private int countBranches() {
        int result = 0;
        for (List<Block> list : branchMap.values()) {
            if (list != null) {
                result += list.size();
            }
        }
        return result;
    }

    private List<Block> findThickBottoms() {
        List<Block> result = new ArrayList<>();
        result.add(bottom);

        BlockFace[] faces = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.WEST, BlockFace.EAST};

        for (BlockFace face : faces) {
            findThickNeighbor(result, bottom.getRelative(face), face, true);
        }
        return result;
    }

    private void findThickNeighbor(List<Block> result, Block block, BlockFace direction, boolean first) {
        if (result.contains(block) || !trunkBlocks.contains(block.getType())) {
            return;
        }
        if (Math.abs(block.getX() - bottom.getX()) > 3 || Math.abs(block.getZ() - bottom.getZ()) > 3) {
            debug.i("too far");
            return;
        }
        if (result.size() > 9) {
            debug.i("we found more than enough already");
            return;
        }
        result.add(block);
        debug.i("continuing " + direction);
        findThickNeighbor(result, block.getRelative(direction), direction, first);
        if (first) {
            debug.i("branching out");
            if (direction == BlockFace.NORTH || direction == BlockFace.SOUTH) {
                findThickNeighbor(result, block.getRelative(BlockFace.EAST), BlockFace.EAST, false);
                findThickNeighbor(result, block.getRelative(BlockFace.WEST), BlockFace.WEST, false);
            } else {
                findThickNeighbor(result, block.getRelative(BlockFace.NORTH), BlockFace.NORTH, false);
                findThickNeighbor(result, block.getRelative(BlockFace.SOUTH), BlockFace.SOUTH, false);
            }
        }
    }

    /**
     * Determine our trunk blocks by going up from the bottom, finding a roof block at the top
     *
     * @return all log blocks, null if we found an unexpected block and we should abandon ship
     */
    private List<Block> findTrunk() {
        List<Block> result = new ArrayList<>();

        Block checkBlock = bottom;

        while (trunkBlocks.contains(checkBlock.getType())) {
            result.add(checkBlock);

            checkBlock = checkBlock.getRelative(BlockFace.UP);

            if (trunkDiagonally && !trunkBlocks.contains(checkBlock.getType())) {
                Material checkMaterial = checkBlock.getType();

                debug.i("No more trunk going up at " + checkBlock.getLocation() + " - type: " + checkMaterial);

                if (!MaterialUtils.isAir(checkBlock.getType())) {
                    if (extraBlocks.contains(checkMaterial)) {
                        debug.i("It's an extra block!");
                    } else if (naturalBlocks.contains(checkMaterial)){
                        debug.i("It's a natural block!");
                    } else if (!allTrunks.contains(checkMaterial) && !allExtras.contains(checkMaterial)) {
                        debug.i("Unexpected block! Not a valid tree!");
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
                        debug.i("Checking diagonal at " + checkBlock.getRelative(x, 0, z).getLocation() + " - type: " + innerCheck);
                        if (!MaterialUtils.isAir(innerCheck)) {
                            if (extraBlocks.contains(innerCheck)) {
                                debug.i("It's an extra block!");
                            } else if (naturalBlocks.contains(innerCheck)){
                                debug.i("It's a natural block!");
                            } else if (!allTrunks.contains(checkMaterial) && !allExtras.contains(checkMaterial)) {
                                debug.i("Unexpected block! Not a valid tree!");
                                return null;
                            }
                        }
                    }
                }
            }
        }

        if (allExtras.contains(checkBlock.getType()) || MaterialUtils.isAir(checkBlock.getType())) {
            debug.i("We hit the roof!");

            // we hit the roof and no problems
            return result;
        }

        debug.i("We did not find a roof block (" + checkBlock.getType() + ") not a valid tree!");
        return null;
    }

    /**
     * Determine our trunks by going up from the bottoms, finding roof blocks at the top
     *
     * @param bottoms the bottom blocks to start looking from
     * @return all log blocks, null if we found an unexpected block and we should abandon ship
     */
    private List<Block> findTrunks(List<Block> bottoms) {
        List<Block> result = new ArrayList<>();

        for (Block checkBlock : bottoms) {

            while (trunkBlocks.contains(checkBlock.getType())) {
                result.add(checkBlock);

                checkBlock = checkBlock.getRelative(BlockFace.UP);
            }

            if (allExtras.contains(checkBlock.getType()) || MaterialUtils.isAir(checkBlock.getType())) {
                debug.i("We hit the roof!");

                // we hit the roof and no problems
                roofs.add(checkBlock);
                continue;
            }
            debug.i("We did not find a roof block (" + checkBlock.getType() + ") not a valid tree!");
            return null;
        }

        return result;
    }

    /**
     * Try to find other nearby trunks that might have leaves interfering
     */
    private void findOtherTrunks() {
        int radius = config.getInt(TreeConfig.CFG.BLOCKS_MIDDLE_RADIUS) * 2;

        int totalChecks = 0;

        for (int x=-radius; x<=radius; x++) {
            blocks: for (int z=-radius; z<= radius; z++) {
                if (x == 0 && z == 0) {
                    continue;
                }

                Block checkBlock = bottom.getRelative(x, 4, z);
                int y = checkBlock.getY();
                while (y > 1) {
                    totalChecks++;
                    checkBlock = checkBlock.getRelative(BlockFace.DOWN);

                    if (trunk.contains(checkBlock)) {
                        break;
                    }

                    if (trunkBlocks.contains(checkBlock.getType())) {
                        Block block = findBottomBlock(checkBlock, config);
                        if (block != null && isNotOurBottom(block)) {
                            TreeStructure otherTree = new TreeStructure(config, block, true);
                            if (otherTree.isValid()) {
                                neighborTrunks.addAll(otherTree.trunk);
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

    private boolean isNotOurBottom(Block block) {
        if (block.equals(bottom)) {
            return false;
        }
        if (block.equals(northEastBlock) || block.equals(northWestBlock) || block.equals(southEastBlock) || block.equals(southWestBlock)) {
            return false;
        }
        return true;
    }

    /**
     * Find branches, looking into all directions
     *
     * Blocks will be added to the branchMap
     */
    private void getAllBranches() {

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
                        if (isInvalidBranch(checkBlock, branch, innerface)) {
                            trunk.clear();
                            branchMap.clear();
                            failReason = FailReason.INVALID_BLOCK;
                            return;
                        }
                    }
                    branchMap.put(checkBlock, branch);
                } else if (!allExtras.contains(checkBlock.getType())
                        && !allTrunks.contains(checkBlock.getType())
                        && !naturalBlocks.contains(checkBlock.getType())) {
                    debug.i("invalid block 2a: " + checkBlock.getType());
                    trunk.clear();
                    branchMap.clear();
                    failReason = FailReason.INVALID_BLOCK;
                    failMaterial = checkBlock.getType();
                    return;
                }
            }
        }
    }

    /**
     * Calculate extra blocks, looking in all directions
     */
    private void getAllExtras() {
        extras = new LinkedHashSet<>();

        int radiusM = config.getInt(TreeConfig.CFG.BLOCKS_MIDDLE_RADIUS);

        boolean edgesM = config.getBoolean(TreeConfig.CFG.BLOCKS_MIDDLE_EDGES);
        boolean airM = config.getBoolean(TreeConfig.CFG.BLOCKS_MIDDLE_AIR);

        Block roof = null;

        BlockFace[] neighbors = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

        for (Block block : trunk) {
            roof = block;

            for (BlockFace face : neighbors) {
                if (isInvalidExtraBlock(block.getRelative(face), face, radiusM, 1, true, edgesM, airM, radiusM)) {
                    valid = false;
                    failReason = FailReason.INVALID_BLOCK;
                    debug.i("invalid block at " + BlockUtils.printBlock(block.getRelative(face)));
                    return;
                }
            }
        }

        if (roof == null) {
            debug.i("No more blocks found!");
            valid = false;
            failReason = FailReason.NO_TRUNK;
            return;
        }

        int radiusT = config.getInt(TreeConfig.CFG.BLOCKS_TOP_RADIUS);
        int heightT = config.getInt(TreeConfig.CFG.BLOCKS_TOP_HEIGHT);
        boolean airT = config.getBoolean(TreeConfig.CFG.BLOCKS_TOP_AIR);

        boolean edgesT = config.getBoolean(TreeConfig.CFG.BLOCKS_TOP_EDGES);

        for (int y=0; y<=heightT; y++) {
            Block checkBlock = roof.getRelative(0, y, 0);
            Material checkMaterial = checkBlock.getType();

            if (!MaterialUtils.isAir(checkMaterial)) {
                if (extraBlocks.contains(checkMaterial)) {
                    extras.add(checkBlock);

                    for (BlockFace face : neighbors) {
                        if (isInvalidExtraBlock(checkBlock.getRelative(face), face, radiusT, 1, true, edgesT, airT, radiusT)) {
                            valid = false;
                            failReason = FailReason.INVALID_BLOCK;
                            debug.i("invalid block at " + BlockUtils.printBlock(checkBlock.getRelative(face)));
                            return;
                        }
                    }
                } else if (
                        !naturalBlocks.contains(checkMaterial) &&
                                !trunkBlocks.contains(checkMaterial) &&
                                !(allTrunks.contains(checkMaterial) || allExtras.contains(checkMaterial))) {
                    valid = false;
                    failReason = FailReason.INVALID_BLOCK;
                    failMaterial = checkMaterial;
                    debug.i("invalid block at " + BlockUtils.printBlock(checkBlock));
                    return;
                }
            }
        }
    }

    /**
     * Find branches, only looking facing outward because we have a thick tree
     *
     * Blocks will be added to the branchMap
     */
    private void getDirectionalBranches() {

        boolean first = true;

        List<Block> checkRoofs = new ArrayList<>(roofs);

        for (Block block : checkRoofs) {
            BlockFace[] directions = getTrunkContinuations(block);
            for (BlockFace face : directions) {
                Block checkBlock = block.getRelative(face);

                List<Block> branch = new ArrayList<>();
                if (!isInvalidBranch(checkBlock, branch, face)) {
                    branchMap.put(checkBlock, branch);
                } else {
                    checkBlock = checkBlock.getRelative(BlockFace.UP);
                    if (!isInvalidBranch(checkBlock, branch, face)) {
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

            BlockFace[] directions = getTrunkContinuations(block);

            for (BlockFace face : directions) {
                Block checkBlock = block.getRelative(face);
                if (trunkBlocks.contains(checkBlock.getType())) {
                    List<Block> branch = new ArrayList<>();
                    if (isInvalidBranch(checkBlock, branch, face)) {
                        trunk.clear();
                        branchMap.clear();
                        failReason = FailReason.INVALID_BLOCK;
                        return;
                    }
                    branchMap.put(checkBlock, branch);
                } else if (!allExtras.contains(checkBlock.getType())
                        && !allTrunks.contains(checkBlock.getType())
                        && !naturalBlocks.contains(checkBlock.getType())) {
                    debug.i("invalid block 2b: " + checkBlock.getType());
                    trunk.clear();
                    branchMap.clear();
                    failReason = FailReason.INVALID_BLOCK;
                    failMaterial = checkBlock.getType();
                    return;
                } else {
                    // This branch ends here
                    roofs.add(checkBlock);
                }
            }
        }
    }

    /**
     * Calculate extra blocks, checking only in trunk facing directions
     */
    private void getDirectionalExtras() {
        extras = new LinkedHashSet<>();

        int radiusM = config.getInt(TreeConfig.CFG.BLOCKS_MIDDLE_RADIUS);

        boolean edgesM = config.getBoolean(TreeConfig.CFG.BLOCKS_MIDDLE_EDGES);
        boolean airM = config.getBoolean(TreeConfig.CFG.BLOCKS_MIDDLE_AIR);

        for (Block checkBlock : trunk) {
            for (BlockFace face : getTrunkContinuations(checkBlock)) {
                if (isInvalidExtraBlock(checkBlock.getRelative(face), face, radiusM, 1, true, edgesM, airM, radiusM)) {
                    valid = false;
                    failReason = FailReason.INVALID_BLOCK;
                    debug.i("invalid block at " + BlockUtils.printBlock(checkBlock.getRelative(face)));
                    return;
                }
            }
        }

        BlockFace[] neighbors = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

        if (roofs.size() <= 0) {
            debug.i("No more blocks found!");
            valid = false;
            failReason = FailReason.NO_TRUNK;
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

                if (!MaterialUtils.isAir(checkMaterial)) {
                    if (extraBlocks.contains(checkMaterial)) {
                        extras.add(checkBlock);

                        for (BlockFace face : neighbors) {
                            if (isInvalidExtraBlock(checkBlock.getRelative(face), face, radiusT, 1, true, edgesT, airT, radiusT)) {
                                valid = false;
                                failReason = FailReason.INVALID_BLOCK;
                                debug.i("invalid block at " + BlockUtils.printBlock(checkBlock.getRelative(face)));
                                return;
                            }
                        }
                    } else if (
                            !naturalBlocks.contains(checkMaterial) &&
                                    !trunkBlocks.contains(checkMaterial) &&
                                    !(allTrunks.contains(checkMaterial) || allExtras.contains(checkMaterial))) {
                        valid = false;
                        failReason = FailReason.INVALID_BLOCK;
                        failMaterial = checkMaterial;
                        debug.i("invalid block at " + BlockUtils.printBlock(checkBlock));
                        return;
                    }
                }
            }
        }
    }

    /**
     * Get the continuations based on a given block
     *
     * @param checkBlock the block to look for
     * @return matching continuations, if this block is part of a trunk
     */
    private BlockFace[] getTrunkContinuations(Block checkBlock) {
        if (    northWestBlock != null &&
                checkBlock.getX() == northWestBlock.getX() &&
                checkBlock.getZ() == northWestBlock.getZ()) {
            return continuations.get(BlockFace.NORTH_WEST);
        }
        if (    northEastBlock != null &&
                checkBlock.getX() == northEastBlock.getX() &&
                checkBlock.getZ() == northEastBlock.getZ()) {
            return continuations.get(BlockFace.NORTH_EAST);
        }
        if (    southWestBlock != null &&
                checkBlock.getX() == southWestBlock.getX() &&
                checkBlock.getZ() == southWestBlock.getZ()) {
            return continuations.get(BlockFace.SOUTH_WEST);
        }
        if (    southEastBlock != null &&
                checkBlock.getX() == southEastBlock.getX() &&
                checkBlock.getZ() == southEastBlock.getZ()) {
            return continuations.get(BlockFace.SOUTH_EAST);
        }

        return new BlockFace[0];
    }

    /**
     * Recursively check a given block whether it is part of a branch, if it is, keep on looking!
     *
     * @param checkBlock the block to start checking
     * @param result the resulting branch List
     * @param direction the direction the branch would start
     * @return whether something went wrong and we should abandon ship because of that
     */
    private boolean isInvalidBranch(Block checkBlock, List<Block> result, BlockFace direction) {
        if (checkedBlocks.contains(checkBlock)) {
            return false;
        }
        checkedBlocks.add(checkBlock);
        BlockFace[] diagonals = diagonalContinuations.get(direction);

        if (diagonals == null) {
            diagonals = new BlockFace[]{direction};
        }

        Material mat = checkBlock.getType();
        if (trunkBlocks.contains(mat)) {
            // Check for other trunks
            Block otherBlock = findBottomBlock(checkBlock, config);

            if (otherBlock != null) {
                TreeStructure anotherTree = new TreeStructure(config, otherBlock, true);
                if (anotherTree.isValid() && !anotherTree.bottom.equals(bottom)) {
                    // This is another trunk! check somewhere else!
                    neighborTrunks.addAll(anotherTree.trunk);
                    debug.i("We hit a neighbor tree! Our bottom block " + bottom.getLocation() + " is not the same as " + anotherTree.bottom.getLocation());
                    return false;
                }
            }
            result.add(checkBlock);
            for (BlockFace face : diagonals) {
                debug.i("Continuing branch " + direction + " to " + face);
                if (
                        isInvalidBranch(checkBlock.getRelative(face), result, direction) ||
                                isInvalidBranch(checkBlock.getRelative(face).getRelative(BlockFace.UP), result, direction) ||
                                isInvalidBranch(checkBlock.getRelative(face).getRelative(BlockFace.DOWN), result, direction)) {
                    result.clear();
                    return true;
                }
            }
            if (isInvalidBranch(checkBlock.getRelative(BlockFace.UP), result, direction)) {
                result.clear();
                return true;
            }
        } else if (extraBlocks.contains(mat)) {
            debug.i("This branch ends now.");
            roofs.add(checkBlock);
            // We found our end!
            return false;
        } else if (
                !naturalBlocks.contains(mat) &&
                        !(allTrunks.contains(mat) || allExtras.contains(mat))) {
            debug.i("invalid block 3: " + mat);
            failMaterial = mat;
            return true;
        }
        return false;
    }

    /**
     * Recursively check for extra blocks going into a certain direction
     *
     * @param checkBlock the currently checked block
     * @param direction the direction to go
     * @param expansion the total distance we have to travel
     * @param progress the progress travelling
     * @param first is this the initial direction? (if so, it will split to perpendicular, too)
     * @param edges are we expecting edge blocks?
     * @param air are we allowing air and thus continuing until we reach the radius?
     * @param radius what is the radius of similar trees to check for?
     * @return whether something went wrong and we should abandon ship because of that
     */
    private boolean isInvalidExtraBlock(Block checkBlock, BlockFace direction,
                                        int expansion, int progress, boolean first, boolean edges, boolean air, int radius) {
        for (Block otherBlock : neighborTrunks) {
            if (otherBlock.getLocation().distance(checkBlock.getLocation()) <= radius) {
                // We are not invalid but we want to NOT add this block and not continue searching for extras
                return false;
            }
        }
        Material checkMaterial = checkBlock.getType();

        if (air || !MaterialUtils.isAir(checkMaterial)) {
            boolean found = air;
            if (config.getBoolean(TreeConfig.CFG.BLOCKS_VINES) && checkBlock.getType() == Material.VINE) {
                followVines(checkBlock);
            }
            if (extraBlocks.contains(checkMaterial)) {
                extras.add(checkBlock);
                found = true;
            }

            if (found) {
                if (first) {
                    boolean shorter = !edges && (progress == expansion);
                    if (direction == BlockFace.EAST || direction == BlockFace.WEST) {
                        // Expand to north/south direction
                        if (isInvalidExtraBlock(checkBlock.getRelative(BlockFace.SOUTH), BlockFace.SOUTH, shorter ? progress - 1 : progress, 1, false, edges, air, radius
                        ) ||
                                isInvalidExtraBlock(checkBlock.getRelative(BlockFace.NORTH), BlockFace.NORTH, progress, 1, false, edges, air, radius
                                )){
                            return true;
                        }
                    } else {
                        // Expand to east/west direction
                        if (isInvalidExtraBlock(checkBlock.getRelative(BlockFace.EAST), BlockFace.EAST, progress, 1, false, edges, air, radius
                        ) ||
                                isInvalidExtraBlock(checkBlock.getRelative(BlockFace.WEST), BlockFace.WEST, progress, 1, false, edges, air, radius
                                )){
                            return true;
                        }
                    }
                }
                // Expand, if we can
                if (progress < expansion) {
                    return isInvalidExtraBlock(checkBlock.getRelative(direction), direction, expansion, progress + 1, first, edges, air, radius
                    );
                }
            } else if (
                    !naturalBlocks.contains(checkMaterial) &&
                            !trunkBlocks.contains(checkMaterial) &&
                            !(allTrunks.contains(checkMaterial) || allExtras.contains(checkMaterial))) {
                debug.i("Invalid block found 1: " + checkMaterial);
                failMaterial = checkMaterial;
                return true;
            }
        }
        return false;
    }

    private void followVines(Block checkBlock) {
        if (extras.contains(checkBlock) || checkBlock.getType() != Material.VINE) {
            return;
        }
        extras.add(checkBlock);
        followVines(checkBlock.getRelative(0, -1, 0));
    }

    /**
     * Try to break a block for the player, checking some things beforehand, like the TATreeBrokenEvent and statistics,
     * dropping custom blocks and so on, based on the tree config setting
     *
     * @param block the block in question
     * @param tool an optional tool being used
     * @param player a player doing the breaking
     * @param statPickup whether we should increase the pickup statistic
     * @param statMineBlock whether we should increase the block mining statistic
     * @param toolDamage whether we should damage the tool
     * @param creative whether the player is in creative mode
     */
    private void maybeBreakBlock(Block block, ItemStack tool, Player player,
                                 boolean statPickup, boolean statMineBlock, boolean toolDamage, boolean creative) {

        TATreeBrokenEvent event = new TATreeBrokenEvent(block, player, tool);
        TreeAssist.instance.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            debug.i(">>> Cancelled by plugin! <<< Aborting breaking!");
            return;
        }

        Material blockMaterial = block.getType();
        boolean calculateCustomDrops = !creative &&
                (config.getBoolean(TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_ACTIVE) && this.extraBlocks.contains(blockMaterial)) ||
                        (config.getBoolean(TreeConfig.CFG.TRUNK_CUSTOM_DROPS_ACTIVE) && this.trunkBlocks.contains(blockMaterial));

        double chanceValue = (new Random()).nextDouble();
        debug.i("breaking " + blockMaterial +". custom drops: " + calculateCustomDrops + " - roll: " + chanceValue);

        BlockUtils.callExternals(block, player, false);

        boolean isTrunk = this.trunkBlocks.contains(blockMaterial);

        if (calculateCustomDrops && tool != null) {
            double chance = isTrunk
                    ? config.getMapEntry(TreeConfig.CFG.TRUNK_CUSTOM_DROPS_FACTORS, tool.getType().name(), 0.0)
                    : config.getMapEntry(TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_FACTORS, tool.getType().name(), 0.0);
            debug.i("probability " + chance + " for " + tool.getType().name());
            double secondChance = isTrunk
                    ? config.getMapEntry(TreeConfig.CFG.TRUNK_CUSTOM_DROPS_FACTORS, "minecraft:" + tool.getType().name().toLowerCase(), 0.0)
                    : config.getMapEntry(TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_FACTORS, "minecraft:" + tool.getType().name().toLowerCase(), 0.0);
            debug.i("probability " + secondChance + " for " + tool.getType().name().toLowerCase());
            if (secondChance > chance) {
                chance = secondChance;
            }

            if (chance > 0.99 || chanceValue < chance) {
                debug.i("dropping custom drop!");

                Map<String, Double> chances = isTrunk ? config.getMap(TreeConfig.CFG.TRUNK_CUSTOM_DROPS_ITEMS) : config.getMap(TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_ITEMS);

                debug.i("custom drop count: " + chances.size());

                for (String key : chances.keySet()) {
                    double innerChance = (chances.get(key));
                    double innerValue = (new Random()).nextDouble();

                    if (innerValue < innerChance) {
                        debug.i("dropping: " + key);

                        try {
                            Material mat = Material.matchMaterial(key);
                            debug.i(">2 : " + mat.name());
                            block.getWorld()
                                    .dropItemNaturally(block.getLocation(),
                                            new ItemStack(mat));
                        } catch (Exception e) {
                            TreeAssist.instance.getLogger().warning(
                                    "Invalid config value: Custom Drops."
                                            + key
                                            + " is not a valid Material!");
                        }

                    } else {
                        debug.i(innerValue + " >= " + innerChance);
                    }
                }
            }
        } else {
            debug.i("mat: " + blockMaterial.name());
        }
        TreeAssist.instance.blockList.logBreak(block, player);
        if (player == null) {
            debug.i("no player, out!");
            BlockUtils.breakBlock(null, block, tool, bottom.getY());
            return;
        }

        if (statMineBlock) {
            player.incrementStatistic(Statistic.MINE_BLOCK, blockMaterial);
        }

        if (MaterialUtils.isLog(blockMaterial)
                && config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_AUTO_ADD_TO_INVENTORY)) {
            debug.i("auto adding!");
            if (statPickup) {
                player.incrementStatistic(Statistic.PICKUP, blockMaterial);
            }
            List<ItemStack> drops = new ArrayList<>(block.getDrops(new ItemStack(tool == null?Material.AIR:tool.getType(), 1)));
            player.getInventory().addItem(drops.toArray(new ItemStack[0]));
            block.setType(Material.AIR, true);
        } else {
            debug.i("not auto adding!");
            if (config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_USE_SILK_TOUCH) && tool != null && tool.hasItemMeta() && tool.getItemMeta().getEnchants().containsKey(Enchantment.SILK_TOUCH)
                    && MaterialUtils.isMushroom(blockMaterial)) {
                debug.i("silk touching mushroom!");

                block.setType(Material.AIR, true);
                block.getWorld().dropItemNaturally(
                        block.getLocation(),
                        new ItemStack(blockMaterial, 1));
                if (config.getBoolean(TreeConfig.CFG.BLOCK_STATISTICS_MINE_BLOCK)) {
                    player.incrementStatistic(Statistic.MINE_BLOCK, blockMaterial);
                }
            } else {
                ItemStack anotherTool = tool;
                if (tool != null && !config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_USE_SILK_TOUCH)) {
                    debug.i("duplicating tool of type " + tool.getType());
                    anotherTool = new ItemStack(tool.getType(), tool.getAmount());
                } else if (MaterialUtils.isLeaf(blockMaterial) && config.getBoolean(TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_OVERRIDE)) {
                    debug.i("nulling tool for leaf " + BlockUtils.printBlock(block));
                    anotherTool = null;
                } else if (MaterialUtils.isLog(blockMaterial) && config.getBoolean(TreeConfig.CFG.TRUNK_CUSTOM_DROPS_OVERRIDE)) {
                    debug.i("nulling tool for log " + BlockUtils.printBlock(block));
                    anotherTool = null;
                } else {
                    debug.i("simply breaking " + BlockUtils.printBlock(block));

                    debug.i("tool used: " + ToolUtils.printTool(anotherTool));
                }
                BlockUtils.breakBlock(player, block, anotherTool, bottom.getY());
            }
        }
        player.sendBlockChange(block.getLocation(), Material.AIR.createBlockData());

        if (!config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_TOOL_DAMAGE_FOR_LEAVES) && MaterialUtils.isLeaf(blockMaterial)) {
            debug.i("skipping damage because of nodamage setting in config");
            return;
        }

        if (!toolDamage) {
            debug.i("skipping damage for other reasons");
            return;
        }

        if (!calculateCustomDrops && tool != null) {
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
            } else if (ToolUtils.isVanillaTool(tool)) {
                ItemMeta meta = tool.getItemMeta();
                if (meta != null) {
                    ((Damageable)meta).setDamage(((Damageable)meta).getDamage() + 2);
                    tool.setItemMeta(meta);
                }
            }
        }
    }

    /**
     * Check whether we should replant a sapling, and maybe do so
     *
     * @param player the player who initiated the breaking
     * @param block the bottom block
     */
    public void maybeReplant(Player player, Block block) {
        if (!config.getBoolean(TreeConfig.CFG.REPLANTING_ACTIVE)) {
            debug.i("replanting is disabled!");
            return;
        }

        MainConfig globalConfig = TreeAssist.instance.config();

        if (!config.getBoolean(TreeConfig.CFG.REPLANTING_ENFORCE)) {

            if (TreeAssist.instance.getBlockListener().isNoReplant(player.getName())) {
                debug.i("Player is NoReplant!");
                return;
            }

            if (globalConfig.getBoolean(MainConfig.CFG.GENERAL_USE_PERMISSIONS) &&
                    !player.hasPermission("treeassist.replant")) {
                debug.i("Player has no replant perms!");
                return;
            }

            if (config.getBoolean(TreeConfig.CFG.REPLANTING_REQUIRES_TOOLS)) {
                if (!ToolUtils.isMatchingTool(player.getInventory().getItemInMainHand(), config)) {
                    debug.i("Player does not have the tool!!");
                    return;
                }
            }

            if (!(block.equals(bottom) ||
                    block.equals(northWestBlock) ||
                    block.equals(southWestBlock) ||
                    block.equals(northEastBlock) ||
                    block.equals(southEastBlock)
            )) {
                if (config.getBoolean(TreeConfig.CFG.REPLANTING_ONLY_WHEN_BOTTOM_BLOCK_BROKEN_FIRST)) {
                    debug.i("We did not break the bottom!");
                    return;
                }
            }
        }

        debug.i("we are replacing now!");

        int delay = Math.max(1, config.getInt(TreeConfig.CFG.REPLANTING_DELAY));

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
            debug.i("checking bottom: " + BlockUtils.printBlock(saplingBlock));

            TASaplingPlaceEvent placeEvent = new TASaplingPlaceEvent(saplingBlock, saplingMat);
            TreeAssist.instance.getServer().getPluginManager().callEvent(placeEvent);

            if (placeEvent.isCancelled()) {
                debug.i("Sapling placement was cancelled by a plugin!");
                continue;
            }

            Runnable b = new TreeAssistReplant(saplingBlock, placeEvent.getType(), config);

            this.saplings.add(new TreeAssistReplantDelay(this, saplingBlock, b, delay));
        }
    }

    /**
     * In case someone set the "Only Above" setting, we remove the blocks below the broken block from the
     * list of blocks to be broken
     *
     * @param block the block the player broke initially
     */
    public void removeBlocksBelow(Block block) {
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

    /**
     * Remove a tree later, or maybe even now!
     *
     * @param player the player initiating the breaking
     * @param tool an optional tool the player is holding
     */
    public void removeTreeLater(Player player, ItemStack tool) {
        boolean creative = player != null && player.getGameMode() == GameMode.CREATIVE;
        boolean damage = !creative && ToolUtils.receivesDamage(config, tool);

        debug.i("Removing The Tree!");

        final int delay = config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_INITIAL_DELAY) ? config.getInt(
                TreeConfig.CFG.AUTOMATIC_DESTRUCTION_INITIAL_DELAY_TIME) * 20 : 0;
        final int offset = config.getInt(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_DELAY);

        debug.i("delay: " + delay +"; offset: " + offset);

        Material sapling = config.getMaterial(TreeConfig.CFG.REPLANTING_MATERIAL);

        if (player != null) {
            TreeAssist.instance.setCoolDown(player, config, trunk);
            TreeAssistPlayerListener.addDestroyer(player, this);
        }

        final boolean statPickup = config.getBoolean(TreeConfig.CFG.BLOCK_STATISTICS_PICKUP);
        final boolean statMineBlock = config.getBoolean(TreeConfig.CFG.BLOCK_STATISTICS_MINE_BLOCK);

        debug.i("pickup: " + statPickup + "; mine: " + statMineBlock);

        final Set<Block> removeBlocks = new LinkedHashSet<>(trunk);

        debug.i("trunk blocks: " + removeBlocks.size());
        debug.i("this trunk is: " + trunk.hashCode());

        for (List<Block> blocks : branchMap.values()) {
            if (blocks != null) {
                removeBlocks.addAll(blocks);
                debug.i("branch blocks: " + blocks.size());
            }
        }

        if (offset >= 0) {
            // If there is an offset, we break the logs from the bottom up
            BlockUtils.sortBottomUp(removeBlocks);
            // And the leaves from inside out
            BlockUtils.sortInsideOut(extras, bottom);
        }

        if (statPickup) {
            BlockUtils.updatePickup(player, trunk.get(1).getType(), removeBlocks.size());
        }

        if (statMineBlock) {
            BlockUtils.updateMining(player, trunk.get(1).getType(), removeBlocks.size());
        }

        if (config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_REMOVE_LEAVES)) {
            removeBlocks.addAll(extras);
            debug.i("extra blocks: " + extras.size());
        }

        class InstantRunner extends BukkitRunnable {
            @Override
            public void run() {
                if (offset < 0) {
                    for (Block block : removeBlocks) {
                        if (sapling.equals(block.getType())) {
                            debug.i("InstantRunner: skipping breaking a sapling");
                            continue;
                        }
                        debug.i("InstantRunner: 1 " + BlockUtils.printBlock(block));
                        maybeBreakBlock(block, tool, player, statPickup, statMineBlock, damage, creative);
                        if (damage && ToolUtils.willBreak(tool, player)) {
                            this.cancel();
                            TreeAssistPlayerListener.removeDestroyer(player, TreeStructure.this);
                            // we skip clearing the block list because there was an issue that requires cleanup
                            return;
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
                            debug.i("InstantRunner: 2 AIR " + BlockUtils.printBlock(block));
                            continue;
                        } else {
                            debug.i("InstantRunner: 2b " + BlockUtils.printBlock(block));
                            maybeBreakBlock(block, tool, player, statPickup, statMineBlock, damage, creative);
                            if (damage && ToolUtils.willBreak(tool, player)) {
                                this.cancel();
                                TreeAssistPlayerListener.removeDestroyer(player, TreeStructure.this);
                                // we skip clearing the block list because there was an issue that requires cleanup
                                return;
                            }
                        }
                        removeBlocks.remove(block);
                        return;
                    }
                }
                try {
                    TreeAssistPlayerListener.removeDestroyer(player, TreeStructure.this);
                    this.cancel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }

        CleanRunner cleaner = (new CleanRunner(this, offset, removeBlocks, sapling));
        if (player != null) {
            (new InstantRunner()).runTaskTimer(TreeAssist.instance, delay, offset);
        }

        int cleanDelay = config.getInt(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_CLEANUP_DELAY_TIME);

        cleaner.runTaskTimer(TreeAssist.instance, cleanDelay *20L, offset);
    }

    /**
     * Specifically set the bottom blocks to our directional block variables
     *
     * @param bottoms the bottom blocks, as they were discovered
     */
    private void setSpecificBottoms(List<Block> bottoms) {

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
    }

    public void setValid(boolean value) {
        this.valid = value;
    }

    public void plantSaplings() {
        debug.i("We are now planning to replant!");
        List<TreeAssistReplantDelay> mySaplings = new ArrayList<>(saplings);
        for (TreeAssistReplantDelay replanting : mySaplings) {
            replanting.commit();
        }
        saplings.clear();
    }

    public void addReplantDelay(TreeAssistReplantDelay delay) {
        delay.setTree(this);
        this.saplings.add(delay);
    }
}
