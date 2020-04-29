package me.itsatacoshop247.TreeAssist.core;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class TreeConfig {
    private final YamlConfiguration cfg;
    private final File configFile;
    private final Map<String, Boolean> booleans;
    private final Map<String, Integer> ints;
    private final Map<String, Double> doubles;
    private final Map<String, String> strings;

    public void loadDefaults(TreeConfig parent) {
        for (CFG c : CFG.values()) {
            if (c.type.equals("string")) {
                cfg.addDefault(c.node, getString(c, parent.getString(c)));
            } else if (c.type.equals("boolean")) {
                cfg.addDefault(c.node, getBoolean(c, parent.getBoolean(c)));
            } else if (c.type.equals("int")) {
                cfg.addDefault(c.node, getInt(c, parent.getInt(c)));
            } else if (c.type.equals("double")) {
                cfg.addDefault(c.node, getDouble(c, parent.getDouble(c)));
            } else if (c.type.equals("list")) {
                cfg.addDefault(c.node, getStringList(c, parent.getStringList(c, new ArrayList<>())));
            } else if (c.type.equals("map")) {
                ConfigurationSection cs = parent.getYamlConfiguration().getConfigurationSection(c.node);

                if (cs != null) {
                    for (String key : cs.getKeys(false)) {
                        String subPath = c.node + "." + key;
                        if (cfg.get(subPath) == null) {
                            // child does not inherit
                            cfg.addDefault(subPath, parent.getYamlConfiguration().get(subPath));
                        }
                    }
                }

                cs = cfg.getConfigurationSection(c.node);

                if (cs != null) {
                    for (String key : cs.getKeys(false)) {
                        String subPath = c.node + "." + key;
                        cfg.addDefault(subPath, cfg.get(subPath));
                    }
                }
            }
        }
    }

    public enum CFG {
        CHILDREN("Children", new ArrayList<>()),
        CUSTOM_DROPS("Custom Drops", new HashMap<>()),
        CUSTOM_DROP_CHANCE("Custom Drop Chance", new HashMap<>()),
        GROUND_BLOCKS("Ground Blocks", new ArrayList<>()), // the allowed blocks below the tree trunk

        BLOCKS_MATERIALS("Blocks.Materials", new ArrayList<>()), // the expected blocks part of the tree, next to the trunk

        BLOCKS_CAP_HEIGHT("Blocks.Cap.Height", 2), // Branch Topping Leaves Height
        BLOCKS_CAP_RADIUS("Blocks.Cap.Radius", 3), // Branch Topping Leaves Radius

        BLOCKS_MIDDLE_AIR("Blocks.Middle.Air", false), // allow air pockets?
        BLOCKS_MIDDLE_EDGES("Blocks.Middle.Edges", false), // would edges be populated?
        BLOCKS_MIDDLE_RADIUS("Blocks.Middle.Radius", 2), // the tree middle leaf radius (radius starts away from trunk!)

        BLOCKS_TOP_AIR("Blocks.Top.Air", false), // allow air pockets?
        BLOCKS_TOP_EDGES("Blocks.Top.Edges", false), // would edges be populated?
        BLOCKS_TOP_RADIUS("Blocks.Top.Radius", 3), // the tree top leaf radius
        BLOCKS_TOP_HEIGHT("Blocks.Top.Height", 3),

        TRUNK_BRANCH("Trunk.Branch", false),
        TRUNK_DIAGONAL("Trunk.Diagonal", false), // Trunk can move diagonally even (Acacia)
        TRUNK_EDGES("Trunk.Edges", false), // Trunk can have extra on the edges (Dark Oak)
        TRUNK_MINIMUM_HEIGHT("Trunk.Minimum Height", 4),
        TRUNK_MAXIMUM_HEIGHT("Trunk.Maximum Height", 30),
        TRUNK_MATERIALS("Trunk.Materials", new ArrayList<>()), // the expected materials part of the tree trunk
        TRUNK_THICKNESS("Trunk.Thickness", 1), // This value is also used for radius calculation!
        TRUNK_RADIUS("Trunk.Radius", 1),
        TRUNK_UNEVEN_BOTTOM("Trunk.Uneven Bottom", false), // Can saplings/lowest trunks be on different Y?

        REPLANT("Replant", "String"),
        TOOL_LIST("Tool List", new ArrayList<>()),
        NATURAL_BLOCKS("Natural Blocks", new ArrayList<>()) // blocks that are okay to have around trees
        ; //

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
    public TreeConfig (final File configFile) {
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

            TreeStructure.allTrunks.addAll(getMaterials(TreeConfig.CFG.TRUNK_MATERIALS));
            TreeStructure.allExtras.addAll(getMaterials(TreeConfig.CFG.BLOCKS_MATERIALS));
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
                // known exceptions
                String[] exceptions = {"Custom Drop Chance",
                        "Blocks", "Blocks.Cap", "Blocks.Top", "Blocks.Middle",
                        "Trunk", "Trunk.Branch"};

                for (String test : exceptions) {
                    if (s.equals(test)) {
                        continue root;
                    }
                }

                String[] materialPaths = { "Custom Drops.", "Custom Drop Chance."};

                for (String test : materialPaths) {
                    if (s.startsWith(test)) {
                        String material = s.replace(test, "");
                        try {
                            Material testMaterial = Material.matchMaterial(material, false);
                            if (testMaterial != null) {
                                continue root;
                            }
                            testMaterial = Material.matchMaterial(material, true);
                            if (testMaterial != null) {
                                Utils.plugin.getLogger().warning("Legacy name used: " + material + " is now " + testMaterial.name());
                                continue root;
                            }
                            Utils.plugin.getLogger().warning("No valid material " + material + " in node " + s);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
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
