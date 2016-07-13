package me.itsatacoshop247.TreeAssist.blocklists;

import me.botsko.prism.Prism;
import me.botsko.prism.actionlibs.ActionsQuery;
import me.botsko.prism.actionlibs.QueryParameters;
import me.botsko.prism.actionlibs.QueryResult;
import me.botsko.prism.actionlibs.RecordingQueue;
import me.botsko.prism.actions.BlockAction;
import me.botsko.prism.actions.Handler;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public class Prism2BlockList implements BlockList {
	private final Prism prism;
	public Prism2BlockList() {
		if (Bukkit.getPluginManager().isPluginEnabled("Prism")) {
			prism = (Prism) Bukkit.getPluginManager().getPlugin("Prism");
		} else {
			prism = null;
		}
	}

	@Override
	public void initiate() {
		if (prism == null) {
			Utils.plugin.getLogger().warning("Prism selected as BlockList, but not enabled!");
		}
	}

	@Override
	public boolean isPlayerPlaced(Block block) {
		if (prism == null) {
			return false;
		}
		QueryParameters parameters = new QueryParameters();
		parameters.setWorld(block.getWorld().toString());
		parameters.setSpecificBlockLocation(block.getLocation());
		parameters.addActionType("block-break");
		parameters.addActionType("block-burn");
		parameters.addActionType("block-spread");
		parameters.addActionType("block-place");
		parameters.addActionType("entity-break");
		parameters.addActionType("entity-explode");
		
		parameters.setLimit(1); // LOOKUP = Most recent actions first.
		
		ActionsQuery aq = new ActionsQuery(prism);
		QueryResult lookupResult = aq.lookup( parameters );
		if(!lookupResult.getActionResults().isEmpty()){
			List<Handler> results = lookupResult.getActionResults();
			if(results != null){
				for(Handler a : results){
					// An example that prints the player name and the action type.
					// full action details will be available to you here.
					if (a.getType().getShortName().equals("block-break")) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void addBlock(Block block) {
		// prism does that
	}

	@Override
	public void removeBlock(Block block) {
		// prism does that
	}

	@Override
	public void save() {
	}

	@Override
	public void save(boolean force) {
	}

	@Override
	public void logBreak(Block block, Player player) {
		if (prism == null) {
			return;
		}
		BlockAction action = new BlockAction();
		action.setBlock(block);
		action.setActionType("block-break");
		action.setPlayerName(player == null ? "TreeAssist" : player.getName());
		RecordingQueue.addToQueue(action);
	}

}
