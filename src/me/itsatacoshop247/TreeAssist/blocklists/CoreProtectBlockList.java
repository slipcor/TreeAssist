package me.itsatacoshop247.TreeAssist.blocklists;

import me.itsatacoshop247.TreeAssist.core.Utils;
import net.coreprotect.CoreProtect;
import net.coreprotect.CoreProtectAPI;
import net.coreprotect.CoreProtectAPI.ParseResult;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class CoreProtectBlockList extends EmptyBlockList {
	private final CoreProtectAPI protect;
	
	public CoreProtectBlockList() {
		super();
		protect = getCoreProtect();
	}

	private CoreProtectAPI getCoreProtect() {
		Plugin plugin = Bukkit.getPluginManager().getPlugin("CoreProtect");
		     
		// Check that CoreProtect is loaded
		if (plugin == null || !(plugin instanceof CoreProtect)) {
		  return null;
		}
		       
		// Check that the API is enabled
		CoreProtectAPI protect = ((CoreProtect)plugin).getAPI();
        if (!protect.isEnabled()) {
            return null;
		}
		return protect;
	}

	@Override
	public void initiate() {
		if (protect == null) {
			Utils.plugin.getLogger().warning("CoreProtect selected as BlockList, but not enabled!");
		}
	}

	@Override
	public boolean isPlayerPlaced(Block block) {
		return false;

		/*

		temporarily disabled until we find a way to distinguish between manually placing a block and doing it by growing a tree

		if (protect == null || lookupTime <= 0) {
			return false;
		}
		List<String[]> lookup = protect.blockLookup(block, lookupTime);

		// results are newest first
		for (String[] value : lookup) {
			ParseResult result = protect.parseResult(value);

			if (result.getPlayer().equals("#tree")) {
				break;
			}

			if (result.getActionId() == 1 && Utils.isLog(result.getBlockData().getMaterial()) && !result.isRolledBack()) {
				break; // lately placed a log
			}
			if (result.getActionId() == 0) {
				break; // lately removed again
			}
		}
		return false;
		*/
	}

	@Override
	public void logBreak(Block block, Player player) {
		if (protect == null) {
			return;
		}
		protect.logRemoval(player == null ? "TreeAssist" : player.getName(),
				block.getLocation(), block.getType(), block.getBlockData());
	}

}
