package net.slipcor.treeassist.discovery;

import net.slipcor.treeassist.yml.TreeConfig;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class LeavesStructure extends TreeStructure {
    /**
     * Create a tree structure only containing extra blocks
     *
     * @param config    the TreeConfig to compare to
     */
    public LeavesStructure(TreeConfig config, Set<Block> extras) {
        super(config);
        this.trunk = new ArrayList<>();
        this.extras = new LinkedHashSet<>();
        this.extras.addAll(extras);
        for (Block block : extras) {
            this.bottom = block;
            break;
        }
    }
}
