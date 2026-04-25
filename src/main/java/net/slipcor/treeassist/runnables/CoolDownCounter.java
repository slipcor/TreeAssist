package net.slipcor.treeassist.runnables;

import com.tcoded.folialib.wrapper.task.WrappedTask;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.yml.Language;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

public class CoolDownCounter implements Consumer<WrappedTask> {
    private final String name;
    private int seconds;
    private WrappedTask wrappedTask;

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
    public void accept(WrappedTask task) {
        this.wrappedTask = task;
        if (--seconds <= 0) {
            commit();
            try {
                task.cancel();
            } catch (IllegalStateException e) {
            }
        }
    }

    public void cancel() {
        if (wrappedTask != null) {
            wrappedTask.cancel();
        }
    }

    private void commit() {

        TreeAssist.instance.removeCountDown(name);
        Player player = Bukkit.getPlayer(name);
        if (player != null) {
            TreeAssist.instance.sendPrefixed(player, Language.MSG.INFO_COOLDOWN_DONE.parse());
        }
    }

    public int getSeconds() {
        return seconds;
    }

}
