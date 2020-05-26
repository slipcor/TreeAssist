package me.itsatacoshop247.TreeAssist.core;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Config {
    private final YamlConfiguration cfg;
    private final File configFile;
    private final Map<String, Boolean> booleans;
    private final Map<String, Integer> ints;
    private final Map<String, Double> doubles;
    private final Map<String, String> strings;

    public void save() {
        try {
            cfg.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public enum CFG {
        MAIN_DESTROY_ONLY_BLOCKS_ABOVE("Main.Destroy Only Blocks Above", false), //TODO: check
        MAIN_FORCE_BREAK_DEFAULT_RADIUS("Main.Force Break Default Radius", 10), //TODO: check
        MAIN_FORCE_GROW_DEFAULT_RADIUS("Main.Force Grow Default Radius", 10), //TODO: check
        MAIN_FORCE_BREAK_MAX_RADIUS("Main.Force Break Max Radius", 30), //TODO: check
        MAIN_FORCE_GROW_MAX_RADIUS("Main.Force Grow Max Radius", 30), //TODO: check
        MAIN_IGNORE_USER_PLACED_BLOCKS("Main.Ignore User Placed Blocks", false), //TODO: check
        MAIN_LANGUAGE("Main.Language", "en"), //TODO: check
        MAIN_TOGGLE_DEFAULT("Main.Toggle Default", true), //TODO: check
        MAIN_USE_MCMMO_IF_AVAILABLE("Main.Use mcMMO if Available", true), //TODO: check
        MAIN_USE_JOBS_IF_AVAILABLE("Main.Use Jobs if Available", true), //TODO: check
        MAIN_USE_WORLDGUARD_IF_AVAILABLE("Main.Use WorldGuard if Available", false), //TODO: check
        MAIN_USE_PERMISSIONS("Main.Use Permissions", false), //TODO: check
        MAIN_USE_FALLING_BLOCKS("Main.Use Falling Blocks", false), //TODO: check

        AUTOMATIC_TREE_DESTRUCTION_WHEN_SNEAKING("Automatic Tree Destruction.When Sneaking", true), //TODO: check
        AUTOMATIC_TREE_DESTRUCTION_WHEN_NOT_SNEAKING("Automatic Tree Destruction.When Not Sneaking", true), //TODO: check

        LEAF_DECAY_FAST_LEAF_DECAY("Leaf Decay.Fast Leaf Decay", true), //TODO: check

        SAPLING_REPLANT_BLOCK_ALL_BREAKING_OF_SAPLINGS("Sapling Replant.Block all breaking of Saplings", false), //TODO: check
        SAPLING_REPLANT_BOTTOM_BLOCK_HAS_TO_BE_BROKEN_FIRST("Sapling Replant.Bottom Block has to be Broken First", true), //TODO: check
        SAPLING_REPLANT_COMMAND_TIME_DELAY("Sapling Replant.Command Time Delay (Seconds)", 30), //TODO: check
        SAPLING_REPLANT_DELAY_UNTIL_SAPLING_IS_REPLANTED("Sapling Replant.Delay until Sapling is replanted (seconds) (minimum 1 second)", 1), //TODO: check
        SAPLING_REPLANT_REPLANT_WHEN_TREE_BURNS_DOWN("Sapling Replant.Replant When Tree Burns Down", true), //TODO: check
        SAPLING_REPLANT_TIME_TO_PROTECT_SAPLING("Sapling Replant.Time to Protect Sapling (Seconds)", 0), //TODO: check
        SAPLING_REPLANT_TIME_TO_BLOCK_SAPLING_GROWTH("Sapling Replant.Time to Block Sapling Growth (Seconds)", 0), //TODO: check

        WORLDS_ENABLE_PER_WORLD("Worlds.Enable Per World", false), //TODO: check
        WORLD_ENABLED_WORLDS("Worlds.Enabled Worlds", new ArrayList<>(Arrays.asList("world", "world2"))), //TODO: check

        PLACED_BLOCKS_HANDLER_PLUGIN_NAME("Placed Blocks.Handler Plugin Name", "TreeAssist"), //TODO: check
        PLACED_BLOCKS_HANDLER_LOOKUP_TIME("Placed Blocks.Handler Lookup Time", 86400), //TODO: check

        MODDING_DISABLE_DURABILITY_FIX("Modding.Disable Durability Fix", false), //TODO: check

        VERSION("Version", 6.0);



        private final String node;
        private final Object value;
        private final String type;

        public static CFG getByNode(final String node) {
            for (final CFG m : CFG.getValues()) {
                if (m.node.equals(node)) {
                    return m;
                }
            }
            return null;
        }

        CFG(final String node, final String value) {
            this.node = node;
            this.value = value;
            type = "string";
        }

        CFG(final String node, final Boolean value) {
            this.node = node;
            this.value = value;
            type = "boolean";
        }

        CFG(final String node, final Integer value) {
            this.node = node;
            this.value = value;
            type = "int";
        }

        CFG(final String node, final Double value) {
            this.node = node;
            this.value = value;
            type = "double";
        }

        CFG(final String node, final List<String> value) {
            this.node = node;
            this.value = value;
            this.type = "list";
        }



        CFG(final String node, final Map<String, String> value) {
            this.node = node;
            this.value = value;
            this.type = "map";
        }

        public String getNode() {
            return node;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        public Object getValue() {
            return value;
        }

        public static CFG[] getValues() {
            return values();
        }

        public String getType() {
            return type;
        }

    }


    /**
     * Create a new TreeConfig instance that uses the specified file for loading
     *
     * @param configFile a YAML file
     */
    public Config(final File configFile) {
        Utils.plugin.getLogger().info("Loading tree config file: " + configFile.getAbsolutePath().replace(Utils.plugin.getDataFolder().getAbsolutePath(), ""));

        cfg = new YamlConfiguration();
        this.configFile = configFile;
        booleans = new HashMap<>();
        ints = new HashMap<>();
        doubles = new HashMap<>();
        strings = new HashMap<>();
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
            reloadMaps();
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Iterates through all keys in the config-file, and populates the value
     * maps. Boolean values are stored in the booleans-map, Strings in the
     * strings-map, etc.
     */
    public void reloadMaps() {
        // known exceptions
        String[] exceptions = {"Main",
                "Automatic Tree Destruction", "Block Statistics", "Auto Plant Dropped Saplings", "Leaf Decay",
                "Sapling Replant", "Tools", "Worlds", "Placed Blocks", "Modding"};

        root: for (final String s : cfg.getKeys(true)) {
            final Object object = cfg.get(s);

            if (object instanceof Boolean) {
                booleans.put(s, (Boolean) object);
            } else if (object instanceof Integer) {
                ints.put(s, (Integer) object);
            } else if (object instanceof Double) {
                doubles.put(s, (Double) object);
            } else if (object instanceof String) {
                strings.put(s, (String) object);
            }

            if (CFG.getByNode(s) == null) {

                for (String test : exceptions) {
                    if (s.equals(test)) {
                        continue root;
                    }
                }

                Utils.plugin.getLogger().warning("No valid node: " + s);
            }
        }
    }


    // /////////////////////////////////////////////////////////////////////////
    // //
    // GETTERS //
    // //
    // /////////////////////////////////////////////////////////////////////////

    /**
     * Get the YamlConfiguration associated with this Config instance. Note that
     * changes made directly to the YamlConfiguration will cause an
     * inconsistency with the value maps unless reloadMaps() is called.
     *
     * @return the YamlConfiguration of this Config instance
     */
    public YamlConfiguration getYamlConfiguration() {
        return cfg;
    }

    /**
     * Retrieve a value from the YamlConfiguration.
     *
     * @param string the path of the value
     * @return the value of the path
     */
    public Object getUnsafe(final String string) {
        return cfg.get(string);
    }

    /**
     * Retrieve a boolean from the value maps.
     *
     * @param cfg the node of the value
     * @return the boolean value of the path if the path exists, false otherwise
     */
    public boolean getBoolean(final CFG cfg) {
        return getBoolean(cfg, (Boolean) cfg.getValue());
    }

    /**
     * Retrieve a boolean from the value maps.
     *
     * @param cfg the node of the value
     * @param def a default value to return if the value was not in the map
     * @return the boolean value of the path if it exists, def otherwise
     */
    private boolean getBoolean(final CFG cfg, final boolean def) {
        final String path = cfg.getNode();
        final Boolean result = booleans.get(path);
        return result == null ? def : result;
    }

    /**
     * Retrieve an int from the value maps.
     *
     * @param cfg the node of the value
     * @return the int value of the path if the path exists, 0 otherwise
     */
    public int getInt(final CFG cfg) {
        return getInt(cfg, (Integer) cfg.getValue());
    }

    /**
     * Retrieve an int from the value maps.
     *
     * @param cfg the node of the value
     * @param def a default value to return if the value was not in the map
     * @return the int value of the path if it exists, def otherwise
     */
    public int getInt(final CFG cfg, final int def) {
        final String path = cfg.getNode();
        final Integer result = ints.get(path);
        return result == null ? def : result;
    }

    /**
     * Retrieve a double from the value maps.
     *
     * @param cfg the node of the value
     * @return the double value of the path if the path exists, 0D otherwise
     */
    public double getDouble(final CFG cfg) {
        return getDouble(cfg, (Double) cfg.getValue());
    }

    /**
     * Retrieve a double from the value maps.
     *
     * @param cfg the node of the value
     * @param def a default value to return if the value was not in the map
     * @return the double value of the path if it exists, def otherwise
     */
    public double getDouble(final CFG cfg, final double def) {
        final String path = cfg.getNode();
        final Double result = doubles.get(path);
        return result == null ? def : result;
    }

    /**
     * Retrieve a string from the value maps.
     *
     * @param cfg the node of the value
     * @return the string value of the path if the path exists, null otherwise
     */
    public String getString(final CFG cfg) {
        return getString(cfg, (String) cfg.getValue());
    }

    /**
     * Retrieve a string from the value maps.
     *
     * @param cfg the node of the value
     * @param def a default value to return if the value was not in the map
     * @return the string value of the path if it exists, def otherwise
     */
    public String getString(final CFG cfg, final String def) {
        final String path = cfg.getNode();
        final String result = strings.get(path);
        return result == null ? def : result;
    }

    /**
     * Retrieve a list of materials from the value maps
     *
     * @param cfg the node of the value
     * @return a list of materials (can contain null)
     */
    public List<Material> getMaterials(CFG cfg) {
        List<String> list = getStringList(cfg, new ArrayList<>());

        List<Material> matList = new ArrayList<>();

        for (String matName : list) {
            matList.add(Material.matchMaterial(matName));
        }

        return matList;
    }

    public Set<String> getKeys(final String path) {
        if (cfg.get(path) == null) {
            return null;
        }

        final ConfigurationSection section = cfg.getConfigurationSection(path);
        return section.getKeys(false);
    }

    public List<String> getStringList(final CFG cfg, final List<String> def) {
        if (this.cfg.get(cfg.node) == null) {
            return def == null ? new LinkedList<>() : def;
        }

        return this.cfg.getStringList(cfg.node);
    }
}
