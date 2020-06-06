package net.slipcor.treeassist.utils;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.configs.MainConfig;
import net.slipcor.treeassist.configs.TreeConfig;
import net.slipcor.treeassist.core.TreeStructure;
import net.slipcor.treeassist.events.TALeafDecay;
import net.slipcor.treeassist.externals.JobsHook;
import net.slipcor.treeassist.externals.mcMMOHook;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

/**
 * Utility methods about Blocks
 */
public class BlockUtils {
    private BlockUtils() {}

    private static Boolean useFallingBlock = null;
    private static final List<FallingBlock> fallingBlocks = new ArrayList<>();

    /**
     * Finally actually break a block
     *
     * @param block the block to break
     */
    public static void breakBlock(Block block) {
        breakBlock(null, block, null);
    }

    /**
     * Finally actually break a block
     *
     * @param player the player initiating the breaking
     * @param block the block to break
     */
    public static void breakBlock(Player player, Block block) {
        breakBlock(player, block, null);
    }

    /**
     * Finally actually break a block
     *
     * @param player the player initiating the breaking
     * @param block the block to break
     * @param tool the item the player is holding
     */
    public static void breakBlock(Player player, Block block, ItemStack tool) {
        if (useFallingBlock == null) {
            useFallingBlock = TreeAssist.instance.getMainConfig().getBoolean(MainConfig.CFG.DESTRUCTION_FALLING_BLOCKS);
        }

        if (useFallingBlock) {
            Collection<ItemStack> drops = tool == null ? block.getDrops() : block.getDrops(tool);

            BlockData data = block.getBlockData();

            block.setType(Material.AIR);

            FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation(), data);

            fallingBlocks.add(falling);

            if (player != null) {
                player.sendBlockChange(block.getLocation(), block.getBlockData());
            }

            for(ItemStack item : drops) {
                block.getWorld().dropItemNaturally(block.getLocation(), item);
            }
        } else {
            if (tool == null) {
                block.breakNaturally();
            } else {
                block.breakNaturally(tool);
            }
        }
    }

    /**
     * Checks if the block is a known leaf block and drops it if a 2 block radius does not contain:
     * a) A valid log
     * b) 5 unexpected blocks
     *
     * @param block the block to check
     * @param config the tree config to check against
     */
    private static void breakIfLonelyLeaf(Block block, TreeConfig config) {
        List<Material> extras = config.getMaterials(TreeConfig.CFG.BLOCKS_MATERIALS);
        if (!extras.contains(block.getType())) {
            return;
        }
        List<Material> trunks = config.getMaterials(TreeConfig.CFG.TRUNK_MATERIALS);
        List<Material> naturals = config.getMaterials(TreeConfig.CFG.NATURAL_BLOCKS);

        TALeafDecay event = new TALeafDecay(block);
        TreeAssist.instance.getServer().getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            World world = block.getWorld();

            int preventCheck = -1; // because we will fail once, when finding blockAt

            for (int x = block.getX() - 2; x <= block.getX() + 2; x++) {
                for (int y = block.getY() - 2; y <= block.getY() + 2; y++) {
                    for (int z = block.getZ() - 2; z <= block.getZ() + 2; z++) {
                        preventCheck += decayPreventionValue(world.getBlockAt(x, y, z), trunks, naturals);
                        if (preventCheck > 4) {
                            return; // prevent threshold -> out!
                        }
                    }
                }
            }

            TreeAssist.instance.blockList.logBreak(block, null);
            breakBlock(block);
        }
    }

    /**
     * Try to break lonely leaves in a 8 block radius, and 2 above and below
     *
     * @param block the center block to check
     * @param config the tree config to take into account
     */
    public static void breakRadiusLeaves(Block block, TreeConfig config) {
        TALeafDecay event = new TALeafDecay(block);
        TreeAssist.instance.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return;
        }
        TreeAssist.instance.blockList.logBreak(block, null);
        breakBlock(block);
        World world = block.getWorld();
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();
        for (int x2 = -8; x2 < 9; x2++) {
            for (int z2 = -8; z2 < 9; z2++) {
                breakIfLonelyLeaf(world.getBlockAt(x + x2, y + 2, z + z2), config);
                breakIfLonelyLeaf(world.getBlockAt(x + x2, y + 1, z + z2), config);
                breakIfLonelyLeaf(world.getBlockAt(x + x2, y, z + z2), config);
                breakIfLonelyLeaf(world.getBlockAt(x + x2, y - 1, z + z2), config);
                breakIfLonelyLeaf(world.getBlockAt(x + x2, y - 2, z + z2), config);
            }
        }
    }

    /**
     * Call external plugins when breaking a block for the player
     *
     * @param block the block being broken
     * @param player the player initiating the breaking
     */
    public static void callExternals(Block block, Player player) {
        boolean leaf = TreeStructure.allExtras.contains(block.getType());

        if (!leaf && player != null) {
            if (TreeAssist.instance.mcMMO) {
                TreeStructure.debug.i("Adding mcMMO EXP!");
                mcMMOHook.mcMMOAddExp(player, block);
            }

            if (TreeAssist.instance.jobs) {
                TreeStructure.debug.i("Adding Jobs EXP!");
                JobsHook.addJobsExp(player, block);
            }
        } else {
            TreeStructure.debug.i("mcMMO: " + TreeAssist.instance.mcMMO);
            TreeStructure.debug.i("jobs: " + TreeAssist.instance.jobs);
            TreeStructure.debug.i("player: " + player);
        }
    }

    /**
     * Return a value that indicates the decay prevention value
     * @param block the block to check
     * @param trunks valid trunk blocks
     * @param naturals valid naturally occurring blocks
     * @return 0 through 5 based on material
     */
    private static int decayPreventionValue(Block block, List<Material> trunks, List<Material> naturals) {
        if (trunks.contains(block.getType())) {
            // our valid trunk blocks should prevent leaf decay
            return 5;
        } else if (MaterialUtils.isAir(block.getType()) || TreeStructure.allExtras.contains(block.getType()) || naturals.contains(block.getType())) {
            // air, all types of leaves and our naturally occurring blocks should not be an issue
            return 0;
        } else {
            // anything else, check for too many of them
            return 1;
        }
    }

    /**
     * Remove if it is a spawned falling block
     *
     * @param item the falling block to check
     * @return whether it was ours and we did remove it
     */
    public static boolean removeIfFallen(final FallingBlock item) {
        if (fallingBlocks.contains(item)) {
            fallingBlocks.remove(item);
            item.remove();
            return true;
        }
        return false;
    }

    public static void sortBottomUp(Set<Block> set) {
        Queue<Block> temp = new PriorityQueue<>(Comparator.comparingInt(Block::getY));
        temp.addAll(set);

        set.clear();
        set.addAll(temp);
    }

    public static void sortInsideOut(Set<Block> set, Block bottom) {
        Queue<Block> temp = new PriorityQueue<>((o1, o2) -> (int) (o1.getLocation().distanceSquared(bottom.getLocation()) - o2.getLocation().distanceSquared(bottom.getLocation())));
        temp.addAll(set);

        set.clear();
        set.addAll(temp);
    }
}
