package net.slipcor.treeassist.configs;

import net.slipcor.treeassist.TreeAssist;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * A temporary solution to migrate config from prior to v7.0
 *
 * @deprecated as it was implemented in 2020 as a one-time measure, it will be removed in the next major version
 */
public class ConfigV7Updater {
    enum ToDefaultTree {
        APPLY_FULL_TOOL_DAMAGE("Main.Apply Full Tool Damage", TreeConfig.CFG.AUTOMATIC_DESTRUCTION_APPLY_FULL_TOOL_DAMAGE),
        AUTO_ADD_TO_INVENTORY("Main.Auto Add To Inventory", TreeConfig.CFG.AUTOMATIC_DESTRUCTION_AUTO_ADD_TO_INVENTORY),

        MAIN_AUTO_PLANT_DROPPED_SAPLINGS("Main.Auto Plant Dropped Saplings", TreeConfig.CFG.REPLANTING_DROPPED_SAPLINGS),
        MAIN_AUTOMATIC_TREE_DESTRUCTION("Main.Automatic Tree Destruction", TreeConfig.CFG.AUTOMATIC_DESTRUCTION_ACTIVE),

        MAIN_INITIAL_DELAY("Main.Initial Delay", TreeConfig.CFG.AUTOMATIC_DESTRUCTION_INITIAL_DELAY),
        MAIN_SAPLING_REPLANT("Main.Sapling Replant", TreeConfig.CFG.REPLANTING_ACTIVE),

        AUTOMATIC_TREE_DESTRUCTION_REQUIRED_LORE("Automatic Tree Destruction.Required Lore", TreeConfig.CFG.AUTOMATIC_DESTRUCTION_REQUIRED_LORE),
        AUTOMATIC_TREE_DESTRUCTION_WHEN_SNEAKING("Automatic Tree Destruction.When Sneaking", TreeConfig.CFG.AUTOMATIC_DESTRUCTION_WHEN_SNEAKING),
        AUTOMATIC_TREE_DESTRUCTION_WHEN_NOT_SNEAKING("Automatic Tree Destruction.When Not Sneaking", TreeConfig.CFG.AUTOMATIC_DESTRUCTION_WHEN_NOT_SNEAKING),
        AUTOMATIC_TREE_DESTRUCTION_FORCED_REMOVAL("Automatic Tree Destruction.Forced Removal", TreeConfig.CFG.AUTOMATIC_DESTRUCTION_FORCED_REMOVAL),
        AUTOMATIC_TREE_DESTRUCTION_REMOVE_LEAVES("Automatic Tree Destruction.Remove Leaves", TreeConfig.CFG.AUTOMATIC_DESTRUCTION_REMOVE_LEAVES),
        AUTOMATIC_TREE_DESTRUCTION_INITIAL_DELAY("Automatic Tree Destruction.Initial Delay (seconds)", TreeConfig.CFG.AUTOMATIC_DESTRUCTION_INITIAL_DELAY_TIME),
        AUTOMATIC_TREE_DESTRUCTION_DELAY("Automatic Tree Destruction.Delay (ticks)", TreeConfig.CFG.AUTOMATIC_DESTRUCTION_DELAY),
        AUTOMATIC_TREE_DESTRUCTION_COOLDOWN("Automatic Tree Destruction.Cooldown (seconds)", TreeConfig.CFG.AUTOMATIC_DESTRUCTION_COOLDOWN),
        AUTOMATIC_TREE_DESTRUCTION_INCREASES_STATISTICS("Automatic Tree Destruction.Increases Statistics", TreeConfig.CFG.AUTOMATIC_DESTRUCTION_INCREASES_STATISTICS),

        BLOCK_STATISTICS_PICKUP("Block Statistics.Pickup", TreeConfig.CFG.BLOCK_STATISTICS_PICKUP),
        BLOCK_STATISTICS_MINE_BLOCK("Block Statistics.Mine Block", TreeConfig.CFG.BLOCK_STATISTICS_MINE_BLOCK),

