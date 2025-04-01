package net.slipcor.treeassist.yml;

import net.slipcor.core.ConfigEntry;
import net.slipcor.core.CoreConfig;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.discovery.TreeStructure;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TreeConfig extends CoreConfig {
    private final Map<String, List<Material>> materials;
    private final Map<String, List<String>> stringLists;
    private final Map<String, Map<String, Double>> maps;
    private TreeConfig parent;

    public enum CFG implements ConfigEntry {
        AUTOMATIC_DESTRUCTION_ACTIVE("Automatic Destruction.Active", true, "Main switch to deactivate automatic destruction"),
        AUTOMATIC_DESTRUCTION_APPLY_FULL_TOOL_DAMAGE("Automatic Destruction.Apply Full Tool Damage", true, "Damage the player's tool for every block of the tree, not just the first they broke"),
        AUTOMATIC_DESTRUCTION_TOOL_DAMAGE_FOR_LEAVES("Automatic Destruction.Apply Tool Damage For Leaves", true, "Damage the player's tool for leaves broken automatically"),
        AUTOMATIC_DESTRUCTION_AUTO_ADD_TO_INVENTORY("Automatic Destruction.Auto Add To Inventory", false, "Add the tree drops to the player's inventory"),
        AUTOMATIC_DESTRUCTION_AUTO_ADD_ONLY_LOGS_TO_INVENTORY("Automatic Destruction.Auto Add Only Logs To Inventory", false, "Only add the logs to the player's inventory"),
        AUTOMATIC_DESTRUCTION_AUTO_ADD_DROP_FAILED("Automatic Destruction.Auto Add Drop Failed", false, "Drop items that did not fit into the inventory"),
        AUTOMATIC_DESTRUCTION_CLEANUP_DELAY_TIME("Automatic Destruction.Cleanup Delay Seconds", 20, "Seconds to wait before (force) removing remnants of the tree"),
        AUTOMATIC_DESTRUCTION_CLEANUP_LEAVES("Automatic Destruction.Cleanup Leaves", true, "If we clean up remnants, we also will remove leaves without drops"),
        AUTOMATIC_DESTRUCTION_COOLDOWN("Automatic Destruction.Cooldown Seconds", 0, "Time to wait before allowing the player to automatically destroy again"),
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

        BLOCKS_CUSTOM_DROPS_ACTIVE("Blocks.Custom Drops.Active", true, "Generate custom drops according to the list"),
        BLOCKS_CUSTOM_DROPS_ITEMS("Blocks.Custom Drops.Items", new HashMap<>(), "Drop chances for extra drops. 1.0 would be 100% chance!"),
        BLOCKS_CUSTOM_DROPS_FACTORS("Blocks.Custom Drops.Factors", new HashMap<>(), "These are additional factors, for example, by default, iron has half the chance to get custom drops"),
        BLOCKS_CUSTOM_DROPS_OVERRIDE("Blocks.Custom Drops.Override", false, "Custom Drops below completely replace the leaf drops"),

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

        COMMANDS_PER_BLOCK("Commands.Per Block", new ArrayList<>(), "These commands will be issued when a tree is felled by a player"),
        COMMANDS_PER_TREE("Commands.Per Tree", new ArrayList<>(), "These commands will be issued when a block is broken for a player"),

        GROUND_BLOCKS("Ground Blocks", new ArrayList<>(), "Valid blocks that are below and around the saplings"),

        NATURAL_BLOCKS("Natural Blocks", new ArrayList<>(), "Blocks that you can expect to be around the tree - these are the exceptions from player building safeguards"),

        PARENT("Parent", "default", "The parent tree config to inherit from, recursively"),
        PERMISSION("Permission", "", "The permission required for this tree type"),

        REPLANTING_ACTIVE("Replanting.Active", true, "Main switch to deactivate sapling replanting"),
        REPLANTING_DELAY("Replanting.Delay", 1, "How long to wait before placing a sapling. Should stay above 0 because of bukkit event handling"),
        REPLANTING_CHECK_DEPTH("Replanting.Check Depth", 12, "How far down do we look for saplings to place them"),
        REPLANTING_DROPPED_SAPLINGS("Replanting.Dropped.Active", false, "Attempt to plant a dropped sapling item"),
        REPLANTING_DROPPED_SAPLINGS_PROBABILITY("Replanting.Dropped.Probability", 0.1, "What is the chance for us doing this, 1.0 means 100%"),
        REPLANTING_DROPPED_SAPLINGS_DELAY("Replanting.Dropped.Delay Ticks", 5, "How many ticks should we wait until attempting to plant it"),
        REPLANTING_ENFORCE("Replanting.Enforce", false, "Even if something would prevent sapling replacement or auto destruction, we will place a sapling"),
        REPLANTING_FORCE_PROTECT("Replanting.Force Protect", false, "Prevent from breaking this type of sapling at all costs"),
        REPLANTING_GROWTH_DELAY_SECONDS("Replanting.Growth Delay Seconds", 0, "How long should saplings stay there before they can grow"),
        REPLANTING_MATERIAL("Replanting.Material", "minecraft:air", "The material to place"),
        REPLANTING_ONLY_WHEN_BOTTOM_BLOCK_BROKEN_FIRST("Replanting.Only When Bottom Block Broken First", true, "Only place saplings when the bottom block was broken"),
        REPLANTING_PROTECT_FOR_SECONDS("Replanting.Protect For Seconds", 0, "How long to protect saplings"),
        REPLANTING_REQUIRES_TOOLS("Replanting.Requires Tools", true, "Only replant with the right tools, they are set in the tree definitions or via command"),
        REPLANTING_WHEN_TREE_BURNS_DOWN("Replanting.When Tree Burns Down", true, "Replant when a tree block burns"),

        TRUNK_BRANCH("Trunk.Branch", false, "Look for branches"),

        TRUNK_CUSTOM_DROPS_ACTIVE("Trunk.Custom Drops.Active", false, "Generate custom drops"),
        TRUNK_CUSTOM_DROPS_ITEMS("Trunk.Custom Drops.Items", new HashMap<>(), "Drop chances for extra drops. 1.0 would be 100% chance!"),
        TRUNK_CUSTOM_DROPS_FACTORS("Trunk.Custom Drops.Factors", new HashMap<>(), "These are additional factors, for example, by default, iron has half the chance to get custom drops"),
        TRUNK_CUSTOM_DROPS_OVERRIDE("Trunk.Custom Drops.Override", false, "The configured drops override regular drops, including logs!"),

        TRUNK_DIAGONAL("Trunk.Diagonal", false, "The trunk can go diagonally"),
        TRUNK_GREEDY("Trunk.Greedy", false, "Try to recognize as many blocks as possible as trunk blocks"),
        TRUNK_MAXIMUM_HEIGHT("Trunk.Maximum Height", -1, "How high can it be to qualify as a tree"),

        TRUNK_MINIMUM_HEIGHT("Trunk.Minimum Height", 4, "How high does it need to be to qualify as a tree"),
        TRUNK_MATERIALS("Trunk.Materials", new ArrayList<>(), "One of these materials needs to be part of the trunk for it to count as a trunk"),
        TRUNK_THICKNESS("Trunk.Thickness", 1, "How thick is the trunk"),
        TRUNK_UNEVEN_BOTTOM("Trunk.Uneven Bottom", false, "Saplings can be at different height"),

        TOOL_LIST("Tool List", new ArrayList<>(), "This is the list that can be required to use when auto destructing or sapling replanting"),

        VERSION("Version", 7.0, "Version number for automagical config updates");

        private final String node;
        private final Object value;
        private final Type type;
        private final String comment;

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

        CFG(final String node, final Map<String, Double> value, String comment) {
            this.node = node;
            this.value = value;
            this.type = Type.MAP;
            this.comment = comment;
        }

        public static TreeConfig.CFG getByNode(String node) {
            for (final TreeConfig.CFG e : TreeConfig.CFG.values()) {
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
     * Create a new TreeConfig instance that uses the specified file for loading
     *
     * @param configFile a YAML file
     */
    public TreeConfig (final TreeAssist plugin, final File configFile) {
        super(plugin, "TreeAssist Tree Config", configFile);

        if (plugin.config().getBoolean(MainConfig.CFG.GENERAL_VERBOSE_CONFIG_LOADING)) {
            plugin.getLogger().info(
                    "Loading tree config file: " +
                            configFile.getAbsolutePath().replace(
                                    TreeAssist.instance.getDataFolder().getAbsolutePath(), ""));
        }

        materials = new HashMap<>();
        stringLists = new HashMap<>();
        maps = new HashMap<>();

        emptyNodes = new String[]{
                "Automatic Destruction",  "Block Statistics", "Commands",
                "Blocks", "Blocks.Custom Drops", "Blocks.Cap", "Blocks.Top", "Blocks.Middle",
                "Replanting", "Replanting.Dropped", "Trunk", "Trunk.Custom Drops"
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
        stringLists.clear();
        maps.clear();
    }

    /**
     * Load defaults from a parent config
     *
     * @param parent the parent config
     */
    public void loadDefaults(TreeConfig parent) {
        if (!getConfigName().equals("default.yml")) {
            this.parent = parent;
        }

        for (CFG c : CFG.values()) {
            if (c.type == ConfigEntry.Type.STRING) {
                strings.put(c.node, parent.getString(c));
            } else if (c.type == ConfigEntry.Type.BOOLEAN) {
                booleans.put(c.node, parent.getBoolean(c));
            } else if (c.type == ConfigEntry.Type.INT) {
                ints.put(c.node, parent.getInt(c));
            } else if (c.type == ConfigEntry.Type.DOUBLE) {
                doubles.put(c.node, parent.getDouble(c));
            } else if (c.type == ConfigEntry.Type.LIST) {
                if (c == CFG.COMMANDS_PER_BLOCK || c == CFG.COMMANDS_PER_TREE) {
                    List<String> strings = parent.getStringList(c);
                    if (stringLists.containsKey(c.node)) {
                        stringLists.get(c.node).addAll(strings);
                    } else {
                        stringLists.put(c.node, new ArrayList<>(strings));
                    }
                } else {
                    List<Material> mats = parent.getMaterials(c);
                    if (materials.containsKey(c.node)) {
                        materials.get(c.node).addAll(mats);
                    } else {
                        materials.put(c.node, new ArrayList<>(mats));
                    }
                }
            } else if (c.type == ConfigEntry.Type.MAP) {
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
            Material replantMat = getMaterial(CFG.REPLANTING_MATERIAL);
            if (!replantMat.equals(Material.AIR)) {
                TreeStructure.allSaplings.add(replantMat);
            }
            TreeStructure.allExtras.addAll(getMaterials(TreeConfig.CFG.BLOCKS_MATERIALS));
            TreeStructure.allNaturals.addAll(getMaterials(CFG.NATURAL_BLOCKS));
            return true;
        } catch (final Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Fill the materials map
     */
    @Override
    protected void loadMaterials() {
        for (CFG cfg : CFG.values()) {
            if (cfg.type == ConfigEntry.Type.LIST) {

                if (cfg == CFG.COMMANDS_PER_TREE || cfg == CFG.COMMANDS_PER_BLOCK) {
                    List<String> newCommands = new ArrayList<>();
                    if (stringLists.containsKey(cfg.getNode())) {
                        // we already have entries from the parent(s)
                        newCommands.addAll(stringLists.get(cfg.getNode()));
                    }
                    newCommands.addAll(this.getStringList(cfg, null));
                    stringLists.put(cfg.getNode(), newCommands);
                } else {
                    Set<Material> newMaterials = new LinkedHashSet<>();
                    if (materials.containsKey(cfg.getNode())) {
                        // we already have entries from the parent(s)
                        newMaterials.addAll(materials.get(cfg.getNode()));
                    }
                    newMaterials.addAll(this.readRawMaterials(cfg));
                    materials.put(cfg.getNode(), new ArrayList<>(newMaterials));
                }
            }
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

        String[] stringPaths = {"Commands.Per Tree", "Commands.Per Block"};

        for (String test : stringPaths) {
            if (node.startsWith(test)) {
                return true;
            }
        }

        String[] materialPaths = {"Blocks.Custom Drops.Items.", "Blocks.Custom Drops.Factors.", "Trunk.Custom Drops.Items.", "Trunk.Custom Drops.Factors."};

        for (String test : materialPaths) {
            if (node.startsWith(test)) {
                String material = node.replace(test, "");
                try {
                    Material testMaterial = Material.matchMaterial(material, false);
                    if (testMaterial != null) {
                        Double value = getYamlConfiguration().getDouble(node);
                        storeMapEntry(test.substring(0, test.length()-1), material, value);
                        return true;
                    }
                    testMaterial = Material.matchMaterial(material, true);
                    if (testMaterial != null) {
                        Double value = getYamlConfiguration().getDouble(node);
                        storeMapEntry(test.substring(0, test.length()-1), material, value);
                        TreeAssist.instance.getLogger().warning("Legacy name used: " + material + " is now " + testMaterial.name());
                        return true;
                    }
                    TreeAssist.instance.getLogger().warning("No valid material " + material + " in node " + node);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
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

    public String getConfigName() {
        return configFile.getName();
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

    /**
     * Retrieve a list of materials from the value maps, overriding with parent if not set and a parent config exists
     *
     * @param cfg the node of the value
     * @return a list of materials (can contain null)
     */
    public List<Material> getInheritedMaterials(CFG cfg) {
        if (materials.containsKey(cfg.node)) {
            return materials.get(cfg.node);
        }

        List<Material> matList = this.readRawInheritedMaterials(cfg);

        materials.put(cfg.node, matList);

        return matList;
    }

    public TreeConfig getParent() {
        return parent;
    }

    /**
     * Retrieve a list of materials from the value maps
     *
     * @param cfg the node of the value
     * @return a list of materials (can contain null)
     */
    public List<String> getStringList(ConfigEntry cfg) {
        if (stringLists.containsKey(cfg.getNode())) {
            return stringLists.get(cfg.getNode());
        }

        List<String> list = getStringList(cfg, null);

        stringLists.put(cfg.getNode(), list);

        return list;
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

    private List<Material> readRawInheritedMaterials(CFG cfg) {
        List<String> list = getInheritedStringList(cfg, new ArrayList<>());

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

    public List<String> getInheritedStringList(ConfigEntry cfg, List<String> def) {
        if (this.cfg.get(cfg.getNode()) == null) {
            if (this.getParent() == null) {
                return (List)(def == null ? new LinkedList() : def);
            }
            return this.getParent().getInheritedStringList(cfg, def);
        } else {
            return this.cfg.getStringList(cfg.getNode());
        }
    }
}
