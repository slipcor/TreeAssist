package net.slipcor.treeassist.core;

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

    private final List<UpdateInstance> instances = new ArrayList<>();

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
            instances.clear();
            type = UpdateType.getBySetting(plugin.getMainConfig().getString(MainConfig.CFG.UPDATE_TYPE));
            instances.add(new UpdateInstance("treeassist"));

            TreeAssist.instance.getLogger().info("Checking for updates...");

            this.start();
        }
    }

    class UpdateInstance {
        private byte updateDigit;
        private String vOnline;
        private String vThis;
        private final String pluginName;
        private String url;

        private boolean msg;
        private boolean outdated;

        UpdateInstance(String checkName) {
            pluginName = checkName;
        }

        /**
         * calculate the message variables based on the versions
         */
        private void calculateVersions() {
            final String[] aOnline = vOnline.split("\\.");
            final String[] aThis = vThis.split("-")[0].split("\\.");
            outdated = false;


            for (int i = 0; i < aOnline.length && i < aThis.length; i++) {
                try {
                    final int iOnline = Integer.parseInt(aOnline[i]);
                    final int iThis = Integer.parseInt(aThis[i]);
                    if (iOnline == iThis) {
                        msg = false;
                        continue;
                    }
                    msg = true;
                    outdated = iOnline > iThis;
                    updateDigit = (byte) i;
                    message(Bukkit.getConsoleSender(), this);
                    return;
                } catch (final Exception e) {
                    calculateRadixString(aOnline[i], aThis[i], i);
                    return;
                }
            }
        }

        /**
         * calculate a version part based on letters
         *
         * @param sOnline the online letter(s)
         * @param sThis   the local letter(s)
         */
        private void calculateRadixString(final String sOnline, final String sThis,
                                          final int pos) {
            try {
                final int iOnline = Integer.parseInt(sOnline, 36);
                final int iThis = Integer.parseInt(sThis, 36);
                if (iOnline == iThis) {
                    msg = false;
                    return;
                }
                msg = true;
                outdated = iOnline > iThis;
                updateDigit = (byte) pos;
                message(Bukkit.getConsoleSender(), this);
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * colorize a given string based on a char
         *
         * @param string the string to colorize
         * @return a colorized string
         */
        private String colorize(final String string) {
            final StringBuffer result;
            if (updateDigit == 0) {
                result = new StringBuffer(ChatColor.RED.toString());
            } else if (updateDigit == 1) {
                result = new StringBuffer(ChatColor.GOLD.toString());
            } else if (updateDigit == 2) {
                result = new StringBuffer(ChatColor.YELLOW.toString());
            } else if (updateDigit == 3) {
                result = new StringBuffer(ChatColor.BLUE.toString());
            } else {
                result = new StringBuffer(ChatColor.GREEN.toString());
            }
            result.append(string);
            result.append(ChatColor.WHITE);
            return result.toString();
        }

        public void runMe() {

            try {

                String version = "";

                URL website = new URL("http://pa.slipcor.net/versioncheck.php?plugin=" + pluginName + "&type=" + type.toString().toLowerCase() + "&major=" + major + "&minor=" + minor);
                URLConnection connection = website.openConnection();
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()));
                String inputLine;

                if ((inputLine = in.readLine()) != null) {
                    version = inputLine;
                }
                in.close();
                vOnline = version.replace("v", "");

                url = "https://www.spigotmc.org/resources/treeassist.67436/";

                website = new URL("http://pa.slipcor.net/versioncheck.php?plugin=" + pluginName + "&link=true&type=" + type.toString().toLowerCase() + "&major=" + major + "&minor=" + minor);
                connection = website.openConnection();
                in = new BufferedReader(
                        new InputStreamReader(
                                connection.getInputStream()));

                if ((inputLine = in.readLine()) != null) {
                    url = inputLine;
                }
                in.close();

                vThis = plugin.getDescription().getVersion().replace("v", "");

                calculateVersions();

            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void message(final CommandSender player, UpdateInstance instance) {
        try {
            if (!instance.msg) {
                return;
            }

            if (instance.outdated) {
                boolean error = false;
                if (!(player instanceof Player) && mode != UpdateMode.ANNOUNCE) {
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

                if ((mode != UpdateMode.DOWNLOAD || error) || (!(player instanceof Player))) {
                    player.sendMessage("You are using " + instance.colorize('v' + instance.vThis)
                            + ", an outdated version! Latest: " + ChatColor.COLOR_CHAR + 'a' + 'v' + instance.vOnline);
                }

                if (mode == UpdateMode.ANNOUNCE) {
                    player.sendMessage(instance.url);
                } else {
                    boolean finalError = error;
                    class RunLater implements Runnable {
                        @Override
                        public void run() {
                            if (finalError) {
                                player.sendMessage("The plugin could not updated, download the new version here: https://www.spigotmc.org/resources/pvp-stats.59124/");
                            } else {
                                player.sendMessage("The plugin has been updated, please restart the server!");
                            }
                        }
                    }
                    Bukkit.getScheduler().runTaskLater(TreeAssist.instance, new RunLater(), 60L);
                }
            } else {
                if (mode != UpdateMode.DOWNLOAD || (!(player instanceof Player))) {
                    player.sendMessage("You are using " + instance.colorize('v' + instance.vThis)
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
                for (final UpdateInstance instance : instances) {
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

        TreeAssist.instance.getLogger().info("Checking for updates...");
        for (UpdateInstance instance : instances) {
            instance.runMe();
        }
    }
}