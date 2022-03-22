package net.slipcor.treeassist.runnables;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.core.TreeAssistDebugger;
import net.slipcor.treeassist.discovery.FailReason;
import net.slipcor.treeassist.discovery.LeavesStructure;
import net.slipcor.treeassist.discovery.TreeStructure;
import net.slipcor.treeassist.utils.BlockUtils;
import net.slipcor.treeassist.yml.MainConfig;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Set;

public class CleanRunner extends BukkitRunnable {
    private final TreeStructure me;
    private final int offset;
    private final Set<Block> removeBlocks;
    public static TreeAssistDebugger debug;
    private final Material sapling;
    private final boolean cleanUpLeaves;
    private final List<Material> leafMaterials;

    public CleanRunner(TreeStructure tree, int offset, Set<Block> removeBlocks, Material sapling, boolean cleanUpLeaves, List<Material> leafMaterials) {
        me = tree;
        this.offset = offset;
        this.removeBlocks = removeBlocks;
        this.sapling = sapling;
        this.cleanUpLeaves = cleanUpLeaves;
        this.leafMaterials = leafMaterials;
    }

    @Override
    public void run() {
        boolean generateDrops = (me instanceof LeavesStructure) && TreeAssist.instance.config().getBoolean(MainConfig.CFG.DESTRUCTION_FAST_LEAF_DECAY_REGULAR_DROPS);
        debug.i("CleanRunner: will generate drops: " + generateDrops);
        ItemStack breakTool = generateDrops ? new ItemStack(Material.AIR, 1) : null;
        if (offset < 0) {
            for (Block block : removeBlocks) {
                if (sapling.equals(block.getType())) {
                    debug.i("CleanRunner 1: skipping breaking a sapling");
                    continue;
                }
                if (block.getType().isAir()) {
                    debug.i("CleanRunner 1: skipping air");
                    continue;
                }
                if (!cleanUpLeaves && leafMaterials.contains(block.getType())) {
                    debug.i("CleanRunner 1: skipping leaf");
                    continue;
                }
                debug.i("CleanRunner - breaking block A: " + BlockUtils.printBlock(block));
                BlockUtils.breakBlock(null, block, breakTool, block.getY()-1);
            }
        } else {
            for (Block block : removeBlocks) {
                if (sapling.equals(block.getType())) {
                    debug.i("CleanRunner 2: skipping breaking a sapling");
                    continue;
                }
                if (block.getType().isAir()) {
                    debug.i("CleanRunner 2: skipping air");
                    continue;
                }
                if (!cleanUpLeaves && leafMaterials.contains(block.getType())) {
                    debug.i("CleanRunner 2: skipping leaf");
                    continue;
                }
                debug.i("CleanRunner - breaking block B: " + BlockUtils.printBlock(block));
                BlockUtils.breakBlock(null, block, breakTool, block.getY()-1);

                TreeAssist.instance.blockList.logBreak(block, null);
                removeBlocks.remove(block);
                return;
            }
        }
        me.plantSaplings();
        removeBlocks.clear();

        me.setValid(false);
        me.setFailReason(FailReason.INVALID_BLOCK);
        try {
            TreeAssist.instance.treeRemove(me);
            this.cancel();
        } catch (Exception e) {
        }
    }

}
