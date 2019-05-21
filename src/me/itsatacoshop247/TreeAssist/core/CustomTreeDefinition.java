package me.itsatacoshop247.TreeAssist.core;

import org.bukkit.Material;

import java.util.ArrayList;
import java.util.List;

public class CustomTreeDefinition {
    private final Material sapling;
    private final Material log;
    private final Material leaf;

    public CustomTreeDefinition(Material sapling, Material log, Material leaf) {
        this.sapling = sapling;
        this.log = log;
        this.leaf = leaf;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof CustomTreeDefinition) {
            final CustomTreeDefinition definition = (CustomTreeDefinition) other;
            return
                    definition.sapling == this.sapling &&
                    definition.log == this.log &&
                    definition.leaf == this.leaf;
        }
        return false;
    }

    public Material getLeaf() {
        return leaf;
    }

    public Material getLog() {
        return log;
    }

    public Material getSapling() {
        return sapling;
    }

    public List<String> getList() {
        List<String> definition = new ArrayList<>();
        definition.add(sapling.getKey().getNamespace()+":"+sapling.getKey().getKey());
        definition.add(log.getKey().getNamespace()+":"+log.getKey().getKey());
        definition.add(leaf.getKey().getNamespace()+":"+leaf.getKey().getKey());
        return definition;
    }
}