        AUTO_PLANT_DROPPED_SAPLINGS_CHANCE("Auto Plant Dropped Saplings.Chance (percent)", TreeConfig.CFG.REPLANTING_DROPPED_SAPLINGS_PROBABILITY, 0.01),
        AUTO_PLANT_DROPPED_SAPLINGS_DELAY("Auto Plant Dropped Saplings.Delay (seconds)", TreeConfig.CFG.REPLANTING_DELAY),

        SAPLING_REPLANT_BOTTOM_BLOCK_HAS_TO_BE_BROKEN_FIRST("Sapling Replant.Bottom Block has to be Broken First", TreeConfig.CFG.REPLANTING_ONLY_WHEN_BOTTOM_BLOCK_BROKEN_FIRST),
        SAPLING_REPLANT_TIME_TO_PROTECT_SAPLING("Sapling Replant.Time to Protect Sapling (Seconds)", TreeConfig.CFG.REPLANTING_PROTECT_FOR_SECONDS),
        SAPLING_REPLANT_TIME_TO_BLOCK_SAPLING_GROWTH("Sapling Replant.Time to Block Sapling Growth (Seconds)", TreeConfig.CFG.REPLANTING_DELAY_GROWTH_SECONDS),
        SAPLING_REPLANT_REPLANT_WHEN_TREE_BURNS_DOWN("Sapling Replant.Replant When Tree Burns Down", TreeConfig.CFG.REPLANTING_WHEN_TREE_BURNS_DOWN),
        SAPLING_REPLANT_BLOCK_ALL_BREAKING_OF_SAPLINGS("Sapling Replant.Block all breaking of Saplings", TreeConfig.CFG.REPLANTING_FORCE_PROTECT),
        SAPLING_REPLANT_DELAY_UNTIL_SAPLING_IS_REPLANTED("Sapling Replant.Delay until Sapling is replanted (seconds) (minimum 1 second)", TreeConfig.CFG.REPLANTING_DELAY),
        SAPLING_REPLANT_ENFORCE("Sapling Replant.Enforce", TreeConfig.CFG.REPLANTING_ENFORCE),

        TOOLS_SAPLING_REPLANT_REQUIRE_TOOLS("Tools.Sapling Replant Require Tools", TreeConfig.CFG.REPLANTING_REQUIRES_TOOLS),
        TOOLS_TREE_DESTRUCTION_REQUIRE_TOOLS("Tools.Tree Destruction Require Tools", TreeConfig.CFG.AUTOMATIC_DESTRUCTION_REQUIRES_TOOLS),
        TOOLS_TOOLS_LIST("Tools.Tools List", TreeConfig.CFG.TOOL_LIST),
        TOOLS_DROP_CHANCE("Tools.Drop Chance", TreeConfig.CFG.BLOCKS_CUSTOM_DROPS_FACTORS, 0.01);

        private final String oldMainNode;
        private final TreeConfig.CFG newTreeNode;
        private final Double factor;

        ToDefaultTree(String oldMainNode, TreeConfig.CFG treeConfigNode) {
            this.oldMainNode = oldMainNode;
            this.newTreeNode = treeConfigNode;
            this.factor = null;
        }

        ToDefaultTree(String node, TreeConfig.CFG treeConfigNode, double factor) {
            this.oldMainNode = node;
            this.newTreeNode = treeConfigNode;
            this.factor = factor;
        }
    }

    enum ToDifferentNode {
        MAIN_LANGUAGE("Main.Language", MainConfig.CFG.GENERAL_LANGUAGE, "lang_"),

        MAIN_DESTROY_ONLY_BLOCKS_ABOVE("Main.Destroy Only Blocks Above", MainConfig.CFG.DESTRUCTION_ONLY_ABOVE),

