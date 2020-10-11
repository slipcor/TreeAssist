package net.slipcor.treeassist.configs;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.core.TreeStructure;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TreeConfig {
    private final YamlConfiguration cfg;
    private final File configFile;
    private final Map<String, Boolean> booleans;
    private final Map<String, Integer> ints;
    private final Map<String, Double> doubles;
    private final Map<String, String> strings;
    private final Map<String, List<Material>> materials;
    private final Map<String, Map<String, Double>> maps;

    public enum CFG {
        AUTOMATIC_DESTRUCTION_ACTIVE("Automatic Destruction.Active", true), // Will we attempt to automatically destroy?
        AUTOMATIC_DESTRUCTION_APPLY_FULL_TOOL_DAMAGE("Automatic Destruction.Apply Full Tool Damage", true),
        AUTOMATIC_DESTRUCTION_AUTO_ADD_TO_INVENTORY("Automatic Destruction.Auto Add To Inventory", false),
        AUTOMATIC_DESTRUCTION_COOLDOWN("Automatic Destruction.Cooldown Seconds", 0),
        AUTOMATIC_DESTRUCTION_DELAY("Automatic Destruction.Delay Ticks", 0),
        AUTOMATIC_DESTRUCTION_FORCED_REMOVAL("Automatic Destruction.Forced Removal", false),
        AUTOMATIC_DESTRUCTION_INCREASES_STATISTICS("Automatic Destruction.Increases Statistics", false),
        AUTOMATIC_DESTRUCTION_INITIAL_DELAY("Automatic Destruction.Initial Delay", false),
        AUTOMATIC_DESTRUCTION_INITIAL_DELAY_TIME("Automatic Destruction.Initial Delay Seconds", 10),
        AUTOMATIC_DESTRUCTION_CLEANUP_DELAY_TIME("Automatic Destruction.Cleanup Delay Seconds", 20),
        AUTOMATIC_DESTRUCTION_REMOVE_LEAVES("Automatic Destruction.Remove Leaves", true),
        AUTOMATIC_DESTRUCTION_REQUIRED_LORE("Automatic Destruction.Required Lore",""),
        AUTOMATIC_DESTRUCTION_REQUIRES_TOOLS("Automatic Destruction.Requires Tools", true),
        AUTOMATIC_DESTRUCTION_WHEN_SNEAKING("Automatic Destruction.When Sneaking", true),
        AUTOMATIC_DESTRUCTION_WHEN_NOT_SNEAKING("Automatic Destruction.When Not Sneaking", true),
        AUTOMATIC_DESTRUCTION_USE_SILK_TOUCH("Automatic Destruction.Use Silk Touch", true),

        BLOCK_STATISTICS_MINE_BLOCK("Block Statistics.Mine Block", false),
        BLOCK_STATISTICS_PICKUP("Block Statistics.Pickup", false),

        BLOCKS_CAP_HEIGHT("Blocks.Cap.Height", 2), // Branch Topping Leaves Height
        BLOCKS_CAP_RADIUS("Blocks.Cap.Radius", 3), // Branch Topping Leaves Radius

        BLOCKS_CUSTOM_DROPS("Blocks.Custom Drops", true),

        BLOCKS_MATERIALS("Blocks.Materials", new ArrayList<>()), // the expected blocks part of the tree, next to the trunk

        BLOCKS_MIDDLE_AIR("Blocks.Middle.Air", false), // allow air pockets?
        BLOCKS_MIDDLE_EDGES("Blocks.Middle.Edges", false), // would edges be populated?
        BLOCKS_MIDDLE_RADIUS("Blocks.Middle.Radius", 2), // the tree middle leaf radius (radius starts away from trunk!)

        BLOCKS_REQUIRED("Blocks.Required", 10), // how many extra blocks do we need to find for it to count as a tree??

        BLOCKS_TOP_AIR("Blocks.Top.Air", false), // allow air pockets?
        BLOCKS_TOP_EDGES("Blocks.Top.Edges", false), // would edges be populated?
        BLOCKS_TOP_RADIUS("Blocks.Top.Radius", 3), // the tree top leaf radius
        BLOCKS_TOP_HEIGHT("Blocks.Top.Height", 3),

        BLOCKS_VINES("Blocks.Vines", false), // do we need to look for vines?

        CUSTOM_DROPS("Custom Drops", new HashMap<>()),
        CUSTOM_DROP_FACTOR("Custom Drop Factor", new HashMap<>()),

        GROUND_BLOCKS("Ground Blocks", new ArrayList<>()), // the allowed blocks below the tree trunk

        NATURAL_BLOCKS("Natural Blocks", new ArrayList<>()), // blocks that are okay to have around trees

        PARENT("Parent", "default"),
        PERMISSION("Permission", ""),

        REPLANTING_ACTIVE("Replanting.Active", true),
        REPLANTING_DELAY_GROWTH_SECONDS("Replanting.Delay Growth Seconds", 0),
        REPLANTING_ENFORCE("Replanting.Enforce", false),
        REPLANTING_FORCE_PROTECT("Replanting.Force Protect", false),
        REPLANTING_DELAY("Replanting.Delay", 1),
        REPLANTING_DROPPED_SAPLINGS("Replanting.Dropped.Active", false),
        REPLANTING_DROPPED_SAPLINGS_PROBABILITY("Replanting.Dropped.Probability", 0.1),
        REPLANTING_DROPPED_SAPLINGS_DELAY("Replanting.Dropped.Delay Ticks", 5),
        REPLANTING_MATERIAL("Replanting.Material", "minecraft:air"),
        REPLANTING_ONLY_WHEN_BOTTOM_BLOCK_BROKEN_FIRST("Replanting.Only When Bottom Block Broken First", true),
        REPLANTING_PROTECT_FOR_SECONDS("Replanting.Protect For Seconds", 0),
        REPLANTING_REQUIRES_TOOLS("Replanting.Requires Tools", true),
        REPLANTING_WHEN_TREE_BURNS_DOWN("Replanting.When Tree Burns Down", true),

        TRUNK_BRANCH("Trunk.Branch", false),
        TRUNK_CUSTOM_DROPS("Trunk.Custom Drops", false), // Trunk will generate custom drops
        TRUNK_DIAGONAL("Trunk.Diagonal", false), // Trunk can move diagonally even (Acacia)
        TRUNK_MINIMUM_HEIGHT("Trunk.Minimum Height", 4),
        TRUNK_MATERIALS("Trunk.Materials", new ArrayList<>()), // the expected materials part of the tree trunk
        TRUNK_THICKNESS("Trunk.Thickness", 1), // This value is also used for radius calculation!
        TRUNK_UNEVEN_BOTTOM("Trunk.Uneven Bottom", false), // Can saplings/lowest trunks be on different Y?

        TOOL_LIST("Tool List", new ArrayList<>()),

        VERSION("Version", 7.0);

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

        CFG(final String node, final Map<String, Double> value) {
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
        TreeAssist.instance.getLogger().info("Loading tree config file: " + configFile.getAbsolutePath().replace(TreeAssist.instance.getDataFolder().getAbsolutePath(), ""));

        cfg = new YamlConfiguration();
        this.configFile = configFile;
        booleans = new HashMap<>();
        ints = new HashMap<>();
        doubles = new HashMap<>();
        strings = new HashMap<>();
        materials = new HashMap<>();
        maps = new HashMap<>();
    }

    /////////////
    //         //
    // LOADING //
    //         //
    /////////////

    /**
     * Clear all definition maps before loading it with default values
     */
    public void clearMaps() {
        booleans.clear();
        ints.clear();
        doubles.clear();
        strings.clear();
        materials.clear();
        maps.clear();
    }

    /**
     * Load defaults from a parent config
     *
     * @param parent the parent config
     */
    public void loadDefaults(TreeConfig parent) {
        for (CFG c : CFG.values()) {
            if (c.type.equals("string")) {
                strings.put(c.node, parent.getString(c));
            } else if (c.type.equals("boolean")) {
                booleans.put(c.node, parent.getBoolean(c));
            } else if (c.type.equals("int")) {
                ints.put(c.node, parent.getInt(c));
            } else if (c.type.equals("double")) {
                doubles.put(c.node, parent.getDouble(c));
            } else if (c.type.equals("list")) {
                List<Material> mats = parent.getMaterials(c);
                if (materials.containsKey(c.node)) {
                    materials.get(c.node).addAll(mats);
                } else {
                    materials.put(c.node, new ArrayList<>(mats));
                }
            } else if (c.type.equals("map")) {
                if (parent.maps.containsKey(c.node)) {
                    maps.put(c.node, new HashMap<>(parent.maps.get(c.node)));
                }
            }
        }
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
            TreeStructure.allNaturals.addAll(getMaterials(CFG.NATURAL_BLOCKS));
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
        // known exceptions (empty nodes)
        String[] exceptions = {"Automatic Destruction",
                "Block Statistics",
                "Blocks", "Blocks.Cap", "Blocks.Top", "Blocks.Middle",
                "Replanting", "Replanting.Dropped", "Trunk"};

        root: for (final String s : cfg.getKeys(true)) {
            final Object object = cfg.get(s);

            CFG node = CFG.getByNode(s);

            double value = 0;

            if (object instanceof Boolean) {
                if (node != null && node.type.equals("boolean")) {
                    booleans.put(s, (Boolean) object);
                } else if (node != null) {
                    TreeAssist.instance.getLogger().severe(configFile.getName() + ": " + s + " has unexpected boolean content, " + node.type + " expected - please fix!");
                }
            } else if (object instanceof Integer) {
                if (node != null && node.type.equals("int")) {
                    ints.put(s, (Integer) object);
                } else if (node != null && node.type.equals("double")) {
                    value = (Integer) object;
                    TreeAssist.instance.getLogger().warning(configFile.getName() + ": " + s + " expects double, integer given!");
                    doubles.put(s, value);
                } else if (node != null) {
                    TreeAssist.instance.getLogger().severe(configFile.getName() + ": " + s + " has unexpected integer content, " + node.type + " expected - please fix!");
                }
            } else if (object instanceof Double) {
                if (node != null && node.type.equals("double")) {
                    doubles.put(s, (Double) object);
                    value = (Double) object;
                } else if (node != null && node.type.equals("int")) {
                    value = (Double) object;
                    TreeAssist.instance.getLogger().warning(configFile.getName() + ": " + s + " expects integer, double given. Trying to round!");
                    ints.put(s, (int) value);
                } else if (node != null) {
                    TreeAssist.instance.getLogger().severe(configFile.getName() + ": " + s + " has unexpected double content, " + node.type + " expected - please fix!");
                }
            } else if (object instanceof String) {
                strings.put(s, (String) object);
                if (node != null && !node.type.equals("string")) {
                    TreeAssist.instance.getLogger().severe(configFile.getName() + ": " + s + " has unexpected string content, " + node.type + " expected - please fix!");
                }
            }

            if (node == null) {
                for (String test : exceptions) {
                    if (s.equals(test)) {
                        continue root;
                    }
                }

                String[] materialPaths = {"Custom Drops.", "Custom Drop Factor."};

                for (String test : materialPaths) {
                    if (s.startsWith(test)) {
                        String material = s.replace(test, "");
                        try {
                            Material testMaterial = Material.matchMaterial(material, false);
                            if (testMaterial != null) {
                                value = getYamlConfiguration().getDouble(s);
                                storeMapEntry(test.substring(0, test.length()-1), material, value);
                                continue root;
                            }
                            testMaterial = Material.matchMaterial(material, true);
                            if (testMaterial != null) {
                                value = getYamlConfiguration().getDouble(s);
                                storeMapEntry(test.substring(0, test.length()-1), material, value);
                                TreeAssist.instance.getLogger().warning("Legacy name used: " + material + " is now " + testMaterial.name());
                                continue root;
                            }
                            TreeAssist.instance.getLogger().warning("No valid material " + material + " in node " + s);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                TreeAssist.instance.getLogger().warning("No valid node: " + s);
            }
        }

        for (CFG cfg : CFG.values()) {
            if (cfg.type.equals("list")) {
                Set<Material> newMaterials = new LinkedHashSet<>();
                if (materials.containsKey(cfg.node)) {
                    // we already have entries from the parent(s)
                    newMaterials.addAll(materials.get(cfg.node));
                }
                newMaterials.addAll(this.readRawMaterials(cfg));
                materials.put(cfg.node, new ArrayList<>(newMaterials));
            }
        }
    }

    /**
     * Load the config file without filling our maps, mainly for checking for config changes
     */
    public void preLoad() {
        try {
            cfg.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            if (!this.getConfigName().contains("bush_jungle")) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Save the config to disk
     */
    public void save() {
        try {
            cfg.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Store a map entry into our map storage
     *
     * @param node   the node key
     * @param key    the map key
     * @param value  the map value
     */
    private void storeMapEntry(String node, String key, Double value) {
        //debug.i("adding " + configFile.getName() + " map entry " + node + " - " + key + " - " + value);
        if (maps.containsKey(node)) {
            maps.get(node).put(key, value);
        } else {
            Map<String, Double> map = new HashMap<>();
            map.put(key, value);
            maps.put(node, map);
        }
    }

    /////////////
    //         //
    // GETTERS //
    //         //
    /////////////

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

    public String getConfigName() {
        return configFile.getName();
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
     * Retrieve a Map Entry
     *
     * @param cfg the node to read
     * @param key the map key to read
     * @param def the fallback value in case there is no config setting
     * @return the map entry value
     */
    public double getMapEntry(CFG cfg, String key, double def) {
        if (maps.containsKey(cfg.node)) {
            Map<String, Double> map = maps.get(cfg.node);

            return map.containsKey(key) ? map.get(key) : def;
        } else {
            return def;
        }
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

    public Map<String, Double> getMap(CFG cfg) {
        if (maps.containsKey(cfg.node)) {
            return maps.get(cfg.node);
        }
        return new HashMap<>();
    }

    /**
     * Retrieve a list of materials from the value maps
     *
     * @param cfg the node of the value
     * @return a list of materials (can contain null)
     */
    public List<Material> getMaterials(CFG cfg) {
        if (materials.containsKey(cfg.node)) {
            return materials.get(cfg.node);
        }

        List<Material> matList = readRawMaterials(cfg);

        materials.put(cfg.node, matList);

        return matList;
    }

    public Material getMaterial(CFG node) {
        return Material.matchMaterial(getString(node));
    }

    public Set<String> getKeys(final String path) {
        if (cfg.get(path) == null) {
            return null;
        }

        final ConfigurationSection section = cfg.getConfigurationSection(path);
        return section == null ? new HashSet<>() : section.getKeys(false);
    }

    public List<String> getStringList(final CFG cfg, final List<String> def) {
        if (this.cfg.get(cfg.node) == null) {
            return def == null ? new LinkedList<>() : def;
        }

        return this.cfg.getStringList(cfg.node);
    }

    private List<Material> readRawMaterials(CFG cfg) {
        List<String> list = getStringList(cfg, new ArrayList<>());

        List<Material> matList = new ArrayList<>();

        for (String matName : list) {
            if (matName.contains("*")) {
                String needle = matName.substring(1).toLowerCase();
                for (Material mat : Material.values()) {
                    if (mat.name().toLowerCase().endsWith(needle)) {
                        matList.add(mat);
                    }
                }
            } else if (Material.matchMaterial(matName) != null){
                matList.add(Material.matchMaterial(matName));
            } else {
                TreeAssist.instance.getLogger().warning("Invalid Material in TreeConfig " + configFile.getName() + " - Node " + cfg.node + " entry invalid: " + matName);
            }
        }
        return matList;
    }
}
