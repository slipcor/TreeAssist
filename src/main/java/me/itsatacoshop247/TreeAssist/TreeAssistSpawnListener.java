package me.itsatacoshop247.TreeAssist;

import java.util.Random;

import me.itsatacoshop247.TreeAssist.core.Config;
import me.itsatacoshop247.TreeAssist.core.Debugger;
import me.itsatacoshop247.TreeAssist.core.TreeConfig;
import me.itsatacoshop247.TreeAssist.events.TASaplingReplaceEvent;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import me.itsatacoshop247.TreeAssist.core.Utils;

public class TreeAssistSpawnListener implements Listener {

	public TreeAssist plugin;
	public static Debugger debug;
	
	public TreeAssistSpawnListener(TreeAssist instance)
	{
		plugin = instance;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void itemSpawnEvent(ItemSpawnEvent event) {
		Item drop = event.getEntity();
		TreeConfig config = Utils.findConfigByDroppedSapling(drop.getItemStack().getType());
		if (config != null) {
			TASaplingReplaceEvent newEvent = new TASaplingReplaceEvent(
					event.getEntity().getLocation().getBlock(),
					drop.getItemStack().getType().name());
			Utils.plugin.getServer().getPluginManager().callEvent(newEvent);
			if (newEvent.isCancelled()) {
				debug.i("TreeAssistSpawnListener.itemSpawnEvent() Sapling Replant was cancelled!");
				return;
			}

			if ((new Random()).nextInt(100) <
					config.getInt(TreeConfig.CFG.REPLANTING_DROPPED_SAPLINGS_CHANCE)) {
				new TreeAssistSaplingSelfPlant(plugin, config, drop);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void playerPickupItemEvent(PlayerPickupItemEvent event) {
		Item item = event.getItem();
		if (Utils.isSapling(item.getItemStack().getType())) {
			TreeAssistSaplingSelfPlant.remove(item);
		}
	}
}
