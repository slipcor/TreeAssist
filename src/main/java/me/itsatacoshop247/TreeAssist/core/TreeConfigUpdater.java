package me.itsatacoshop247.TreeAssist.core;

import java.util.ArrayList;
import java.util.List;

public class TreeConfigUpdater {
    enum Adding {
        NEW_AIR_CAVE(8.0f, "overworld", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:cave_air"),
        NEW_AIR_VOID(8.0f, "overworld", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:void_air")
        ;

        private final float version;
        private final String config;
        private final TreeConfig.CFG node;
        private final String value;

        /**
         * An addition definition
         *
         * @param version the version it was introduced
         * @param config the affected config
         * @param node the node to write to
         * @param value the value to add
         */
        Adding(float version, String config, TreeConfig.CFG node, String value) {
            this.version = version;
            this.config = config;
            this.node = node;
            this.value = value;
        }
    }

    enum Removing {
        TRUNK_EDGES_CRIMSON(8.0f, "nether/thick_crimson_fungus", "Trunk.Edges"),
        TRUNK_EDGES_WARPED(8.0f, "nether/thick_warped_fungus", "Trunk.Edges")
        ;

        private final float version;
        private final String config;
        private final String node;

        /**
         * An addition definition
         *
         * @param version the version it was introduced
         * @param config the affected config
         * @param node the node to clear
         */
        Removing(float version, String config, String node) {
            this.version = version;
            this.config = config;
            this.node = node;
        }
    }

    /**
     * Check for changes
     * @param config the TreeConfig
     * @param configPath the path to the config, without tree folder and file extension
     */
    public static void check(TreeConfig config, String configPath) {
        config.preLoad();
        double version = config.getYamlConfiguration().getDouble(TreeConfig.CFG.VERSION.getNode(), 7.0);
        double newVersion = version;
        boolean changed = false;
        for (Adding m : Adding.values()) {
            if (m.version > version && m.config.equals(configPath)) {
                newVersion = Math.max(newVersion, m.version);
                List<String> newList = new ArrayList<>();
                newList.addAll(config.getYamlConfiguration().getStringList(m.node.getNode()));
                newList.add(m.value);
                config.getYamlConfiguration().set(m.node.getNode(), newList);
                Utils.plugin.getLogger().info("Config String value added: " + m.toString());
                changed = true;
            }
        }
        for (Removing m : Removing.values()) {
            if (m.version > version && m.config.equals(configPath)) {
                newVersion = Math.max(newVersion, m.version);
                config.getYamlConfiguration().set(m.node, null);
                Utils.plugin.getLogger().info("Config String value removed: " + m.toString());
                changed = true;
            }
        }
        if (changed) {
            config.getYamlConfiguration().set(TreeConfig.CFG.VERSION.getNode(), newVersion);
            config.save();
        }
    }
}
