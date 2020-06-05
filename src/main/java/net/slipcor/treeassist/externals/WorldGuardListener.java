package net.slipcor.treeassist.externals;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.util.Location;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.events.TASaplingReplaceEvent;
import net.slipcor.treeassist.events.TATreeBrokenEvent;
import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class WorldGuardListener implements Listener {
    StateFlag replantFlag;
    StateFlag autoChopFlag;

    public WorldGuardListener() {
        replantFlag = tryRegister("treeassist-replant");
        autoChopFlag = tryRegister("treeassist-autochop");
    }

    /**
     * Attempt to register a custom WorldGuard flag
     *
     * @param name the name of the flag
     * @return a resulting state flag
     */
    private StateFlag tryRegister(String name) {
        try {
            FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
            StateFlag myFlag = new StateFlag(name, true);
            registry.register(myFlag);
            TreeAssist.instance.getLogger().info("WorldGuard flag added: '" + name + "'");
            return myFlag;
        } catch (FlagConflictException e) {
            TreeAssist.instance.getLogger().severe("Flag '" + name + "' is already registered in WorldGuard!");
        }
        return null;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTreeBreak(TATreeBrokenEvent event) {
        cancelIfProtected(event, event.getBlock(), autoChopFlag);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onSaplingReplace(TASaplingReplaceEvent event) {
        cancelIfProtected(event, event.getBlock(), replantFlag);
    }

    /**
     * Cancel the event in case we should protect it based on WorldGuard's input
     *
     * @param event the event to cancel
     * @param block the block to check
     * @param flag  the flag to check
     */
    private void cancelIfProtected(Cancellable event, Block block, StateFlag flag) {
        if (flag == null) {
            return;
        }
        Location loc = BukkitAdapter.adapt(block.getLocation());
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        if (!query.testState(loc, null, flag)) {
            event.setCancelled(true);
        }
    }
}
