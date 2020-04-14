package me.itsatacoshop247.TreeAssist;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;


public class TreeAssistAntiGrow {
    final Map<String, Integer> blocks = new HashMap<String, Integer>();
    private boolean lock = false;
    private final TreeAssist plugin;

    public TreeAssistAntiGrow(final TreeAssist plugin) {
        this.plugin = plugin;
    }

    class AntiGrowRunner extends BukkitRunnable {

        @Override
        public void run() {

            final Map<String, Integer> temp = new HashMap<String, Integer>();

            for (Map.Entry<String, Integer> entry : blocks.entrySet()) {
                temp.put(entry.getKey(), entry.getValue() - 1);
            }

            try {
                lock = true;
                for (Map.Entry<String, Integer> entry : blocks.entrySet()) {
                    if (entry.getValue() < 1) {
                        blocks.remove(entry.getKey());
                    } else {
                        blocks.put(entry.getKey(), entry.getValue());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                blocks.clear();
            } finally {
                lock = false;
            }

            if (blocks.size() < 1) {
                this.cancel();
            }
        }
    }

    public void add(final Block block, final int seconds) {
        if (block == null) {
            return;
        }

        if (blocks.size() < 1) {
            // empty, refill!
            blocks.put(locToString(block.getLocation()), seconds);
            new AntiGrowRunner().runTaskTimer(plugin, 20L, 20L);
        } else {
            // add
            while (lock) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
            blocks.put(locToString(block.getLocation()), seconds);
        }
    }

    public boolean contains(final Location location) {
        if (location == null) {
            return false;
        }
        return blocks.containsKey(locToString(location));
    }

    private String locToString(final Location loc) {
        return loc.getWorld() + ":" + loc.getBlockX() + "/" + loc.getBlockY() + "/" + loc.getBlockZ();
    }
}
