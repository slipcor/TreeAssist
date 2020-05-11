package me.itsatacoshop247.TreeAssist;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Item;

import me.itsatacoshop247.TreeAssist.core.Utils;

public class TreeAssistSaplingSelfPlant implements Runnable {
	private final TreeAssist plugin;
	private Item drop;
	private final static Set<Item> items = new HashSet<Item>();
	
	public TreeAssistSaplingSelfPlant(TreeAssist instance, Item item)
	{
		this.plugin = instance;
		drop = item;
		items.add(drop);
		
		int delay = plugin.getConfig().getInt("Auto Plant Dropped Saplings.Delay (seconds)", 5);
		if (delay < 1) {
			delay = 1;
		}
		
		Bukkit.getScheduler().runTaskLater(plugin, this, 20L * delay);
	}

	@Override
	public void run() 
	{
		if (!items.contains(drop)) {
			return;
		}
		Block block = drop.getLocation().getBlock();
		
		if ((Utils.isAir(block.getType()) || block.getType() == Material.SNOW) &&
				(block.getRelative(BlockFace.DOWN).getType() == Material.DIRT ||
						block.getRelative(BlockFace.DOWN).getType() == Material.MYCELIUM ||
						block.getRelative(BlockFace.DOWN).getType() == Material.GRASS_BLOCK ||
						block.getRelative(BlockFace.DOWN).getType() == Material.PODZOL)) {

			block.setType(Utils.resolveLegacySapling(drop.getItemStack().getData().getData()));
			drop.remove();
		}
	}
	
	public static void remove(Item item) {
		items.remove(item);
	}
}
