package net.slipcor.treeassist.configs;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.core.TreeStructure;
import net.slipcor.treeassist.utils.StringUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
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
        AUTOMATIC_DESTRUCTION_ACTIVE("Automatic Destruction.Active", true, "Main switch to deactivate automatic destruction"),
        AUTOMATIC_DESTRUCTION_APPLY_FULL_TOOL_DAMAGE("Automatic Destruction.Apply Full Tool Damage", true, "Damage the player's tool for every block of the tree, not just the first they broke"),
        AUTOMATIC_DESTRUCTION_AUTO_ADD_TO_INVENTORY("Automatic Destruction.Auto Add To Inventory", false, "Add the logs the player's inventory"),
        AUTOMATIC_DESTRUCTION_CLEANUP_DELAY_TIME("Automatic Destruction.Cleanup Delay Seconds", 20, "Seconds to wait before (force) removing remnants of the tree"),
        AUTOMATIC_DESTRUCTION_COOLDOWN("Automatic Destruction.Cooldown Seconds", 0, "Time to wait before allowing the player to automatically destroy again"),
        AUTOMATIC_DESTRUCTION_CUSTOM_DROPS_OVERRIDE("Automatic Destruction.Custom Drops Override", false, "Custom Drops below completely replace the leaf drops"),
        AUTOMATIC_DESTRUCTION_DELAY("Automatic Destruction.Delay Ticks", 0, "Ticks to wait before breaking the next block, set to -1 for instant breaking"),
        AUTOMATIC_DESTRUCTION_FORCED_REMOVAL("Automatic Destruction.Forced Removal", false, "Always remove remnants of the tree, as soon as a tree has been verified and is being broken"),
        AUTOMATIC_DESTRUCTION_INCREASES_STATISTICS("Automatic Destruction.Increases Statistics", false, "Main switch for the Block Statistic nodes"),
        AUTOMATIC_DESTRUCTION_INITIAL_DELAY("Automatic Destruction.Initial Delay", false, "Initial Delay before actually starting to break the tree"),
        AUTOMATIC_DESTRUCTION_INITIAL_DELAY_TIME("Automatic Destruction.Initial Delay Seconds", 10, "Seconds to delay automatic destruction"),
        AUTOMATIC_DESTRUCTION_REMOVE_LEAVES("Automatic Destruction.Remove Leaves", true, "Remove not only logs, but also leaves"),
        AUTOMATIC_DESTRUCTION_REQUIRED_LORE("Automatic Destruction.Required Lore","", "Required lore on tool in order to automatically remove a tree. Empty means no requirement"),
        AUTOMATIC_DESTRUCTION_REQUIRES_TOOLS("Automatic Destruction.Requires Tools", true, "Only automatically destroy with the right tools, they are set in the tree definitions or via command"),
        AUTOMATIC_DESTRUCTION_WHEN_SNEAKING("Automatic Destruction.When Sneaking", true, "Automatically destroy when sneaking"),
        AUTOMATIC_DESTRUCTION_WHEN_NOT_SNEAKING("Automatic Destruction.When Not Sneaking", true, "Automatically destroy when not sneaking"),
        AUTOMATIC_DESTRUCTION_USE_SILK_TOUCH("Automatic Destruction.Use Silk Touch", true, "Support silk touch affect when a player has it"),

        BLOCK_STATISTICS_MINE_BLOCK("Block Statistics.Mine Block", false, "Count minecraft block breaking statistics when automatically breaking"),
        BLOCK_STATISTICS_PICKUP("Block Statistics.Pickup", false, "Count minecraft pickup statistics when automaticall adding blocks to inventory"),

        BLOCKS_CAP_HEIGHT("Blocks.Cap.Height", 2, "Max height of a branch cap"),
        BLOCKS_CAP_RADIUS("Blocks.Cap.Radius", 3, "Max radius of a branch cap"),

        BLOCKS_CUSTOM_DROPS("Blocks.Custom Drops", true, "Generate custom drops according to the list"),

        BLOCKS_MATERIALS("Blocks.Materials", new ArrayList<>(), "Here you can add extra blocks that can be expected inside or around tree leaves"),

        BLOCKS_MIDDLE_AIR("Blocks.Middle.Air", false, "Allow air pockets in leaves"),
        BLOCKS_MIDDLE_EDGES("Blocks.Middle.Edges", false, "Check cubic edges"),
        BLOCKS_MIDDLE_RADIUS("Blocks.Middle.Radius", 2, "Radius around the trunk to check for leaves"),

        BLOCKS_REQUIRED("Blocks.Required", 10, "How many leaves do we require for it to be a valid tree"),

        BLOCKS_TOP_AIR("Blocks.Top.Air", false, "Allow air pockets in leaves"),
        BLOCKS_TOP_EDGES("Blocks.Top.Edges", false, "Check cubic edges"),
        BLOCKS_TOP_RADIUS("Blocks.Top.Radius", 3, "Radius around the trunk to check for leaves"),
        BLOCKS_TOP_HEIGHT("Blocks.Top.Height", 3, "Height above the trunk to check for leaves"),

        BLOCKS_VINES("Blocks.Vines", false, "Do follow vines"), // do we need to look for vines?

        CUSTOM_DROPS("Custom Drops", new HashMap<>(), "Drop chances for extra drops. 1.0 would be 100% chance!"),
        CUSTOM_DROP_FACTOR("Custom Drop Factor", new HashMap<>(), "These are additional factors, for example, by default, iron has half the chance to get custom drops"),

        GROUND_BLOCKS("Ground Blocks", new ArrayList<>(), "Valid blocks that are below and around the saplings"),

        NATURAL_BLOCKS("Natural Blocks", new ArrayList<>(), "Blocks that you can expect to be around the tree - these are the exceptions from player building safeguards"),

        PARENT("Parent", "default", "The parent tree config to inherit from, recursively"),
        PERMISSION("Permission", "", "The permission required for this tree type"),

        REPLANTING_ACTIVE("Replanting.Active", true, "Main switch to deactivate sapling replanting"),
        REPLANTING_DELAY("Replanting.Delay", 1, "How long to wait before placing a sapling. Should stay above 0 because of bukkit event handling"),
        REPLANTING_DELAY_GROWTH_SECONDS("Replanting.Delay Growth Seconds", 0, "How long should saplings stay there before they can grow"),
        REPLANTING_DROPPED_SAPLINGS("Replanting.Dropped.Active", false, "Attempt to plant a dropped sapling item"),
        REPLANTING_DROPPED_SAPLINGS_PROBABILITY("Replanting.Dropped.Probability", 0.1, "What is the chance for us doing this, 1.0 means 100%"),
        REPLANTING_DROPPED_SAPLINGS_DELAY("Replanting.Dropped.Delay Ticks", 5, "How many ticks should we wait until attempting to plant it"),
        REPLANTING_ENFORCE("Replanting.Enforce", false, "Even if something would prevent sapling replacement or auto destruction, we will place a sapling"),
        REPLANTING_FORCE_PROTECT("Replanting.Force Protect", false, "Prevent from breaking this type of sapling at all costs"),
        REPLANTING_MATERIAL("Replanting.Material", "minecraft:air", "The material to place"),
        REPLANTING_ONLY_WHEN_BOTTOM_BLOCK_BROKEN_FIRST("Replanting.Only When Bottom Block Broken First", true, "Only place saplings when the bottom block was broken"),
        REPLANTING_PROTECT_FOR_SECONDS("Replanting.Protect For Seconds", 0, "How long to protect saplings"),
        REPLANTING_REQUIRES_TOOLS("Replanting.Requires Tools", true, "Only replant with the right tools, they are set in the tree definitions or via command"),
        REPLANTING_WHEN_TREE_BURNS_DOWN("Replanting.When Tree Burns Down", true, "Replant when a tree block burns"),

        TRUNK_BRANCH("Trunk.Branch", false, "Look for branches"),
        TRUNK_CUSTOM_DROPS("Trunk.Custom Drops", false, "Generate custom drops"),
        TRUNK_DIAGONAL("Trunk.Diagonal", false, "The trunk can go diagonally"),
        TRUNK_MINIMUM_HEIGHT("Trunk.Minimum Height", 4, "How high does it need to be to qualify as a tree"),
        TRUNK_MATERIALS("Trunk.Materials", new ArrayList<>(), "One of these materials needs to be part of the trunk for it to count as a trunk"),
        TRUNK_THICKNESS("Trunk.Thickness", 1, "How thick is the trunk"),
        TRUNK_UNEVEN_BOTTOM("Trunk.Uneven Bottom", false, "Saplings can be at different height"),

        TOOL_LIST("Tool List", new ArrayList<>(), "This is the list that can be required to use when auto destructing or sapling replanting"),

        VERSION("Version", 7.0, "Version number for automagical config updates");

        private final String node;
        private final Object value;
        private final String type;
        private final String comment;

        public static CFG getByNode(final String node) {
            for (final CFG m : CFG.getValues()) {
                if (m.node.equals(node)) {
                    return m;
                }
            }
            return null;
        }

        CFG(final String node, final String value, String comment) {
            this.node = node;
            this.value = value;
            type = "string";
            this.comment = comment;
        }

        CFG(final String node, final Boolean value, String comment) {
            this.node = node;
            this.value = value;
            type = "boolean";
            this.comment = comment;
        }

        CFG(final String node, final Integer value, String comment) {
            this.node = node;
            this.value = value;
            type = "int";
            this.comment = comment;
        }

        CFG(final String node, final Double value, String comment) {
            this.node = node;
            this.value = value;
            type = "double";
            this.comment = comment;
        }

        CFG(final String node, final List<String> value, String comment) {
            this.node = node;
            this.value = value;
            this.type = "list";
            this.comment = comment;
        }

        CFG(final String node, final Map<String, Double> value, String comment) {
            this.node = node;
            this.value = value;
            this.type = "map";
            this.comment = comment;
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
     * Append the comments.
     *
     * Iterate over the config file and add comments, if we didn't do that already.
     */
    private void appendComments() {
        try {

            final FileInputStream fis = new FileInputStream(configFile);
            final DataInputStream dis = new DataInputStream(fis);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(dis));

            final File tempFile = new File(TreeAssist.instance.getDataFolder(), "config-temp.yml");
            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }

            final FileOutputStream fos = new FileOutputStream(tempFile);
            final DataOutputStream dos = new DataOutputStream(fos);
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(dos));

            String readLine;

            int indent = 0;

            String key = null;

            while ((readLine = reader.readLine()) != null) {
                if (key == null && writer.toString().length() < 1 && !readLine.startsWith("#")) {
                    writer.append("# === [ TreeAssist TreeConfig ] ===");
                    writer.newLine();
                }

                if (readLine.trim().startsWith("#")) {
                    continue;
                }

                final int firstContentCharacter = (indent * 2);

                if (readLine.contains(":")) {
                    final String newStringLine = readLine.split(":")[0] + ":";
                    int pos;
                    final StringBuilder builder = new StringBuilder();
                    int newDigit = -1;

                    for (pos = 0; pos<newStringLine.length(); pos++) {
                        if (newStringLine.charAt(pos) != ' '
                                && newStringLine.charAt(pos) != ':') {
                            if (newDigit == -1) {
                                newDigit = pos;
                            }
                            builder.append(newStringLine.charAt(pos));
                        } else if (newStringLine.charAt(pos) == ' ') {
                            if (builder.length() > 0) {
                                builder.append(newStringLine.charAt(pos));
                            }
                        } else if (newStringLine.charAt(pos) != ':') {
                            builder.append(newStringLine.charAt(pos));
                        }
                    }

                    if (key == null) {
                        key = builder.toString();
                    }

                    String[] split = key.split("\\.");

                    if (newDigit > firstContentCharacter) {
                        indent++;

                        final String[] newString = new String[split.length+1];
                        System.arraycopy(split, 0, newString, 0, split.length);
                        newString[split.length] = builder.toString();
                        split = newString;
                    } else if (newDigit < firstContentCharacter) {

                        indent = (newDigit/2);

                        final String[] newString = new String[indent+1];

                        System.arraycopy(split, 0, newString, 0, indent);

                        newString[newString.length-1] = builder.toString();
                        split = newString;
                    } else {
                        split[split.length-1] = builder.toString();
                    }

                    final StringBuilder buffer = new StringBuilder();
                    for (String string : split) {
                        buffer.append('.');
                        buffer.append(string);
                    }

                    key = buffer.substring(1);

                    final TreeConfig.CFG entry = TreeConfig.CFG.getByNode(key);

                    if (entry == null) {
                        writer.append(readLine);
                        writer.newLine();
                        continue;
                    }

                    final StringBuilder value = new StringBuilder();

                    for (int k=0; k<indent; k++) {
                        value.append("  ");
                    }
                    if (entry.comment != null && !entry.comment.isEmpty()) {
                        writer.append(value);
                        writer.append("# ");
                        writer.append(entry.comment);
                        writer.newLine();
                    }
                }
                writer.append(readLine);
                writer.newLine();
            }

            writer.flush();
            writer.close();
            reader.close();

            if (!configFile.delete()) {
                TreeAssist.instance.getLogger().severe("Could not delete un-commented config!");
            }
            if (!tempFile.renameTo(configFile)) {
                TreeAssist.instance.getLogger().severe("Could not rename Config!");
            }

        } catch (Exception e) {
            e.printStackTrace();
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
        appendComments();
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
            appendComments();
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
