package net.slipcor.treeassist.configs;

import net.slipcor.treeassist.TreeAssist;

import java.util.ArrayList;
import java.util.List;

public class TreeConfigUpdater {
    enum Adding {
        NATURAL_LARGE_FERN(7.0095f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:large_fern"),
        //NEW_AIR_VOID(8.0f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:void_air")
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

    enum Updating {
        //MUSHROOM_TRUNK(7.0096f, "mushroom", TreeConfig.CFG.TRUNK_MINIMUM_HEIGHT, 5, 4)
        ;
        private final float version;
        private final String config;
        private final TreeConfig.CFG node;
        private final Object oldValue;
        private final Object newValue;

        Updating(float version, String config, TreeConfig.CFG node, Object oldValue, Object newValue) {
            this.version = version;
            this.config = config;
            this.node = node;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }

    enum Removing {
        MUSHROOM_TRUNK(7.0097f, "mushroom", "Trunk.Minimum Height"),
        //TRUNK_EDGES_WARPED(8.0f, "thick_warped_fungus", "Trunk.Edges")
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
     *
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
                List<String> newList = new ArrayList<>(config.getYamlConfiguration().getStringList(m.node.getNode()));
                newList.add(m.value);
                config.getYamlConfiguration().set(m.node.getNode(), newList);
                TreeAssist.instance.getLogger().info("Config String value added: " + m.toString());
                changed = true;
            }
        }
        for (Removing m : Removing.values()) {
            if (m.version > version && m.config.equals(configPath)) {
                newVersion = Math.max(newVersion, m.version);
                config.getYamlConfiguration().set(m.node, null);
                TreeAssist.instance.getLogger().info("Config String value removed: " + m.toString());
                changed = true;
            }
        }
        for (Updating m : Updating.values()) {
            if (m.version > version && m.config.equals(configPath)) {
                newVersion = Math.max(newVersion, m.version);
                if (m.oldValue.equals(config.getYamlConfiguration().get(m.node.getNode(), null))) {
                    config.getYamlConfiguration().set(m.node.getNode(), m.newValue);
                    TreeAssist.instance.getLogger().info("Config String value updated: " + m.toString());
                    changed = true;
                } else {
                    TreeAssist.instance.getLogger().warning("Config String value not updated: " + m.toString());
                }
            }
        }
        if (changed) {
            config.getYamlConfiguration().set(TreeConfig.CFG.VERSION.getNode(), newVersion);
            config.save();
        }
    }
}
