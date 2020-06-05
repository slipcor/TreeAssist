package net.slipcor.treeassist.runnables;

import net.slipcor.treeassist.TreeAssist;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;


/**
 * A manager for Runnables that will prevent saplings from growing
 */
public class TreeAssistAntiGrow {
    final Map<String, Integer> blocks = new HashMap<>();
    private boolean lock = false;

    class AntiGrowRunner extends BukkitRunnable {

        @Override
        public void run() {

            final Map<String, Integer> temp = new HashMap<>();

            for (Map.Entry<String, Integer> entry : blocks.entrySet()) {
                temp.put(entry.getKey(), entry.getValue() - 1);
            }

            try {
                lock = true;
                for (Map.Entry<String, Integer> entry : temp.entrySet()) {
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

    /**
     * Add a block to the lists to be held back
     *
     * @param block   the block to add
     * @param seconds the seconds to hold it
     */
    public void add(final Block block, final int seconds) {
        if (block == null) {
            return;
        }

        if (blocks.size() < 1) {
            // empty, refill!
            blocks.put(locToString(block.getLocation()), seconds);
            new AntiGrowRunner().runTaskTimer(TreeAssist.instance, 20L, 20L);
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

    /**
     * Do we prevent this block currently?
     *
     * @param location the block location to check
     * @return whether we do prevent it
     */
    public boolean contains(final Location location) {
        if (location == null) {
            return false;
        }
        return blocks.containsKey(locToString(location));
    }

    /**
     * Small helper function to make Locations easily comparable
     *
     * @param loc the location to read
     * @return the identifying String
     */
    private String locToString(final Location loc) {
        return loc.getWorld() + ":" + loc.getBlockX() + "/" + loc.getBlockY() + "/" + loc.getBlockZ();
    }
}
