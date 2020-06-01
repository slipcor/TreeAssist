package me.itsatacoshop247.TreeAssist.core;

import org.bukkit.configuration.file.FileConfiguration;

public class ConfigUpdater {
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

    public static boolean check(FileConfiguration config) {
        double version = config.getDouble("Version", 6.0);
        double newVersion = version;
        boolean changed = false;
        for (Moving m : Moving.values()) {
            if (m.version > version) {
                newVersion = Math.max(newVersion, m.version);
                config.set(m.destination, config.get(m.source));
                config.set(m.source, null);
                Utils.plugin.getLogger().warning("Config node moved: " + m.toString());
                changed = true;
            }
        }
        config.set("Version", newVersion);
        return changed;
    }
}
