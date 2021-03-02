package net.slipcor.treeassist.configs;

import net.slipcor.treeassist.TreeAssist;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public abstract class CommentableConfig {

    protected final YamlConfiguration cfg;
    protected final File configFile;

    protected final Map<String, Boolean> booleans;
    protected final Map<String, Integer> ints;
    protected final Map<String, Double> doubles;
    protected final Map<String, String> strings;

    protected String[] emptyNodes;

    public CommentableConfig(File configFile) {
        cfg = new YamlConfiguration();
        this.configFile = configFile;

        booleans = new HashMap<>();
        ints = new HashMap<>();
        doubles = new HashMap<>();
        strings = new HashMap<>();
    }

    /**
     * Append the comments.
     *
     * Iterate over the config file and add comments, if we didn't do that already.
     */
    protected void appendComments() {
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

                    final ConfigEntry entry = this.getByNode(key);

                    if (entry == null) {
                        writer.append(readLine);
                        writer.newLine();
                        continue;
                    }

                    final StringBuilder value = new StringBuilder();

                    for (int k=0; k<indent; k++) {
                        value.append("  ");
                    }
                    if (entry.getComment() != null && !entry.getComment().isEmpty()) {
                        writer.append(value);
                        writer.append("# ");
                        writer.append(entry.getComment());
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

    public abstract ConfigEntry getByNode(final String node);

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
    public boolean getBoolean(final ConfigEntry cfg) {
        return getBoolean(cfg, (Boolean) cfg.getValue());
    }

    /**
     * Retrieve a boolean from the value maps.
     *
     * @param cfg the node of the value
     * @param def a default value to return if the value was not in the map
     * @return the boolean value of the path if it exists, def otherwise
     */
    private boolean getBoolean(final ConfigEntry cfg, final boolean def) {
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
    public int getInt(final ConfigEntry cfg) {
        return getInt(cfg, (Integer) cfg.getValue());
    }

    /**
     * Retrieve an int from the value maps.
     *
     * @param cfg the node of the value
     * @param def a default value to return if the value was not in the map
     * @return the int value of the path if it exists, def otherwise
     */
    public int getInt(final ConfigEntry cfg, final int def) {
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
    public double getDouble(final ConfigEntry cfg) {
        return getDouble(cfg, (Double) cfg.getValue());
    }

    /**
     * Retrieve a double from the value maps.
     *
     * @param cfg the node of the value
     * @param def a default value to return if the value was not in the map
     * @return the double value of the path if it exists, def otherwise
     */
    public double getDouble(final ConfigEntry cfg, final double def) {
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
    public String getString(final ConfigEntry cfg) {
        return getString(cfg, (String) cfg.getValue());
    }

    /**
     * Retrieve a string from the value maps.
     *
     * @param cfg the node of the value
     * @param def a default value to return if the value was not in the map
     * @return the string value of the path if it exists, def otherwise
     */
    public String getString(final ConfigEntry cfg, final String def) {
        final String path = cfg.getNode();
        final String result = strings.get(path);
        return result == null ? def : result;
    }

    public List<String> getStringList(final ConfigEntry cfg, final List<String> def) {
        if (this.cfg.get(cfg.getNode()) == null) {
            return def == null ? new LinkedList<>() : def;
        }

        return this.cfg.getStringList(cfg.getNode());
    }

    /**
     * Check for invalid materials
     *
     * @param node the full node ending in the material name
     *
     * @return whether the material is valid or legacy
     */
    abstract protected boolean checkMaterials(String node);


    /**
     * Iterates through all keys in the config-file, and populates the value
     * maps. Boolean values are stored in the booleans-map, Strings in the
     * strings-map, etc.
     */
    public void reloadMaps() {
        root: for (final String s : cfg.getKeys(true)) {
            final Object object = cfg.get(s);

            ConfigEntry node = getByNode(s);

            if (object instanceof Boolean) {
                if (node != null && node.getType() == ConfigEntry.Type.BOOLEAN) {
                    booleans.put(s, (Boolean) object);
                } else if (node != null) {
                    TreeAssist.instance.getLogger().severe(configFile.getName() + ": " + s + " has unexpected boolean content, " + node.getType() + " expected - please fix!");
                }
            } else if (object instanceof Integer) {
                if (node != null && node.getType() == ConfigEntry.Type.INT) {
                    ints.put(s, (Integer) object);
                } else if (node != null && node.getType() == ConfigEntry.Type.DOUBLE) {
                    double value = (Integer) object;
                    TreeAssist.instance.getLogger().warning(configFile.getName() + ": " + s + " expects double, integer given!");
                    doubles.put(s, value);
                } else if (node != null) {
                    TreeAssist.instance.getLogger().severe(configFile.getName() + ": " + s + " has unexpected integer content, " + node.getType() + " expected - please fix!");
                }
            } else if (object instanceof Double) {
                if (node != null && node.getType() == ConfigEntry.Type.DOUBLE) {
                    doubles.put(s, (Double) object);
                } else if (node != null && node.getType() == ConfigEntry.Type.INT) {
                    double value = (Double) object;
                    TreeAssist.instance.getLogger().warning(configFile.getName() + ": " + s + " expects integer, double given. Trying to round!");
                    ints.put(s, (int) value);
                } else if (node != null) {
                    TreeAssist.instance.getLogger().severe(configFile.getName() + ": " + s + " has unexpected double content, " + node.getType() + " expected - please fix!");
                }
            } else if (object instanceof String) {
                strings.put(s, (String) object);
                if (node != null && node.getType()!= ConfigEntry.Type.STRING) {
                    TreeAssist.instance.getLogger().severe(configFile.getName() + ": " + s + " has unexpected string content, " + node.getType() + " expected - please fix!");
                }
            }

            if (node == null) {

                for (String test : emptyNodes) {
                    if (s.equals(test)) {
                        continue root;
                    }
                }

                if (checkMaterials(s)) {
                    continue;
                }

                TreeAssist.instance.getLogger().warning("No valid node: " + s);
            }
        }
        loadMaterials();
        appendComments();
    }

    protected abstract void loadMaterials();

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
}
