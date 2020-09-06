package net.slipcor.treeassist.core;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.configs.TreeConfig;
import net.slipcor.treeassist.runnables.TreeAssistProtect;
import net.slipcor.treeassist.utils.BlockUtils;
import org.bukkit.block.Block;

import java.util.Set;

public class TreeAssistReplantDelay {
    Runnable b;
    int delay;
    TreeStructure tree;
    Block saplingBlock;

    public TreeAssistReplantDelay(TreeStructure tree, Block saplingBlock, Runnable b, int delay) {
        this.b = b;
        this.delay = delay;
        this.tree = tree;
        this.saplingBlock = saplingBlock;
    }

    public void commit() {

        // look for close trees that might grow into us

        int distance = tree.getConfig().getInt(TreeConfig.CFG.BLOCKS_MIDDLE_RADIUS) * 2;

        Set<TreeStructure> trees = TreeAssist.instance.treesThatQualify(tree.getConfig(), saplingBlock, distance * distance);

        for (TreeStructure tree : trees) {
            if (tree.isValid() && !tree.equals(this.tree)) {
                tree.addReplantDelay(this);
                TreeStructure.debug.i("moving sapling from " + this.tree + " to " + tree);
                return;
            }
        }
        TreeStructure.debug.i("committing TreeAssistReplantDelay " + this.tree);

        TreeAssist.instance.getServer().getScheduler()
                .scheduleSyncDelayedTask(TreeAssist.instance, b, 20 * delay);
        int timeToProtect = tree.getConfig().getInt(TreeConfig.CFG.REPLANTING_PROTECT_FOR_SECONDS);

        timeToProtect += delay; // prevent the protection running out before the sapling was even planted

        if (timeToProtect > 0) {
            TreeStructure.debug.i("Sapling at " + saplingBlock.getLocation().getBlock() + " will be protected for " + timeToProtect + " seconds");

            if (delay > 0) {
                TreeStructure.debug.i("Sapling will be protected (after " + delay + ") at " + BlockUtils.printBlock(saplingBlock));
                Runnable X = new TreeAssistProtect(saplingBlock.getLocation());
                TreeAssist.instance
                        .getServer()
                        .getScheduler()
                        .scheduleSyncDelayedTask(
                                TreeAssist.instance,
                                () -> TreeAssist.instance.saplingLocationList.add(saplingBlock.getLocation()),
                                20 * delay);
                TreeAssist.instance
                        .getServer()
                        .getScheduler()
                        .scheduleSyncDelayedTask(
                                TreeAssist.instance,
                                X, 20 * timeToProtect);

            } else {
                TreeStructure.debug.i("Sapling will be protected at " + BlockUtils.printBlock(saplingBlock));
                TreeAssist.instance.saplingLocationList.add(saplingBlock.getLocation());
                Runnable X = new TreeAssistProtect(saplingBlock.getLocation());
                TreeAssist.instance
                        .getServer()
                        .getScheduler()
                        .scheduleSyncDelayedTask(
                                TreeAssist.instance,
                                X, 20 * timeToProtect);
            }
        } else  {
            TreeStructure.debug.i("Saplings do not need to be protected");
        }
    }

    public void setTree(TreeStructure tree) {
        this.tree = tree;
    }
}
