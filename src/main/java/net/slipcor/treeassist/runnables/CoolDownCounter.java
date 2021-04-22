package net.slipcor.treeassist.runnables;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.core.Language;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CoolDownCounter extends BukkitRunnable {
    private final String name;
    private int seconds;

    /**
     * A runnable counting down by the second until a player can use the auto destruction again
     *
     * @param player  the player to handle
     * @param seconds the initial seconds to count from
     */
    public CoolDownCounter(Player player, int seconds) {
        name = player.getName();
        this.seconds = seconds;
    }

    @Override
    public void run() {
        if (--seconds <= 0) {
            commit();
            try {
                this.cancel();
            } catch (IllegalStateException e) {
            }
        }
    }

    private void commit() {

        TreeAssist.instance.removeCountDown(name);
        Player player = Bukkit.getPlayer(name);
        if (player != null) {
            TreeAssist.instance.sendPrefixed(player, Language.parse(Language.MSG.INFO_COOLDOWN_DONE));
        }
    }

    public int getSeconds() {
        return seconds;
    }

}
