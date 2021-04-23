package net.slipcor.treeassist.core;

import com.google.gson.*;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.configs.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.List;

public class Updater extends Thread {
    private final UpdateMode mode;
    private final UpdateType type;

    private final Plugin plugin;
    private final File file;

    private final int major;
    private final int minor;

    private UpdateInstance instance = null;

    protected enum UpdateMode {
        OFF, ANNOUNCE, DOWNLOAD, BOTH;

        public static UpdateMode getBySetting(final String setting) {
            final String lcSetting = setting.toLowerCase();
            if (lcSetting.contains("ann")) {
                return ANNOUNCE;
            }
            if (lcSetting.contains("down") || lcSetting.contains("load")) {
                return DOWNLOAD;
            }
            if ("both".equals(lcSetting)) {
                return BOTH;
            }
            return OFF;
        }
    }

    protected enum UpdateType {
        ALPHA, BETA, RELEASE;

        public static UpdateType getBySetting(final String setting) {
            if ("beta".equalsIgnoreCase(setting)) {
                return BETA;
            }
            if ("alpha".equalsIgnoreCase(setting)) {
                return ALPHA;
            }
            return RELEASE;
        }
    }

    public Updater(final TreeAssist plugin, final File file) {
        super();

        String version = Bukkit.getServer().getBukkitVersion();

        String[] chunks;
        try {
            chunks = version.split("-")[0].split("\\.");
        } catch (Exception e) {
            chunks = new String[]{"1", "11"};
        }
        int a, b;
        try {
            a = Integer.parseInt(chunks[0]);
        } catch (Exception e) {
            a = 1;
        }
        major = a;
        try {
            b = Integer.parseInt(chunks[1]);
        } catch (Exception e) {
            b = 9;
        }
        minor = b;

        this.plugin = plugin;
        this.file = file;

        mode = UpdateMode.getBySetting(plugin.getMainConfig().getString(MainConfig.CFG.UPDATE_MODE));

        if (mode == UpdateMode.OFF) {
            type = UpdateType.RELEASE;

            TreeAssist.instance.getLogger().info("Updates deactivated. Please check spigotmc.org for updates");

        } else {
            type = UpdateType.getBySetting(plugin.getMainConfig().getString(MainConfig.CFG.UPDATE_TYPE));
            instance = new UpdateInstance("treeassist");

            TreeAssist.instance.getLogger().info("Checking for updates...");

            this.start();
        }
    }

    class UpdateInstance {
        public boolean outdated = false;
        private byte updateDigit;
        private String vOnline;
        private String vThis;
        private final String pluginName;
        private String url;

        UpdateInstance(String checkName) {
            pluginName = checkName;
        }

        /**
         * colorize a given string based on the updated digit
         *
         * @param string the string to colorize
         * @return a colorized string
         */
        private String colorize(final String string) {
            final StringBuffer result;
            if (updateDigit == 0) {
                // first digit means major update
                result = new StringBuffer(ChatColor.RED.toString());
            } else if (updateDigit == 1) {
                // second digit means minor update
                result = new StringBuffer(ChatColor.GOLD.toString());
            } else if (updateDigit == 2) {
                // third digit means a small patch or feature
                result = new StringBuffer(ChatColor.YELLOW.toString());
            } else if (updateDigit == 3) {
                // is that even used? what is blue anyway?
                result = new StringBuffer(ChatColor.BLUE.toString());
            } else {
                result = new StringBuffer(ChatColor.GREEN.toString());
            }
            result.append(string);
            result.append(ChatColor.WHITE);
            return result.toString();
        }

        /**
         * @return the upgrade type, colorized by volatility
         */
        private String colorizeUpgradeType() {
            StringBuffer result;

            switch (type) {
                case ALPHA:
                    result = new StringBuffer(ChatColor.RED.toString());
                    break;
                case BETA:
                    result = new StringBuffer(ChatColor.YELLOW.toString());
                    break;
                case RELEASE:
                    result = new StringBuffer(ChatColor.GREEN.toString());
                    break;
                default:
                    result = new StringBuffer(ChatColor.BLUE.toString());
            }

            result.append(String.valueOf(type).toLowerCase());
            result.append(ChatColor.RESET);

            return result.toString();
        }

