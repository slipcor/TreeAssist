package net.slipcor.treeassist.configs;

import net.slipcor.treeassist.TreeAssist;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.*;

public class MainConfig {
    private final YamlConfiguration cfg;
    private final File configFile;
    private final Map<String, Boolean> booleans;
    private final Map<String, Integer> ints;
    private final Map<String, Double> doubles;
    private final Map<String, String> strings;

    public void save() {
        try {
            cfg.save(configFile);
            appendComments();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public enum CFG {
        GENERAL("General", "=== [ General Settings ] ==="),
        GENERAL_LANGUAGE("General.Language", "lang_en", "Language file to load. Does not need YML extension!"),
        GENERAL_TOGGLE_DEFAULT("General.Toggle Default", true, "For the toggling command, should players start with TreeAssist active?"),
        GENERAL_USE_PERMISSIONS("General.Use Permissions", false, "Should we check if players have permissions? If false, all features are available to everyone."),

        COMMANDS("Commands", "=== [ Command Settings ] ==="),
        COMMANDS_FORCE_BREAK_DEFAULT_RADIUS("Commands.Force Break.Default Radius", 10, ""),
        COMMANDS_FORCE_BREAK_MAX_RADIUS("Commands.Force Break.Max Radius", 30, ""),
        COMMANDS_FORCE_GROW_DEFAULT_RADIUS("Commands.Force Grow.Default Radius", 10, ""),
        COMMANDS_FORCE_GROW_MAX_RADIUS("Commands.Force Grow.Max Radius", 30, ""),
        COMMANDS_NOREPLANT_COMMAND_TIME_COOLDOWN("Commands.No Replant.Cooldown Time", 30, ""),

        DESTRUCTION("Destruction", "=== [ Automatic Destruction Settings ] ==="),
        DESTRUCTION_FALLING_BLOCKS("Destruction.Falling Blocks", false, "Spawn a FallingBlock when breaking a block"),
        DESTRUCTION_FAST_LEAF_DECAY("Destruction.Fast Leaf Decay", true, "Increase leaf decay by looking for nearby lonely leaves"),
        DESTRUCTION_ONLY_ABOVE("Destruction.Only Above", false, "Only break blocks that are above the block the player broke"),

        PLUGINS("Plugins", "=== [ Plugin Integration Settings ] ==="),
        PLUGINS_USE_MCMMO("Plugins.mcMMO", true, "Count broken blocks towards the TreeFeller ability"),
        PLUGINS_USE_JOBS("Plugins.Jobs", true, "Count broken blocks towards Jobs jobs that fit"),
        PLUGINS_USE_WORLDGUARD("Plugins.WorldGuard", false, "Allow to set up regions with WorldGuard to prevent destruction with 'treeassist-autochop' and sapling replacement with 'treeassist-replant'"),

        WORLDS("Worlds", "=== [ World Related Settings ] ==="),
        WORLDS_RESTRICT("Worlds.Restrict", false, "Enable world based restrictions"),
        WORLDS_ENABLED_WORLDS("Worlds.Enabled Worlds", new ArrayList<>(Arrays.asList("world", "world2")), "Worlds that are not restricted"),

        PLACED_BLOCKS("Placed Blocks", "=== [ Placed Blocks Settings ] ==="),
        PLACED_BLOCKS_ACTIVE("Placed Blocks.Active", true, "Check for player placed blocks"),
        PLACED_BLOCKS_PLUGIN_NAME("Placed Blocks.Plugin Name", "TreeAssist", "A plugin that will look for placed blocks (Prism, LogBlock, CoreProtect)"),
        PLACED_BLOCKS_LOOKUP_TIME("Placed Blocks.Lookup Time", 86400, "How many seconds back we want to look"),

        MODDING_DISABLE_DURABILITY_FIX("Modding.Disable Durability Fix", false, "This is for hacky mods that use infinity durability"),

        VERSION("Version", 7.0, "The config version for update checks"),
        DEBUG("Debug", "none", "");

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

        CFG(final String node, String comment) {
            this.node = node;
            this.value = comment;
            type = "comment";
            this.comment = comment;
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
    public MainConfig(final File configFile) {
        TreeAssist.instance.getLogger().info("Loading main config file: " + configFile.getAbsolutePath().replace(TreeAssist.instance.getDataFolder().getAbsolutePath(), ""));

        cfg = new YamlConfiguration();
        this.configFile = configFile;

        if (MainConfigUpdater.check(cfg)) {
            save();
        }

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
            if (cfg.contains("Main.Use Permissions")) {
                ConfigV7Updater.commit();
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
     * Append the comments.
     *
     * Iterate over the config file and add comments, if we didn't do that already.
     */
    private void appendComments() {
        final File ymlFile = new File(TreeAssist.instance.getDataFolder(), "config.yml");

        try {

            final FileInputStream fis = new FileInputStream(ymlFile);
            final DataInputStream dis = new DataInputStream(fis);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(dis));

            final File tempFile = new File(TreeAssist.instance.getDataFolder(), "config-temp.yml");
            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }

            final FileOutputStream fos = new FileOutputStream(tempFile);
            final DataOutputStream dos = new DataOutputStream(fos);
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(dos));

            String stringLine;

            int indent = 0;

            String key = null;

            while ((stringLine = reader.readLine()) != null) {

                if (key == null && writer.toString().length() < 1 && !stringLine.startsWith("#")) {
                    writer.append("# === [ TreeAssist Config ] ===");
                    writer.newLine();
                }

                if (stringLine.trim().startsWith("#")) {
                    writer.flush();
                    writer.close();
                    reader.close();
                    tempFile.delete();
                    return;
                }

                final int firstDigit = (indent * 2);

                if (stringLine.startsWith("#") || stringLine.length() < firstDigit+1 || stringLine.charAt(firstDigit) == '#') {

                    writer.append(stringLine);
                    writer.newLine();
                    continue;
                }

                if (stringLine.contains(":")) {
                    final String newStringLine = stringLine.split(":")[0] + ":";
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
                        }
                    }

                    if (key == null) {
                        key = builder.toString();
                    }

                    String[] split = key.split("\\.");

                    if (newDigit > firstDigit) {
                        indent++;

                        final String[] newString = new String[split.length+1];
                        System.arraycopy(split, 0, newString, 0, split.length);
                        newString[split.length] = builder.toString();
                        split = newString;
                    } else if (newDigit < firstDigit) {

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

                    final CFG entry = CFG.getByNode(key);

                    if (entry == null) {
                        writer.append(stringLine);
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
                writer.append(stringLine);
                writer.newLine();
            }

            writer.flush();
            writer.close();
            reader.close();

            if (!ymlFile.delete()) {
                TreeAssist.instance.getLogger().severe("Could not delete un-commented config!");
            }
            if (!tempFile.renameTo(ymlFile)) {
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
        String[] exceptions = {"General", "Commands", "Commands.Force Break", "Commands.Force Grow", "Commands.No Replant",
                "Destruction", "Placed Blocks", "Plugins", "Worlds", "Modding"};

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

                TreeAssist.instance.getLogger().warning("No valid node: " + s);
            }
        }
        appendComments();
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

    public List<String> getStringList(final CFG cfg, final List<String> def) {
        if (this.cfg.get(cfg.node) == null) {
            return def == null ? new LinkedList<>() : def;
        }

        return this.cfg.getStringList(cfg.node);
    }
}
