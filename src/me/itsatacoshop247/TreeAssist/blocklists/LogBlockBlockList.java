package me.itsatacoshop247.TreeAssist.blocklists;

import de.diddiz.LogBlock.BlockChange;
import de.diddiz.LogBlock.Consumer;
import de.diddiz.LogBlock.LogBlock;
import de.diddiz.LogBlock.QueryParams;
import de.diddiz.LogBlock.QueryParams.BlockChangeType;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class LogBlockBlockList implements BlockList {
	private final LogBlock logBlock;
	private Consumer lbconsumer = null;
	
	public LogBlockBlockList() {
		if (Bukkit.getPluginManager().isPluginEnabled("LogBlock")) {
			logBlock = (LogBlock) Bukkit.getPluginManager().getPlugin("LogBlock");
		} else {
			logBlock = null;
		}
	}

	@Override
	public void initiate() {
		if (logBlock == null) {
			Utils.plugin.getLogger().warning("LogBlock selected as BlockList, but not enabled!");
		}
	}

	@Override
	public boolean isPlayerPlaced(Block block) {
		if (logBlock == null) {
			return false;
		}
		QueryParams params = new QueryParams(logBlock);
		params.bct = BlockChangeType.ALL;
		params.limit = 1;
		params.loc = block.getLocation();
		params.needType = true;
		params.world = block.getWorld();

		try {
		    for (BlockChange bc : logBlock.getBlockChanges(params)) {
		        if (bc.type == BlockChangeType.CREATED.ordinal()) {
		        	return true;
		        }
		    }
		} catch (SQLException ex) {
		    // Do nothing or throw an error if you want
		}
		return false;
	}

	@Override
	public void addBlock(Block block) {
		// prism does that
	}

	@Override
	public void removeBlock(Block block) {
	}

	@Override
	public void save() {
	}

	@Override
	public void save(boolean force) {
	}

	@Override
	public void logBreak(Block block, Player player) {
		if (logBlock == null) {
			return;
		}
		
		if (lbconsumer == null) {
			lbconsumer = logBlock.getConsumer();
		}
		
		lbconsumer.queueBlockBreak(player == null ? "TreeAssist" : player.getName(),
				block.getState());
	}

}