        MAIN_FORCE_BREAK_DEFAULT_RADIUS("Main.Force Break Default Radius", MainConfig.CFG.COMMANDS_FORCE_BREAK_DEFAULT_RADIUS),
        MAIN_FORCE_GROW_DEFAULT_RADIUS("Main.Force Grow Default Radius", MainConfig.CFG.COMMANDS_FORCE_GROW_DEFAULT_RADIUS),
        MAIN_FORCE_BREAK_MAX_RADIUS("Main.Force Break Max Radius", MainConfig.CFG.COMMANDS_FORCE_BREAK_MAX_RADIUS),
        MAIN_FORCE_GROW_MAX_RADIUS("Main.Force Grow Max Radius", MainConfig.CFG.COMMANDS_FORCE_GROW_MAX_RADIUS),
        MAIN_IGNORE_USER_PLACED_BLOCKS("Main.Ignore User Placed Blocks", MainConfig.CFG.PLACED_BLOCKS_ACTIVE, true),

        MAIN_TOGGLE_DEFAULT("Main.Toggle Default", MainConfig.CFG.GENERAL_TOGGLE_DEFAULT),
        MAIN_USE_MCMMO_IF_AVAILABLE("Main.Use mcMMO if Available", MainConfig.CFG.PLUGINS_USE_MCMMO),
        MAIN_USE_JOBS_IF_AVAILABLE("Main.Use Jobs if Available", MainConfig.CFG.PLUGINS_USE_JOBS),
        MAIN_USE_WORLDGUARD_IF_AVAILABLE("Main.Use WorldGuard if Available", MainConfig.CFG.PLUGINS_USE_WORLDGUARD),
        MAIN_USE_PERMISSIONS("Main.Use Permissions", MainConfig.CFG.GENERAL_USE_PERMISSIONS),
        MAIN_USE_FALLING_BLOCKS("Main.Use Falling Blocks", MainConfig.CFG.DESTRUCTION_FALLING_BLOCKS),

        LEAF_DECAY_FAST_LEAF_DECAY("Leaf Decay.Fast Leaf Decay", MainConfig.CFG.DESTRUCTION_FAST_LEAF_DECAY),

        SAPLING_REPLANT_COMMAND_TIME_DELAY("Sapling Replant.Command Time Delay (Seconds)", MainConfig.CFG.COMMANDS_NOREPLANT_COMMAND_TIME_COOLDOWN),

        WORLDS_ENABLE_PER_WORLD("Worlds.Enable Per World", MainConfig.CFG.WORLDS_RESTRICT),

        PLACED_BLOCKS_HANDLER_PLUGIN_NAME("Placed Blocks.Handler Plugin Name", MainConfig.CFG.PLACED_BLOCKS_PLUGIN_NAME),
        PLACED_BLOCKS_HANDLER_LOOKUP_TIME("Placed Blocks.Handler Lookup Time", MainConfig.CFG.PLACED_BLOCKS_LOOKUP_TIME);

        private final String oldNode;
        private final MainConfig.CFG newNode;
        private final String prefix;
        private final boolean invert;

        ToDifferentNode(String oldNode, MainConfig.CFG newNode, String prefix) {
            this.oldNode = oldNode;
            this.newNode = newNode;
            this.prefix = prefix;
            this.invert = false;
        }

        ToDifferentNode(String oldNode, MainConfig.CFG newNode) {
            this.oldNode = oldNode;
            this.newNode = newNode;
            this.prefix = null;
            this.invert = false;
        }

        ToDifferentNode(String oldNode, MainConfig.CFG newNode, boolean invert) {
            this.oldNode = oldNode;
            this.newNode = newNode;
            this.prefix = null;
            this.invert = invert;
        }
    }

