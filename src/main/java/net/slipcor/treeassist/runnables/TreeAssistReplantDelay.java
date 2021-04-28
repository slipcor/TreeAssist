package net.slipcor.treeassist.runnables;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.discovery.TreeStructure;
import net.slipcor.treeassist.utils.BlockUtils;
import net.slipcor.treeassist.yml.TreeConfig;
import org.bukkit.block.Block;

import java.util.Set;

public class TreeAssistReplantDelay {
    Runnable runnable;
    int delayTicks;
    TreeStructure tree;
    Block saplingBlock;

    public TreeAssistReplantDelay(TreeStructure tree, Block saplingBlock, Runnable runnable, int delayTicks) {
        this.runnable = runnable;
        this.delayTicks = delayTicks;
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
                .scheduleSyncDelayedTask(TreeAssist.instance, runnable, 20 * delayTicks);
        int timeToProtect = tree.getConfig().getInt(TreeConfig.CFG.REPLANTING_PROTECT_FOR_SECONDS);

        timeToProtect += delayTicks; // prevent the protection running out before the sapling was even planted

        if (timeToProtect > 0) {
            TreeStructure.debug.i("Sapling at " + saplingBlock.getLocation().getBlock() + " will be protected for " + timeToProtect + " seconds");

            if (delayTicks > 0) {
                TreeStructure.debug.i("Sapling will be protected (after " + delayTicks + ") at " + BlockUtils.printBlock(saplingBlock));
                Runnable X = new TreeAssistProtect(saplingBlock.getLocation());
                TreeAssist.instance
                        .getServer()
                        .getScheduler()
                        .scheduleSyncDelayedTask(
                                TreeAssist.instance,
                                () -> TreeAssist.instance.saplingLocationList.add(saplingBlock.getLocation()),
                                20 * delayTicks);
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
