package net.slipcor.treeassist.runnables;

import net.slipcor.core.CoreDebugger;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.discovery.FailReason;
import net.slipcor.treeassist.discovery.TreeStructure;
import net.slipcor.treeassist.utils.BlockUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Set;

public class CleanRunner extends BukkitRunnable {
    private final TreeStructure me;
    private final int offset;
    private final Set<Block> removeBlocks;
    public static CoreDebugger debug;
    private final Material sapling;

    public CleanRunner(TreeStructure tree, int offset, Set<Block> removeBlocks, Material sapling) {
        me = tree;
        this.offset = offset;
        this.removeBlocks = removeBlocks;
        this.sapling = sapling;
    }

    @Override
    public void run() {
        if (offset < 0) {
            for (Block block : removeBlocks) {
                if (sapling.equals(block.getType())) {
                    debug.i("CleanRunner: skipping breaking a sapling");
                    continue;
                }
                if (block.getType().isAir()) {
                    debug.i("CleanRunner: skipping air");
                    continue;
                }
                debug.i("CleanRunner - breaking block A: " + BlockUtils.printBlock(block));
                BlockUtils.breakBlock(block);
            }
        } else {
            for (Block block : removeBlocks) {
                if (sapling.equals(block.getType())) {
                    debug.i("CleanRunner: skipping breaking a sapling");
                    continue;
                }
                if (block.getType().isAir()) {
                    debug.i("CleanRunner: skipping air");
                    continue;
                }
                debug.i("CleanRunner - breaking block B: " + BlockUtils.printBlock(block));
                BlockUtils.breakBlock(block);
                TreeAssist.instance.blockList.logBreak(block, null);
                removeBlocks.remove(block);
                return;
            }
        }
        me.plantSaplings();
        removeBlocks.clear();

        me.setValid(false);
        me.failReason = FailReason.INVALID_BLOCK;
        try {
            TreeAssist.instance.treeRemove(me);
            this.cancel();
        } catch (Exception e) {
        }
    }

}
