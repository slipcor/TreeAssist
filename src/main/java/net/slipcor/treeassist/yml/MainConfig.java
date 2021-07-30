package net.slipcor.treeassist.yml;

import net.slipcor.core.ConfigEntry;
import net.slipcor.core.CoreConfig;
import net.slipcor.treeassist.TreeAssist;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainConfig extends CoreConfig {

    public enum CFG implements ConfigEntry {
        GENERAL("General", "=== [ General Settings ] ==="),
        GENERAL_LANGUAGE("General.Language", "lang_en", "Language file to load. Does not need YML extension!"),
        GENERAL_PREVENT_WITHOUT_TOOL("General.Prevent Log Breaking Without Tool", false, "Prevent breaking of logs without the right tool"),
        GENERAL_TOGGLE_DEFAULT("General.Toggle Default", true, "For the toggling command, should players start with TreeAssist active?"),
        GENERAL_USE_PERMISSIONS("General.Use Permissions", false, "Should we check if players have permissions? If false, all features are available to everyone."),
        GENERAL_VERBOSE_CONFIG_LOADING("General.Verbose Config Loading", false, "Should we announce information about every tree config we load?"),

        COMMANDS("Commands", "=== [ Command Settings ] ==="),
        COMMANDS_FORCE_BREAK_DEFAULT_RADIUS("Commands.Force Break.Default Radius", 10, ""),
        COMMANDS_FORCE_BREAK_MAX_RADIUS("Commands.Force Break.Max Radius", 30, ""),
        COMMANDS_FORCE_GROW_DEFAULT_RADIUS("Commands.Force Grow.Default Radius", 10, ""),
        COMMANDS_FORCE_GROW_MAX_RADIUS("Commands.Force Grow.Max Radius", 30, ""),
        COMMANDS_NOREPLANT_COMMAND_TIME_COOLDOWN("Commands.No Replant.Cooldown Time", 30, ""),

        DESTRUCTION("Destruction", "=== [ Automatic Destruction Settings ] ==="),
        DESTRUCTION_FALLING_BLOCKS("Destruction.Falling Blocks", false, "Spawn a FallingBlock when breaking a block"),
        DESTRUCTION_FALLING_BLOCKS_FANCY("Destruction.Falling Blocks Fancy", false, "Make the FallingBlocks look almost like an actual tree falling over"),
        DESTRUCTION_FAST_LEAF_DECAY("Destruction.Fast Leaf Decay", true, "Increase leaf decay by looking for nearby lonely leaves"),
        DESTRUCTION_MESSAGE("Destruction.Message Invalid Blocks", true, "Message when a tree determination fails based on invalid nearby blocks"),
        DESTRUCTION_ONLY_ABOVE("Destruction.Only Above", false, "Only break blocks that are above the block the player broke"),

        PLUGINS("Plugins", "=== [ Plugin Integration Settings ] ==="),
        PLUGINS_USE_CUSTOM_EVENTS("Plugins.CustomEvents", false, "Create a custom block break event for every automatic block broken"),
        PLUGINS_USE_MCMMO("Plugins.mcMMO", true, "Count broken blocks towards the TreeFeller ability"),
        PLUGINS_USE_TREEMCMMO("Plugins.TreeMcMMO", false, "Count broken trees towards the TreeFeller ability"),
        PLUGINS_USE_JOBS("Plugins.Jobs", true, "Count broken blocks towards Jobs jobs that fit"),
        PLUGINS_USE_TREEJOBS("Plugins.TreeJobs", false, "Count broken trees towards Jobs jobs that fit"),
        PLUGINS_USE_WORLDGUARD("Plugins.WorldGuard", false, "Allow to set up regions with WorldGuard to prevent destruction with 'treeassist-autochop' and sapling replacement with 'treeassist-replant'"),

        WORLDS("Worlds", "=== [ World Related Settings ] ==="),
        WORLDS_RESTRICT("Worlds.Restrict", false, "Enable world based restrictions"),
        WORLDS_ENABLED_WORLDS("Worlds.Enabled Worlds", new ArrayList<>(Arrays.asList("world", "world2")), "Worlds that are not restricted"),

        PLACED_BLOCKS("Placed Blocks", "=== [ Placed Blocks Settings ] ==="),
        PLACED_BLOCKS_ACTIVE("Placed Blocks.Active", true, "Check for player placed blocks"),
        PLACED_BLOCKS_PLUGIN_NAME("Placed Blocks.Plugin Name", "TreeAssist", "A plugin that will look for placed blocks (Prism, LogBlock, CoreProtect)"),
        PLACED_BLOCKS_LOOKUP_TIME("Placed Blocks.Lookup Time", 86400, "How many seconds back we want to look"),

        MODDING_DISABLE_DURABILITY_FIX("Modding.Disable Durability Fix", false, "This is for hacky mods that use infinity durability"),

        BSTATS("bStats", "=== [ bStats Metrics Settings ] ==="),
        BSTATS_ACTIVE("bStats.Active", true, "Should we send stats at all? Please keep this in so we have an overview of spread of versions <3"),
        BSTATS_FULL("bStats.Full", true, "This setting sends a bit more detailed information about which features are used at all. Thank you for supporting me!"),

        UPDATE_MODE("Update.Mode", "both", "what to do? Valid values: disable, announce, download, both"),
        UPDATE_TYPE("Update.Type", "beta", "which type of branch to get updates? Valid values: dev, alpha, beta, release"),

        VERSION("Version", 7.1009, "The config version for update checks"),
        DEBUG("Debug", "none", "");

        private final String node;
        private final Object value;
        private final Type type;
        private final String comment;

        CFG(final String node, String comment) {
            this.node = node;
            this.value = comment;
            type = Type.COMMENT;
            this.comment = comment;
        }

        CFG(final String node, final String value, String comment) {
            this.node = node;
            this.value = value;
            type = Type.STRING;
            this.comment = comment;
        }

        CFG(final String node, final Boolean value, String comment) {
            this.node = node;
            this.value = value;
            type = Type.BOOLEAN;
            this.comment = comment;
        }

        CFG(final String node, final Integer value, String comment) {
            this.node = node;
            this.value = value;
            type = Type.INT;
            this.comment = comment;
        }

        CFG(final String node, final Double value, String comment) {
            this.node = node;
            this.value = value;
            type = Type.DOUBLE;
            this.comment = comment;
        }

        CFG(final String node, final List<String> value, String comment) {
            this.node = node;
            this.value = value;
            this.type = Type.LIST;
            this.comment = comment;
        }

        public static CFG getByNode(String node) {
            for (final CFG e : CFG.values()) {
                if (e.node.equals(node)) {
                    return e;
                }
            }
            return null;
        }


        @Override
        public String getComment() {
            return comment;
        }

        @Override
        public String getNode() {
            return node;
        }

        @Override
        public Object getValue() {
            return value;
        }

        @Override
        public Type getType() {
            return type;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }
    }

    /**
     * Create a new MainConfig instance that uses the specified file for loading
     *
     * @param configFile a YAML file
     */
    public MainConfig(final TreeAssist plugin, final File configFile) {
        super(plugin, "TreeAssist Main Config", configFile);

        TreeAssist.instance.getLogger().info("Loading main config file: " + configFile.getAbsolutePath().replace(TreeAssist.instance.getDataFolder().getAbsolutePath(), ""));

        if (MainConfigUpdater.check(this, cfg)) {
            save();
        }
        emptyNodes = new String[]{
                "General", "Commands", "Commands.Force Break", "Commands.Force Grow", "Commands.No Replant",
                "Destruction", "Placed Blocks", "Plugins", "Worlds", "Modding", "Update"
        };
    }

    public CFG getByNode(final String node) {
        for (final CFG m : CFG.values()) {
            if (m.node.equals(node) ||
                    m.node.replaceAll("\\s+", "").equals(node)) {
                return m;
            }
        }
        return null;
    }

    /**
     * Load the config-file into the YamlConfiguration, and then populate the
     * value maps.
     *
     * @return true, if the load succeeded, false otherwise.
     */
    public boolean load() {
        try {
            cfg.load(configFile);
            if (cfg.contains("Main.Use Permissions")) {
                ConfigV7Updater.commit(); //TODO: remove class next major bump or reuse next rewrite
                cfg.load(configFile); // reload again!
            }
            reloadMaps();
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Load the config file without filling our maps, mainly for checking for config changes
     */
    public void preLoad() {
        try {
            cfg.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check for invalid materials
     *
     * @param node the full node ending in the material name
     *
     * @return whether the material is valid or legacy
     */
    protected boolean checkMaterials(String node) {
        return false;
    }

    @Override
    protected void loadMaterials() {
    }
}
