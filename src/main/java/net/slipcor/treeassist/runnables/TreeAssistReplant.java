package net.slipcor.treeassist.runnables;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.configs.TreeConfig;
import net.slipcor.treeassist.core.TreeStructure;
import net.slipcor.treeassist.utils.BlockUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class TreeAssistReplant implements Runnable {
    private final TreeConfig config;
    public Block block;
    public Material mat;

    /**
     * A Runnable to replant a sapling
     *
     * @param block    the block to replant
     * @param material the material to place
     * @param config   the TreeConfig to check
     */
    public TreeAssistReplant(Block block, Material material, TreeConfig config) {
        this.block = block;
        this.mat = material;
        this.config = config;
    }

    @Override
    public void run() {
        TreeStructure.debug.i("TreeAssistReplant!");
        Material below = this.block.getRelative(BlockFace.DOWN).getType();
        if (TreeAssist.instance.isEnabled() && (config.getMaterials(TreeConfig.CFG.GROUND_BLOCKS).contains(below))) {
            TreeStructure.debug.i("Replanting block: " + BlockUtils.printBlock(block));
            this.block.setType(mat);
            if (config.getInt(TreeConfig.CFG.REPLANTING_DELAY_GROWTH_SECONDS) > 0) {
                TreeAssist.instance.getBlockListener().getAntiGrow().add(this.block, config.getInt(TreeConfig.CFG.REPLANTING_DELAY_GROWTH_SECONDS));
            }
        } else {
            TreeStructure.debug.i("Not a ground block: " + below.name());
        }
    }
}
