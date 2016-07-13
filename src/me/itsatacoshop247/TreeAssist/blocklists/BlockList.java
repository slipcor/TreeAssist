package me.itsatacoshop247.TreeAssist.blocklists;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface BlockList {
	void initiate();
	boolean isPlayerPlaced(Block block);
	void addBlock(Block block);
	void removeBlock(Block block);
	void save();

	void save(boolean force);
	void logBreak(Block block, Player player);
}