    /**
     * Commit updating the config, moving the nodes from the 6.X logic to the 7.X logic
     */
    public static void commit() {
        FileConfiguration config = TreeAssist.instance.getMainConfig().getYamlConfiguration();

        FileConfiguration treeConfig = TreeAssist.treeConfigs.get("default").getYamlConfiguration();

        for (ToDefaultTree defaultTree : ToDefaultTree.values()) {
            if (defaultTree.factor != null) {
                ConfigurationSection sec = config.getConfigurationSection(defaultTree.oldMainNode);
                if (sec == null) {
                    continue;
                }
                Set<String> keys = sec.getKeys(false);
                for (String key : keys) {
                    double value = sec.getInt(key, 100);
                    value *= defaultTree.factor;
                    String newNode = defaultTree.newTreeNode.getNode() + "." + key;
                    System.out.println("Moving " + defaultTree.oldMainNode + " to " + newNode);
                    treeConfig.set(newNode, value);
                }
                config.set(defaultTree.oldMainNode, null); // Remove old node
            } else {
                if (config.contains(defaultTree.oldMainNode)) {
                    if (defaultTree.newTreeNode.getType() == ConfigEntry.Type.MAP) {
                        ConfigurationSection sec = config.getConfigurationSection(defaultTree.oldMainNode);
                        if (sec == null) {
                            continue;
                        }
                        Set<String> keys = sec.getKeys(false);
                        for (String key : keys) {
                            String newNode = defaultTree.newTreeNode.getNode() + "." + key;
                            System.out.println("Moving " + defaultTree.oldMainNode + " to " + newNode);
                            treeConfig.set(newNode, sec.get(key));
                        }
                    } else if (defaultTree.newTreeNode.getType() == ConfigEntry.Type.LIST) {
                        List<String> copy = new ArrayList<>(config.getStringList(defaultTree.oldMainNode));

                        System.out.println("Moving " + defaultTree.oldMainNode + " to " + defaultTree.newTreeNode.getNode());
                        treeConfig.set(defaultTree.newTreeNode.getNode(), copy);
                    } else {
                        System.out.println("Moving " + defaultTree.oldMainNode + " to " + defaultTree.newTreeNode.getNode());
                        treeConfig.set(defaultTree.newTreeNode.getNode(), config.get(defaultTree.oldMainNode));
                    }

                    config.set(defaultTree.oldMainNode, null); // Remove old node
                }
            }
        }

        for (ToDifferentNode differentNode : ToDifferentNode.values()) {
            System.out.println("Moving " + differentNode.oldNode + " to " + differentNode.newNode.getNode());
            if (differentNode.prefix != null) {
                // We need to replace the value
                String value = differentNode.prefix + config.getString(differentNode.oldNode);
                config.set(differentNode.newNode.getNode(), value);
            } else if (differentNode.invert) {
                // We need to invert the value
                boolean value = !config.getBoolean(differentNode.oldNode);
                config.set(differentNode.newNode.getNode(), value);
            } else {
                // All the worlds are trivial, just move them over
                config.set(differentNode.newNode.getNode(), config.get(differentNode.oldNode));
            }
            config.set(differentNode.oldNode, null); // Remove old node
        }

        File home = TreeAssist.instance.getDataFolder();
        File configFile = new File(home, "config.yml");
        File tree = new File(home, "trees");
        File newConfigFile = new File(tree, "default.yml");

        // General cleanup of empty nodes
        config.set("Main", null);
        config.set("Automatic Tree Destruction", null);
        config.set("Block Statistics", null);
        config.set("Auto Plant Dropped Saplings", null);
        config.set("Leaf Decay", null);
        config.set("Sapling Replant", null);
        config.set("Tools", null);
        config.set("Custom Drops", null);

        // due to a bug this never worked and no-one complained
        treeConfig.set(TreeConfig.CFG.REPLANTING_ONLY_WHEN_BOTTOM_BLOCK_BROKEN_FIRST.getNode(), false);

        try {
            config.save(configFile);
            treeConfig.save(newConfigFile);
        } catch (IOException e) {
            TreeAssist.instance.getLogger().severe("Failed to update config!");
            e.printStackTrace();
        }
    }
}
