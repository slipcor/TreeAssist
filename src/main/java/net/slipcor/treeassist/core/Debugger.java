package net.slipcor.treeassist.core;

import net.slipcor.treeassist.TreeAssist;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import java.util.logging.Formatter;

public class Debugger {
    public static boolean override = false;

    private static Logger logger = null;
    private static List<Logger> loggers = new ArrayList<>();

    private final TreeAssist plugin;
    private final int debugID;

    private static Set<Integer> check = new HashSet<>();

    public Debugger(TreeAssist plugin, int id) {
        debugID = id;
        this.plugin = plugin;
    }

    private boolean debugs() {
        return override || check.contains(debugID);
    }

    private Logger getLogger() {

        if (logger == null) {
            logger = Logger.getAnonymousLogger();
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(false);

            for (Handler handler : logger.getHandlers()) {
                logger.removeHandler(handler);
                handler.close();
            }

            try {
                final SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");

                final File debugFolder = new File(plugin.getDataFolder(), "debug");
                debugFolder.mkdirs();
                final File logFile = new File(debugFolder, dateformat.format(new Date()) + ".log");

                final FileHandler handler = new FileHandler(logFile.getAbsolutePath());

                handler.setFormatter(LogFileFormatter.newInstance());

                logger.addHandler(handler);

                loggers.add(logger);
            } catch (IOException | SecurityException ex) {
                plugin.getLogger().log(Level.SEVERE, null, ex);
            }
        }
        return logger;
    }

    static class LogFileFormatter extends Formatter {

        private final SimpleDateFormat date;

        public static LogFileFormatter newInstance() {
            return new LogFileFormatter();
        }

        private LogFileFormatter() {
            super();
            this.date = new SimpleDateFormat("yy.MM.dd HH:mm:ss");
        }

        public String format(final LogRecord record) {
            final StringBuilder builder = new StringBuilder();
            final Throwable exception = record.getThrown();

            builder.append(this.date.format(record.getMillis()));
            builder.append(" [");
            builder.append(record.getLevel().getLocalizedName().toUpperCase());
            builder.append("] ");
            builder.append(record.getMessage());
            builder.append('\n');

            if (exception != null) {
                final StringWriter writer = new StringWriter();
                exception.printStackTrace(new PrintWriter(writer));
                builder.append(writer);
            }

            return builder.toString();
        }
    }

    public void i(final String string) {
        if (!debugs()) {
            return;
        }
        getLogger().info(System.currentTimeMillis() % 1000 + " " + string);
        //TreeAssist.instance.getLogger().info(string);
    }

    public static void load(final TreeAssist instance, final CommandSender sender) {
        check.clear();
        override = false;
        final String debugs = instance.getConfig().getString("Debug", "none");
        if (debugs.equals("none")) {
            instance.getLogger().info("debugging: off");
        } else {
            if (debugs.equals("all") || debugs.equals("full") || debugs.equals("on")) {
                override = true;
                sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_DEBUG_ALL));
            } else {
                final String[] sIds = debugs.split(",");
                sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_DEBUG_X, debugs));
                for (String s : sIds) {
                    try {
                        check.add(Integer.valueOf(s));
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }
    }

    public static void destroy() {
        for (Logger log : loggers) {
            Handler[] handlers = log.getHandlers().clone();
            for (Handler hand : handlers) {
                log.removeHandler(hand);
                hand.close();
            }
        }
        loggers.clear();
    }

    public static String parse(Location location) {
        StringBuilder sb = new StringBuilder(location.getWorld().getName());
        sb.append(':');
        sb.append(location.getBlockX());
        sb.append('/');
        sb.append(location.getBlockY());
        sb.append('/');
        sb.append(location.getBlockZ());
        return sb.toString();
    }
}
