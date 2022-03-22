package net.slipcor.treeassist.yml;

import net.slipcor.treeassist.TreeAssist;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TreeConfigUpdater {
    enum Adding {
        NATURAL_LARGE_FERN(7.0095f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:large_fern"),
        TOOL_LIST_NETHERITE(7.0106f, "default", TreeConfig.CFG.TOOL_LIST, "minecraft:netherite_axe"),
        NATURAL_AZURE_CORRECT(7.0108f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:azure_bluet"),
        NATURAL_COCOA_CORRECT(7.0108f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:cocoa"),
        NATURAL_PEONY(7.2018f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:peony"),
        TRUNK_STRIPPED_CRIMSON_FUNGUS(7.2018f, "crimson_fungus", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:stripped_crimson_stem"),
        TRUNK_STRIPPED_WARPED_FUNGUS(7.2018f, "warped_fungus", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:stripped_warped_stem"),
        TRUNK_STRIPPED_ACACIA(7.2018f, "acacia", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:stripped_acacia_log"),
        TRUNK_STRIPPED_BIRCH(7.2018f, "birch", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:stripped_birch_log"),
        TRUNK_STRIPPED_DARK_OAK(7.2018f, "dark_oak", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:stripped_dark_oak_log"),
        TRUNK_STRIPPED_JUNGLE(7.2018f, "jungle", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:stripped_jungle_log"),
        TRUNK_STRIPPED_OAK(7.2018f, "oak", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:stripped_oak_log"),
        TRUNK_STRIPPED_SPRUCE(7.2018f, "spruce", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:stripped_spruce_log"),
        NETHER_NATURAL_BLOCK(7.2018f, "nether", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:netherrack"),
        NATURAL_RED_SHROOM(7.2020f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:brown_mushroom"),
        NATURAL_BROWN_SHROOM(7.2020f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:red_mushroom"),
        GROUND_MUSHROOM_CRIMSON_NYLIUM(7.3014f, "mushroom", TreeConfig.CFG.GROUND_BLOCKS, "minecraft:crimson_nylium"),
        GROUND_MUSHROOM_WARPED_NYLIUM(7.3014f, "mushroom", TreeConfig.CFG.GROUND_BLOCKS, "minecraft:warped_nylium"),
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
        TALL_JUNGLE_VINES(7.0118f, "tall_jungle", TreeConfig.CFG.BLOCKS_VINES.getNode(), true),
        SILK_TOUCH(7.0137f, "default", TreeConfig.CFG.AUTOMATIC_DESTRUCTION_USE_SILK_TOUCH.getNode(), true),
        AUTOMATIC_DESTRUCTION_CUSTOM_DROPS_OVERRIDE(7.1003f, "default", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_OVERRIDE.getNode(), false),
        DARK_OAK_MINIMUM_HEIGHT(7.1008f, "dark_oak", TreeConfig.CFG.TRUNK_MINIMUM_HEIGHT.getNode(), 3),
        TOOL_DAMAGE_FOR_LEAVES(7.1010f, "default", TreeConfig.CFG.AUTOMATIC_DESTRUCTION_TOOL_DAMAGE_FOR_LEAVES.getNode(), true),

        TRUNK_CUSTOM_DROPS_ACTIVE(7.1012f, "default", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_ACTIVE.getNode(), false),
        TRUNK_CUSTOM_DROPS_OVERRIDE(7.1012f, "default", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_OVERRIDE.getNode(), false),

        TRUNK_DROP_CHANCE_NETHERITE_AXE(7.1012f, "default", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_FACTORS.getNode() + ".minecraft:netherite_axe", 1.0),
        TRUNK_DROP_CHANCE_DIAMOND_AXE(7.1012f, "default", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_FACTORS.getNode() + ".minecraft:diamond_axe", 1.0),
        TRUNK_DROP_CHANCE_GOLD_AXE(7.1012f, "default", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_FACTORS.getNode() + ".minecraft:golden_axe", 0.75),
        TRUNK_DROP_CHANCE_IRON_AXE(7.1012f, "default", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_FACTORS.getNode() + ".minecraft:iron_axe", 0.5),
        TRUNK_DROP_CHANCE_STONE_AXE(7.1012f, "default", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_FACTORS.getNode() + ".minecraft:stone_axe", 0.25),
        TRUNK_DROP_CHANCE_WOOD_AXE(7.1012f, "default", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_FACTORS.getNode() + ".minecraft:wooden_axe", 0.1),

        TRUNK_DROP_FACTOR_GOLDEN(7.1012f, "default", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_ITEMS.getNode() + ".minecraft:golden_apple", 0.0001),

        NETHER_LEAF_GAPPLE(7.1019f, "nether", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_ITEMS.getNode() + ".minecraft:golden_apple", 0f),
        NETHER_LEAF_APPLE(7.1019f, "nether", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_ITEMS.getNode() + ".minecraft:apple", 0f),
        NETHER_TRUNK_GAPPLE(7.1019f, "nether", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_ITEMS.getNode() + ".minecraft:golden_apple", 0f),

        REPLANTING_CHECK_DEPTH(7.2051f, "default", TreeConfig.CFG.REPLANTING_CHECK_DEPTH.getNode(), 12),
        REPLANTING_CHECK_DEPTH_OAK(7.2051f, "oak", TreeConfig.CFG.REPLANTING_CHECK_DEPTH.getNode(), 14),
        REPLANTING_CHECK_DEPTH_SPRUCE(7.2051f, "tall_spruce", TreeConfig.CFG.REPLANTING_CHECK_DEPTH.getNode(), 24),
        REPLANTING_CHECK_DEPTH_JUNGLE(7.2051f, "tall_jungle", TreeConfig.CFG.REPLANTING_CHECK_DEPTH.getNode(), 40),
        REPLANTING_CHECK_DEPTH_C_FUNGUS(7.2051f, "crimson_fungus", TreeConfig.CFG.REPLANTING_CHECK_DEPTH.getNode(), 26),
        REPLANTING_CHECK_DEPTH_W_FUNGUS(7.2051f, "warped_fungus", TreeConfig.CFG.REPLANTING_CHECK_DEPTH.getNode(), 26),
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
        TALL_JUNGLE_TOP_LEAVES(7.0118f, "tall_jungle", TreeConfig.CFG.BLOCKS_TOP_RADIUS, 3, 6),
        TALL_JUNGLE_MIDDLE_LEAVES(7.0118f, "tall_jungle", TreeConfig.CFG.BLOCKS_MIDDLE_RADIUS, 2, 6),
        ACACIA_MIDDLE_LEAVES(7.2047f, "acacia", TreeConfig.CFG.BLOCKS_MIDDLE_RADIUS, 2, 3),
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

    enum MapMoving {
        CUSTOM_DROPS_ITEMS(7.1012f, null, "Custom Drops", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_ITEMS),
        CUSTOM_DROPS_FACTORS(7.1012f, null, "Custom Drop Factor", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_FACTORS);

        private final float version;
        private final String config;
        private final String source;
        private final TreeConfig.CFG destination;

        /**
         * A map moving definition
         *
         * @param version the version it was introduced
         * @param config the affected config (or null if all)
         * @param source the node to read
         * @param destination the node to write
         */
        MapMoving(float version, String config, String source, TreeConfig.CFG destination) {
            this.version = version;
            this.config = config;
            this.source = source;
            this.destination = destination;
        }
    }

    enum Moving {
        CUSTOM_DROPS_ACTIVE(7.1012f, null, "Blocks.Custom Drops", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_ACTIVE.getNode()),
        CUSTOM_DROPS_OVERRIDE(7.1012f, null, "Automatic Destruction.Custom Drops Override", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_OVERRIDE.getNode()),

        REPLANTING_GROWTH_DELAY_SECONDS(7.3016f, null, "Replanting.Delay Growth Seconds", TreeConfig.CFG.REPLANTING_GROWTH_DELAY_SECONDS.getNode()),

        NETHER_LEAF_GAPPLE(7.1020f, "nether", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_ITEMS.getNode() + "minecraft:golden_apple", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_ITEMS.getNode() + ".minecraft:golden_apple"),
        NETHER_LEAF_APPLE(7.1020f, "nether", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_ITEMS.getNode() + "minecraft:apple", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_ITEMS.getNode() + ".minecraft:apple"),
        NETHER_TRUNK_GAPPLE(7.1020f, "nether", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_ITEMS.getNode() + "minecraft:golden_apple", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_ITEMS.getNode() + ".minecraft:golden_apple"),
        ;

        private final float version;
        private final String config;
        private final String source;
        private final String destination;

        /**
         * A moving definition
         *
         * @param version the version it was introduced
         * @param config the affected config (or null if all)
         * @param source the node to read
         * @param destination the node to write
         */
        Moving(float version, String config, String source, String destination) {
            this.version = version;
            this.config = config;
            this.source = source;
            this.destination = destination;
        }
    }

    enum Removing {
        MUSHROOM_TRUNK(7.0097f, "mushroom", "Trunk.Minimum Height"),
        CRIMSON_TRUNK_WART(7.0100f, "crimson_fungus", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:nether_wart_block"),
        WARPED_TRUNK_WART(7.0100f, "warped_fungus", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:warped_wart_block"),
        NETHER_TOOL_LIST(7.0103f, "nether", TreeConfig.CFG.TOOL_LIST.getNode()),
        NETHER_TOOL_CHANCES(7.0103f, "nether", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_FACTORS.getNode()),
        NATURAL_AZURE_WRONG(7.0108f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:azure-bluet"),
        NATURAL_COCOA_WRONG(7.0108f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:cococa"),
        DEFAULT_BEDROCK(7.2028f, "default", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:bedrock"),
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
        boolean verbose = TreeAssist.instance.config().getBoolean(MainConfig.CFG.GENERAL_VERBOSE_CONFIG_LOADING);

        for (Adding m : Adding.values()) {
            if (m.version > version && m.config.equals(configPath)) {
                newVersion = Math.max(newVersion, m.version);
                List<String> newList = new ArrayList<>(config.getYamlConfiguration().getStringList(m.node.getNode()));
                if (!newList.contains(m.addition)) {
                    newList.add(m.addition);
                    config.getYamlConfiguration().set(m.node.getNode(), newList);
                    if (verbose) {
                        TreeAssist.instance.getLogger().info("Config String list value added: " + m.toString());
                    }
                }
                changed = true;
            }
        }

        for (Moving m : Moving.values()) {
            if (m.version > version && (m.config == null || m.config.equals(configPath))) {
                newVersion = Math.max(newVersion, m.version);
                Object moving = config.getYamlConfiguration().get(m.source);
                if (moving != null) {
                    config.getYamlConfiguration().set(m.destination, moving);
                    if (!m.destination.startsWith(m.source)) {
                        config.getYamlConfiguration().set(m.source, null);
                    }

                    if (verbose) {
                        TreeAssist.instance.getLogger().info("Config value moved: " + m.toString());
                    }
                }
                changed = true;
            }
        }

        for (MapMoving m : MapMoving.values()) {
            if (m.version > version && (m.config == null || m.config.equals(configPath))) {
                newVersion = Math.max(newVersion, m.version);
                ConfigurationSection section = config.getYamlConfiguration().getConfigurationSection(m.source);
                if (section != null) {
                    Set<String> set = section.getKeys(true);

                    for (String node : set) {
                        config.getYamlConfiguration().set(m.destination.getNode() + "." + node, config.getYamlConfiguration().get(m.source + "." + node));
                    }

                    if (!set.isEmpty() && verbose) {
                        TreeAssist.instance.getLogger().info("Config value moved: " + m.toString());
                    }

                    config.getYamlConfiguration().set(m.source, null);
                }
                changed = true;
            }
        }

        for (PreciseAdding m : PreciseAdding.values()) {
            if (m.version > version && m.config.equals(configPath)) {
                newVersion = Math.max(newVersion, m.version);
                if (config.getYamlConfiguration().get(m.node, null) == null) {
                    config.getYamlConfiguration().set(m.node, m.value);

                    if (verbose) {
                        TreeAssist.instance.getLogger().info("Config value added: " + m.toString());
                    }
                }
                changed = true;
            }
        }
        for (Removing m : Removing.values()) {
            if (m.version > version && m.config.equals(configPath)) {
                if (m.removal == null) {
                    newVersion = Math.max(newVersion, m.version);
                    config.getYamlConfiguration().set(m.node, null);

                    if (verbose) {
                        TreeAssist.instance.getLogger().info("Config String value removed: " + m.toString());
                    }
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

                    if (verbose) {
                        TreeAssist.instance.getLogger().info("Config value updated: " + m.toString());
                    }
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