        public void runMe() {

            try {
                String version = "";

                vThis = plugin.getDescription().getVersion().replace("v", "");

                outdated = false;

                URL website = new URL(String.format(
                        "http://pa.slipcor.net/versioncheck.php?plugin=%s&type=%s&major=%d&minor=%d&version=%s",
                        pluginName, type.toString().toLowerCase(), major, minor, vThis));
                URLConnection connection = website.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                StringBuffer buffer = new StringBuffer();
                String inputLine = in.readLine();

                while (inputLine != null) {
                    buffer.append(inputLine);
                    inputLine = in.readLine();
                }
                in.close();

                url = "https://www.spigotmc.org/resources/treeassist.67436/";

                try {
                    JsonElement element = new JsonParser().parse(buffer.toString());

                    if (element.isJsonObject()) {
                        JsonObject object = element.getAsJsonObject();
                        if (object.has("update") && object.get("update").isJsonPrimitive()) {
                            JsonPrimitive rawElement = object.getAsJsonPrimitive("update");
                            outdated = rawElement.getAsBoolean();
                        }
                        if (outdated &&
                                object.has("version") &&
                                object.has("link") &&
                                object.has("digit")) {
                            version = object.getAsJsonPrimitive("version").getAsString();
                            updateDigit = object.getAsJsonPrimitive("digit").getAsByte();
                            url = object.getAsJsonPrimitive("link").getAsString();
                        } else {
                            return;
                        }
                    }
                } catch (JsonSyntaxException e) {
                    // something is wrong here. let's just assume everything is up to date
                    version = vThis;
                }

                vOnline = version.replace("v", "");

                message(Bukkit.getConsoleSender(), this);

            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void message(final CommandSender sender, UpdateInstance instance) {
        try {
            if (instance.outdated) {
                boolean error = false;
                if (!(sender instanceof Player) && mode != UpdateMode.ANNOUNCE) {
                    // not only announce, download!
                    final File updateFolder = Bukkit.getServer().getUpdateFolderFile();
                    if (!updateFolder.exists()) {
                        updateFolder.mkdirs();
                    }
                    final File pluginFile = new File(updateFolder, file.getName());
                    if (pluginFile.exists()) {
                        pluginFile.delete();
                    }

                    try {

                        final URL url = new URL(instance.url);
                        final ReadableByteChannel rbc = Channels.newChannel(url.openStream());
                        final FileOutputStream output = new FileOutputStream(pluginFile);
                        output.getChannel().transferFrom(rbc, 0, 1 << 24);
                        output.close();

                    } catch (IOException exception) {
                        error = true;
                    }

                }

                if ((mode != UpdateMode.DOWNLOAD || error) || (!(sender instanceof Player))) {
                    TreeAssist.instance.sendPrefixed(sender,
                            String.format("You are using %s, an outdated version! Latest %s build: %sv%s",
                                    instance.colorize('v' + instance.vThis),
                                    instance.colorizeUpgradeType(),
                                    ChatColor.GREEN,
                                    instance.vOnline));
                }

                if (mode == UpdateMode.ANNOUNCE) {
                    TreeAssist.instance.sendPrefixed(sender, instance.url);
                } else {
                    boolean finalError = error;
                    class RunLater implements Runnable {
                        @Override
                        public void run() {
                            if (finalError) {
                                TreeAssist.instance.sendPrefixed(sender, "The plugin could not updated, download the new version here: " + instance.url);
                            } else {
                                TreeAssist.instance.sendPrefixed(sender, "The plugin has been updated, please restart the server!");
                            }
                        }
                    }
                    Bukkit.getScheduler().runTaskLater(TreeAssist.instance, new RunLater(), 60L);
                }
            } else {
                if (mode != UpdateMode.DOWNLOAD || (!(sender instanceof Player))) {
                    TreeAssist.instance.sendPrefixed(sender, "You are using " + instance.colorize('v' + instance.vThis)
                            + ", an experimental version! Latest stable: " + ChatColor.COLOR_CHAR + 'a' + 'v'
                            + instance.vOnline);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * message a player if the version is different
     *
     * @param player the player to message
     */
    public void message(final CommandSender player) {
        class DownloadLater implements Runnable {

            @Override
            public void run() {
                if (instance != null) {
                    message(player, instance);
                }
            }
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new DownloadLater());
    }

    @Override
    public void run() {

        if (mode == null || mode == UpdateMode.OFF) {
            TreeAssist.instance.getLogger().info("Updates deactivated. Please check spigotmc.org for updates");
            return;
        }

        if (instance != null) {
            instance.runMe();
        }
    }
}