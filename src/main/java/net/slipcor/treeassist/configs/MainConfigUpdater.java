package net.slipcor.treeassist.configs;

import net.slipcor.treeassist.TreeAssist;
import org.bukkit.configuration.file.FileConfiguration;

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

    /**
     * Check a config for necessary changes
     *
     * @param config the config to check for outdated values
     * @return whether we found a change and thus need to save and reload
     */
    public static boolean check(FileConfiguration config) {
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
        config.set("Version", newVersion);
        return changed;
    }
}
