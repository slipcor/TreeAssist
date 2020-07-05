package net.slipcor.treeassist.configs;

import net.slipcor.treeassist.TreeAssist;

import java.util.ArrayList;
import java.util.List;

public class TreeConfigUpdater {
    enum Adding {
        NATURAL_LARGE_FERN(7.0095f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:large_fern"),
        TOOL_LIST_NETHERITE(7.0106f, "default", TreeConfig.CFG.TOOL_LIST, "minecraft:netherite_axe"),
        ;

        private final float version;
        private final String config;
        private final TreeConfig.CFG node;
        private final String addition;

        /**
         * An addition definition
         *
         * @param version the version it was introduced
         * @param config the affected config
         * @param node the node to write to
         * @param addition the value to add
         */
        Adding(float version, String config, TreeConfig.CFG node, String addition) {
            this.version = version;
            this.config = config;
            this.node = node;
            this.addition = addition;
        }
    }
    enum PreciseAdding {
        DROP_CHANCE_NETHERITE(7.0106f, "default", "Custom Drop Factor.minecraft:netherite_axe", 1.0),
        ;

        private final float version;
        private final String config;
        private final String node;
        private final Object value;

        /**
         * An addition definition
         *
         * @param version the version it was introduced
         * @param config the affected config
         * @param node the node to write to
         * @param value the value to add
         */
        PreciseAdding(float version, String config, String node, Object value) {
            this.version = version;
            this.config = config;
            this.node = node;
            this.value = value;
        }
    }

    enum Updating {
        CRIMSON_MIDDLE_RADIUS(7.0101f, "crimson_fungus", TreeConfig.CFG.BLOCKS_MIDDLE_RADIUS, 2, 3),
        WARPED_MIDDLE_RADIUS(7.0101f, "warped_fungus", TreeConfig.CFG.BLOCKS_MIDDLE_RADIUS, 2, 3),
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
        CRIMSON_TRUNK_WART(7.0100f, "crimson_fungus", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:nether_wart_block"),
        WARPED_TRUNK_WART(7.0100f, "warped_fungus", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:warped_wart_block"),
        NETHER_TOOL_LIST(7.0103f, "nether", TreeConfig.CFG.TOOL_LIST.getNode()),
        NETHER_TOOL_CHANCES(7.0103f, "nether", TreeConfig.CFG.CUSTOM_DROP_FACTOR.getNode()),
        //TRUNK_EDGES_WARPED(8.0f, "thick_warped_fungus", "Trunk.Edges")
        ;

        private final float version;
        private final String config;
        private final String node;
        private final String removal;

        /**
         * A removing definition
         *
         * @param version the version it was introduced
         * @param config the affected config
         * @param node the node to clear
         */
        Removing(float version, String config, String node) {
            this.version = version;
            this.config = config;
            this.node = node;
            this.removal = null;
        }

        /**
         * An element removing definition
         *
         * @param version the version it was introduced
         * @param config the affected config
         * @param node the node to access
         * @param removal the value to remove
         */
        Removing(float version, String config, TreeConfig.CFG node, String removal) {
            this.version = version;
            this.config = config;
            this.node = node.getNode();
            this.removal = removal;
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
                if (!newList.contains(m.addition)) {
                    newList.add(m.addition);
                    config.getYamlConfiguration().set(m.node.getNode(), newList);
                    TreeAssist.instance.getLogger().info("Config String list value added: " + m.toString());
                }
                changed = true;
            }
        }
        for (PreciseAdding m : PreciseAdding.values()) {
            if (m.version > version && m.config.equals(configPath)) {
                newVersion = Math.max(newVersion, m.version);
                if (config.getYamlConfiguration().get(m.node, null) == null) {
                    config.getYamlConfiguration().set(m.node, m.value);
                    TreeAssist.instance.getLogger().info("Config value added: " + m.toString());
                }
                changed = true;
            }
        }
        for (Removing m : Removing.values()) {
            if (m.version > version && m.config.equals(configPath)) {
                if (m.removal == null) {
                    newVersion = Math.max(newVersion, m.version);
                    config.getYamlConfiguration().set(m.node, null);
                    TreeAssist.instance.getLogger().info("Config String value removed: " + m.toString());
                    changed = true;
                } else {
                    newVersion = Math.max(newVersion, m.version);
                    List<String> values = config.getYamlConfiguration().getStringList(m.node);
                    values.remove(m.removal);
                    config.getYamlConfiguration().set(m.node, values);
                    changed = true;
                }
            }
        }
        for (Updating m : Updating.values()) {
            if (m.version > version && m.config.equals(configPath)) {
                newVersion = Math.max(newVersion, m.version);
                if (m.oldValue.equals(config.getYamlConfiguration().get(m.node.getNode(), m.oldValue))) {
                    config.getYamlConfiguration().set(m.node.getNode(), m.newValue);
                    TreeAssist.instance.getLogger().info("Config value updated: " + m.toString());
                    changed = true;
                } else {
                    TreeAssist.instance.getLogger().warning("Config value not updated: " + m.toString());
                }
            }
        }
        if (changed) {
            config.getYamlConfiguration().set(TreeConfig.CFG.VERSION.getNode(), newVersion);
            config.save();
        }
    }
}
