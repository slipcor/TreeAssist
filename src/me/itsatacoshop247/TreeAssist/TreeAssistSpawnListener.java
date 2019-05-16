package me.itsatacoshop247.TreeAssist;

import java.util.Random;

import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

import me.itsatacoshop247.TreeAssist.core.Utils;

public class TreeAssistSpawnListener implements Listener {

	public TreeAssist plugin;
	
	public TreeAssistSpawnListener(TreeAssist instance)
	{
		plugin = instance;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void itemSpawnEvent(ItemSpawnEvent event) {
		Item drop = event.getEntity();
		if (Utils.isSapling(drop.getItemStack().getType())) {
			if ((new Random()).nextInt(100) < 
					plugin.getConfig().getInt("Auto Plant Dropped Saplings.Chance (percent)",10)) {
				new TreeAssistSaplingSelfPlant(plugin, drop);
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
