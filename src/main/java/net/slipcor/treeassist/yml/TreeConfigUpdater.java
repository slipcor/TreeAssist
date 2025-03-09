package net.slipcor.treeassist.yml;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TreeConfigUpdater {
    enum Adding {
        NATURAL_LARGE_FERN(7.0095f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:large_fern", new int[] {1, 7, 2}),
        TOOL_LIST_NETHERITE(7.0106f, "default", TreeConfig.CFG.TOOL_LIST, "minecraft:netherite_axe", new int[] {1, 7, 2}),
        NATURAL_AZURE_CORRECT(7.0108f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:azure_bluet", new int[] {1, 7, 2}),
        NATURAL_COCOA_CORRECT(7.0108f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:cocoa", new int[] {1, 7, 2}),
        NATURAL_PEONY(7.2018f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:peony", new int[] {1, 7, 2}),
        TRUNK_STRIPPED_CRIMSON_FUNGUS(7.2018f, "crimson_fungus", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:stripped_crimson_stem", new int[] {1, 13, 0}),
        TRUNK_STRIPPED_WARPED_FUNGUS(7.2018f, "warped_fungus", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:stripped_warped_stem", new int[] {1, 13, 0}),
        TRUNK_STRIPPED_ACACIA(7.2018f, "acacia", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:stripped_acacia_log", new int[] {1, 13, 0}),
        TRUNK_STRIPPED_BIRCH(7.2018f, "birch", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:stripped_birch_log", new int[] {1, 13, 0}),
        TRUNK_STRIPPED_DARK_OAK(7.2018f, "dark_oak", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:stripped_dark_oak_log", new int[] {1, 13, 0}),
        TRUNK_STRIPPED_JUNGLE(7.2018f, "jungle", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:stripped_jungle_log", new int[] {1, 13, 0}),
        TRUNK_STRIPPED_OAK(7.2018f, "oak", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:stripped_oak_log", new int[] {1, 13, 0}),
        TRUNK_STRIPPED_SPRUCE(7.2018f, "spruce", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:stripped_spruce_log", new int[] {1, 13, 0}),
        NETHER_NATURAL_BLOCK(7.2018f, "nether", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:netherrack", new int[] {1, 13, 0}),
        NATURAL_RED_SHROOM(7.2020f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:brown_mushroom", new int[] {1, 13, 0}),
        NATURAL_BROWN_SHROOM(7.2020f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:red_mushroom", new int[] {1, 13, 0}),
        GROUND_MUSHROOM_CRIMSON_NYLIUM(7.3014f, "mushroom", TreeConfig.CFG.GROUND_BLOCKS, "minecraft:crimson_nylium", new int[] {1, 16, 0}),
        GROUND_MUSHROOM_WARPED_NYLIUM(7.3014f, "mushroom", TreeConfig.CFG.GROUND_BLOCKS, "minecraft:warped_nylium", new int[] {1, 16, 0}),
        NATURAL_PINK_PETALS(7.3043f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:pink_petals", new int[] {1, 20, 0}),
        DEFAULT_SHORT_GRASS(7.3051f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:short_grass", new int[] {1, 20, 4}),
        DEFAULT_RESIN_CLUMP(7.3058f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:resin_clump", new int[] {1, 21, 4}),
        DEFAULT_PALE_HANGING_MOSS(7.3058f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:pale_hanging_moss", new int[] {1, 21, 4}),
        DEFAULT_PALE_MOSS_BLOCK(7.3064f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:pale_moss_block", new int[] {1, 21, 4}),
        DEFAULT_PALE_MOSS_CARPET(7.3064f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:pale_moss_carpet", new int[] {1, 21, 4}),
        DEFAULT_EYEBLOSSOM(7.3064f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:*_eyeblossom", new int[] {1, 21, 4}),
        ;

        private final float version;
        private final String config;
        private final TreeConfig.CFG node;
        private final String addition;
        private final int[] mcversion;

        /**
         * An addition definition
         *
         * @param version the plugin version it was introduced
         * @param config the affected config
         * @param node the node to write to
         * @param addition the value to add
         * @param mcversion the minecraft version it was introduced
         */
        Adding(float version, String config, TreeConfig.CFG node, String addition, int[] mcversion) {
            this.version = version;
            this.config = config;
            this.node = node;
            this.addition = addition;
            this.mcversion = mcversion;
        }
    }

    enum PreciseAdding {
        TALL_JUNGLE_VINES(7.0118f, "tall_jungle", TreeConfig.CFG.BLOCKS_VINES.getNode(), true, new int[] {1, 7, 2}),
        SILK_TOUCH(7.0137f, "default", TreeConfig.CFG.AUTOMATIC_DESTRUCTION_USE_SILK_TOUCH.getNode(), true, new int[] {1, 7, 2}),
        AUTOMATIC_DESTRUCTION_CUSTOM_DROPS_OVERRIDE(7.1003f, "default", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_OVERRIDE.getNode(), false, new int[] {1, 7, 2}),
        DARK_OAK_MINIMUM_HEIGHT(7.1008f, "dark_oak", TreeConfig.CFG.TRUNK_MINIMUM_HEIGHT.getNode(), 3, new int[] {1, 7, 2}),
        TOOL_DAMAGE_FOR_LEAVES(7.1010f, "default", TreeConfig.CFG.AUTOMATIC_DESTRUCTION_TOOL_DAMAGE_FOR_LEAVES.getNode(), true, new int[] {1, 7, 2}),

        TRUNK_CUSTOM_DROPS_ACTIVE(7.1012f, "default", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_ACTIVE.getNode(), false, new int[] {1, 7, 2}),
        TRUNK_CUSTOM_DROPS_OVERRIDE(7.1012f, "default", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_OVERRIDE.getNode(), false, new int[] {1, 7, 2}),

        TRUNK_DROP_CHANCE_NETHERITE_AXE(7.1012f, "default", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_FACTORS.getNode() + ".minecraft:netherite_axe", 1.0, new int[] {1, 7, 2}),
        TRUNK_DROP_CHANCE_DIAMOND_AXE(7.1012f, "default", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_FACTORS.getNode() + ".minecraft:diamond_axe", 1.0, new int[] {1, 7, 2}),
        TRUNK_DROP_CHANCE_GOLD_AXE(7.1012f, "default", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_FACTORS.getNode() + ".minecraft:golden_axe", 0.75, new int[] {1, 7, 2}),
        TRUNK_DROP_CHANCE_IRON_AXE(7.1012f, "default", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_FACTORS.getNode() + ".minecraft:iron_axe", 0.5, new int[] {1, 7, 2}),
        TRUNK_DROP_CHANCE_STONE_AXE(7.1012f, "default", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_FACTORS.getNode() + ".minecraft:stone_axe", 0.25, new int[] {1, 7, 2}),
        TRUNK_DROP_CHANCE_WOOD_AXE(7.1012f, "default", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_FACTORS.getNode() + ".minecraft:wooden_axe", 0.1, new int[] {1, 7, 2}),

        TRUNK_DROP_FACTOR_GOLDEN(7.1012f, "default", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_ITEMS.getNode() + ".minecraft:golden_apple", 0.0001, new int[] {1, 7, 2}),

        NETHER_LEAF_GAPPLE(7.1019f, "nether", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_ITEMS.getNode() + ".minecraft:golden_apple", 0f, new int[] {1, 7, 2}),
        NETHER_LEAF_APPLE(7.1019f, "nether", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_ITEMS.getNode() + ".minecraft:apple", 0f, new int[] {1, 7, 2}),
        NETHER_TRUNK_GAPPLE(7.1019f, "nether", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_ITEMS.getNode() + ".minecraft:golden_apple", 0f, new int[] {1, 7, 2}),

        REPLANTING_CHECK_DEPTH(7.2051f, "default", TreeConfig.CFG.REPLANTING_CHECK_DEPTH.getNode(), 12, new int[] {1, 13, 0}),
        REPLANTING_CHECK_DEPTH_OAK(7.2051f, "oak", TreeConfig.CFG.REPLANTING_CHECK_DEPTH.getNode(), 14, new int[] {1, 13, 0}),
        REPLANTING_CHECK_DEPTH_SPRUCE(7.2051f, "tall_spruce", TreeConfig.CFG.REPLANTING_CHECK_DEPTH.getNode(), 24, new int[] {1, 13, 0}),
        REPLANTING_CHECK_DEPTH_JUNGLE(7.2051f, "tall_jungle", TreeConfig.CFG.REPLANTING_CHECK_DEPTH.getNode(), 40, new int[] {1, 13, 0}),
        REPLANTING_CHECK_DEPTH_C_FUNGUS(7.2051f, "crimson_fungus", TreeConfig.CFG.REPLANTING_CHECK_DEPTH.getNode(), 26, new int[] {1, 13, 0}),
        REPLANTING_CHECK_DEPTH_W_FUNGUS(7.2051f, "warped_fungus", TreeConfig.CFG.REPLANTING_CHECK_DEPTH.getNode(), 26, new int[] {1, 13, 0}),

        AUTOMATIC_DESTRUCTION_CLEANUP_LEAVES(7.3018f, "default", TreeConfig.CFG.AUTOMATIC_DESTRUCTION_CLEANUP_LEAVES.getNode(), true, new int[] {1, 16, 0}),
        TRUNK_GREEDY(7.3025f, "default", TreeConfig.CFG.TRUNK_GREEDY.getNode(), false, new int[] {1, 16, 0}),
        AUTOMATIC_DESTRUCTION_AUTO_ADD_ONLY_LOGS_TO_INVENTORY(7.3037f, "default", TreeConfig.CFG.AUTOMATIC_DESTRUCTION_AUTO_ADD_ONLY_LOGS_TO_INVENTORY.getNode(), false, new int[] {1, 16, 0}),
        DEFAULT_MAXIMUM_HEIGHT(7.3041f, "default", TreeConfig.CFG.TRUNK_MAXIMUM_HEIGHT.getNode(), -1, new int[] {1, 16, 0}),

        AUTOMATIC_DESTRUCTION_AUTO_ADD_DROP_FAILED(7.3055f, "default", TreeConfig.CFG.AUTOMATIC_DESTRUCTION_AUTO_ADD_DROP_FAILED.getNode(), false, new int[] {1, 13, 0})
        ;

        private final float version;
        private final String config;
        private final String node;
        private final Object value;
        private final int[] mcversion;

        /**
         * An addition definition
         *
         * @param version the plugin version it was introduced
         * @param config the affected config
         * @param node the node to write to
         * @param value the value to add
         * @param mcversion the minecraft version it was introduced
         */
        PreciseAdding(float version, String config, String node, Object value, int[] mcversion) {
            this.version = version;
            this.config = config;
            this.node = node;
            this.value = value;
            this.mcversion = mcversion;
        }
    }

    enum Updating {
        CRIMSON_MIDDLE_RADIUS(7.0101f, "crimson_fungus", TreeConfig.CFG.BLOCKS_MIDDLE_RADIUS, 2, 3, new int[] {1, 7, 2}),
        WARPED_MIDDLE_RADIUS(7.0101f, "warped_fungus", TreeConfig.CFG.BLOCKS_MIDDLE_RADIUS, 2, 3, new int[] {1, 7, 2}),
        TALL_JUNGLE_TOP_LEAVES(7.0118f, "tall_jungle", TreeConfig.CFG.BLOCKS_TOP_RADIUS, 3, 6, new int[] {1, 7, 2}),
        TALL_JUNGLE_MIDDLE_LEAVES(7.0118f, "tall_jungle", TreeConfig.CFG.BLOCKS_MIDDLE_RADIUS, 2, 6, new int[] {1, 7, 2}),
        ACACIA_MIDDLE_LEAVES(7.2047f, "acacia", TreeConfig.CFG.BLOCKS_MIDDLE_RADIUS, 2, 3, new int[] {1, 13, 0}),
        ;
        private final float version;
        private final String config;
        private final TreeConfig.CFG node;
        private final Object oldValue;
        private final Object newValue;
        private final int[] mcversion;

        Updating(float version, String config, TreeConfig.CFG node, Object oldValue, Object newValue, int[] mcversion) {
            this.version = version;
            this.config = config;
            this.node = node;
            this.oldValue = oldValue;
            this.newValue = newValue;
            this.mcversion = mcversion;
        }
    }

    enum MapMoving {
        CUSTOM_DROPS_ITEMS(7.1012f, null, "Custom Drops", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_ITEMS, new int[] {1, 7, 2}),
        CUSTOM_DROPS_FACTORS(7.1012f, null, "Custom Drop Factor", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_FACTORS, new int[] {1, 7, 2});

        private final float version;
        private final String config;
        private final String source;
        private final TreeConfig.CFG destination;
        private final int[] mcversion;

        /**
         * A map moving definition
         *
         * @param version the plugin version it was introduced
         * @param config the affected config (or null if all)
         * @param source the node to read
         * @param destination the node to write
         * @param mcversion the minecraft version it was introduced
         */
        MapMoving(float version, String config, String source, TreeConfig.CFG destination, int[] mcversion) {
            this.version = version;
            this.config = config;
            this.source = source;
            this.destination = destination;
            this.mcversion = mcversion;
        }
    }

    enum Moving {
        CUSTOM_DROPS_ACTIVE(7.1012f, null, "Blocks.Custom Drops", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_ACTIVE.getNode(), new int[] {1, 7, 2}),
        CUSTOM_DROPS_OVERRIDE(7.1012f, null, "Automatic Destruction.Custom Drops Override", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_OVERRIDE.getNode(), new int[] {1, 7, 2}),

        REPLANTING_GROWTH_DELAY_SECONDS(7.3016f, null, "Replanting.Delay Growth Seconds", TreeConfig.CFG.REPLANTING_GROWTH_DELAY_SECONDS.getNode(), new int[] {1, 16, 0}),

        NETHER_LEAF_GAPPLE(7.1020f, "nether", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_ITEMS.getNode() + "minecraft:golden_apple", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_ITEMS.getNode() + ".minecraft:golden_apple", new int[] {1, 7, 2}),
        NETHER_LEAF_APPLE(7.1020f, "nether", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_ITEMS.getNode() + "minecraft:apple", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_ITEMS.getNode() + ".minecraft:apple", new int[] {1, 7, 2}),
        NETHER_TRUNK_GAPPLE(7.1020f, "nether", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_ITEMS.getNode() + "minecraft:golden_apple", TreeConfig.CFG.TRUNK_CUSTOM_DROPS_ITEMS.getNode() + ".minecraft:golden_apple", new int[] {1, 7, 2}),
        ;

        private final float version;
        private final String config;
        private final String source;
        private final String destination;
        private final int[] mcversion;

        /**
         * A moving definition
         *
         * @param version the plugin version it was introduced
         * @param config the affected config (or null if all)
         * @param source the node to read
         * @param destination the node to write
         * @param mcversion the minecraft version it was introduced
         */
        Moving(float version, String config, String source, String destination, int[] mcversion) {
            this.version = version;
            this.config = config;
            this.source = source;
            this.destination = destination;
            this.mcversion = mcversion;
        }
    }

    enum Removing {
        MUSHROOM_TRUNK(7.0097f, "mushroom", "Trunk.Minimum Height", new int[] {1, 7, 2}),
        CRIMSON_TRUNK_WART(7.0100f, "crimson_fungus", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:nether_wart_block", new int[] {1, 7, 2}),
        WARPED_TRUNK_WART(7.0100f, "warped_fungus", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:warped_wart_block", new int[] {1, 7, 2}),
        NETHER_TOOL_LIST(7.0103f, "nether", TreeConfig.CFG.TOOL_LIST.getNode(), new int[] {1, 7, 2}),
        NETHER_TOOL_CHANCES(7.0103f, "nether", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_FACTORS.getNode(), new int[] {1, 7, 2}),
        NATURAL_AZURE_WRONG(7.0108f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:azure-bluet", new int[] {1, 7, 2}),
        NATURAL_COCOA_WRONG(7.0108f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:cococa", new int[] {1, 7, 2}),
        DEFAULT_BEDROCK(7.2028f, "default", TreeConfig.CFG.TRUNK_MATERIALS, "minecraft:bedrock", new int[] {1, 13, 0}),
        NETHER_QUARTZ(7.3048f, "nether", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:nether_quartz_ore", new int[] {1, 16, 0}),
        DEFAULT_GRASS(7.3051f, "default", TreeConfig.CFG.NATURAL_BLOCKS, "minecraft:grass", new int[] {1, 20, 4}),
        ;

        private final float version;
        private final String config;
        private final String node;
        private final String removal;
        private final int[] mcversion;

        /**
         * A removing definition
         *
         * @param version the plugin version it was introduced
         * @param config the affected config
         * @param node the node to clear
         * @param mcversion the minecraft version it was introduced
         */
        Removing(float version, String config, String node, int[] mcversion) {
            this.version = version;
            this.config = config;
            this.node = node;
            this.removal = null;
            this.mcversion = mcversion;
        }

        /**
         * An element removing definition
         *
         * @param version the plugin version it was introduced
         * @param config the affected config
         * @param node the node to access
         * @param removal the value to remove
         * @param mcversion the minecraft version it was introduced
         */
        Removing(float version, String config, TreeConfig.CFG node, String removal, int[] mcversion) {
            this.version = version;
            this.config = config;
            this.node = node.getNode();
            this.removal = removal;
            this.mcversion = mcversion;
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

        int[] serverVersion = StringUtils.splitToVersionArray(Bukkit.getBukkitVersion());

        double version = config.getYamlConfiguration().getDouble(TreeConfig.CFG.VERSION.getNode(), 7.0);
        double newVersion = version;
        boolean changed = false;
        boolean verbose = TreeAssist.instance.config().getBoolean(MainConfig.CFG.GENERAL_VERBOSE_CONFIG_LOADING);

        for (Adding m : Adding.values()) {
            if (m.version > version && m.config.equals(configPath) && StringUtils.isSupportedVersion(serverVersion, m.mcversion)) {
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
            if (m.version > version && (m.config == null || m.config.equals(configPath)) && StringUtils.isSupportedVersion(serverVersion, m.mcversion)) {
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
            if (m.version > version && (m.config == null || m.config.equals(configPath)) && StringUtils.isSupportedVersion(serverVersion, m.mcversion)) {
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
            if (m.version > version && m.config.equals(configPath) && StringUtils.isSupportedVersion(serverVersion, m.mcversion)) {
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
            if (m.version > version && m.config.equals(configPath) && StringUtils.isSupportedVersion(serverVersion, m.mcversion)) {
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
            if (m.version > version && m.config.equals(configPath) && StringUtils.isSupportedVersion(serverVersion, m.mcversion)) {
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
