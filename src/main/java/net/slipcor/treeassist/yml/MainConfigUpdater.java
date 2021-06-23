package net.slipcor.treeassist.yml;

import net.slipcor.treeassist.TreeAssist;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;

public class MainConfigUpdater {
    enum Moving {
        //APPLY_TOOL_DAMAGE(7.0f, "Main.Apply Full Tool Damage", "Automatic Tree Destruction.Apply Full Tool Damage"),
        //AUTO_ADD_TO_INVENTORY(7.0f, "Main.Auto Add To Inventory", "Automatic Tree Destruction.Auto Add To Inventory"),
        //INITIAL_DELAY(7.0f, "Main.Initial Delay", "Automatic Tree Destruction.Initial Delay")
        ;

        private final float version;
        private final String source;
        private final String destination;

        /**
         * A moving definition
         *
         * @param version the version it was introduced
         * @param source the source path
         * @param destination the destination path
         */
        Moving(float version, String source, String destination) {
            this.version = version;
            this.source = source;
            this.destination = destination;
        }
    }

    enum TreeAddition {
        AZALEA(7.2012f, "overworld", "azalea.yml");

        private final float version;
        private final String path;
        private final String file;

        TreeAddition(float v, String p, String f) {
            version = v;
            path = p;
            file = f;
        }
    }

    enum TreeRemoval {
        JUNGLE_BUSH(7.0142f, "overworld", "bush_jungle.yml");

        private final float version;
        private final String path;
        private final String file;

        TreeRemoval(float v, String p, String f) {
            version = v;
            path = p;
            file = f;
        }
    }

    enum Adding {

        DESTRUCTION_FALLING_BLOCKS_FANCY(7.0117f, MainConfig.CFG.DESTRUCTION_FALLING_BLOCKS_FANCY, false),
        BSTATS_ACTIVE(7.0147f, MainConfig.CFG.BSTATS_ACTIVE, true),
        BSTATS_FULL(7.0147f, MainConfig.CFG.BSTATS_FULL, true),
        UPDATE_MODE(7.1010f, MainConfig.CFG.UPDATE_MODE, "both"),
        UPDATE_TYPE(7.1010f, MainConfig.CFG.UPDATE_TYPE, "beta"),
        FULL_MCMMO_TREE(7.1011f, MainConfig.CFG.PLUGINS_USE_TREEMCMMO, true),
        FULL_JOBS_TREE(7.1011f, MainConfig.CFG.PLUGINS_USE_TREEJOBS, true),
        VERBOSE_CONFIG_LOADING(7.2013f, MainConfig.CFG.GENERAL_VERBOSE_CONFIG_LOADING, true),
        PREVENT_WITHOUT_TOOL(7.2014f, MainConfig.CFG.GENERAL_PREVENT_WITHOUT_TOOL, false)
        ;

        private final float version;
        private final MainConfig.CFG node;
        private final Object value;

        /**
         * An adding definition
         *
         * @param version the version it was introduced
         * @param node the node
         * @param value the value
         */
        Adding(float version, MainConfig.CFG node, Object value) {
            this.version = version;
            this.node = node;
            this.value = value;
        }
    }

    /**
     * Check a config for necessary changes
     *
     * @param config the config to check for outdated values
     * @return whether we found a change and thus need to save and reload
     */
    public static boolean check(MainConfig instance, FileConfiguration config) {
        instance.preLoad();
        double version = config.getDouble("Version", 7.0);
        double newVersion = version;
        boolean changed = false;
        for (Moving m : Moving.values()) {
            if (m.version > version) {
                newVersion = Math.max(newVersion, m.version);
                config.set(m.destination, config.get(m.source));
                config.set(m.source, null);
                TreeAssist.instance.getLogger().warning("Config node moved: " + m.toString());
                changed = true;
            }
        }
        for (Adding m : Adding.values()) {
            if (m.version > version) {
                newVersion = Math.max(newVersion, m.version);
                config.set(m.node.getNode(), m.value);
                TreeAssist.instance.getLogger().warning("Config node added: " + m.toString());
                changed = true;
            }
        }
        for (TreeRemoval m : TreeRemoval.values()) {
            if (m.version > version) {
                newVersion = Math.max(newVersion, m.version);

                File trees = new File(TreeAssist.instance.getDataFolder(), "trees");
                File subTree = new File(trees, m.path);
                File configFile = new File(subTree, m.file);

                if (configFile.exists()) {

                    configFile.delete();
                    TreeAssist.instance.getLogger().info("Config deleted: " + m.toString());

                }

                changed = true;
            }
        }
        for (TreeAddition m : TreeAddition.values()) {
            if (m.version > version) {
                newVersion = Math.max(newVersion, m.version);

                File trees = new File(TreeAssist.instance.getDataFolder(), "trees");
                File subTree = new File(trees, m.path);
                File configFile = new File(subTree, m.file);

                if (!configFile.exists()) {

                    TreeAssist.instance.saveResource("trees/" + m.path + "/" + m.file, false);

                    configFile.delete();
                    TreeAssist.instance.getLogger().info("Config created: " + m.toString());

                }

                changed = true;
            }
        }
        config.set("Version", newVersion);
        return changed;
    }
}
