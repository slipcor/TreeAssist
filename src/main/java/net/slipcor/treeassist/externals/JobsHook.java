package net.slipcor.treeassist.externals;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.actions.BlockActionInfo;
import com.gamingmesh.jobs.container.ActionType;
import com.gamingmesh.jobs.container.JobsPlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Hook into Jobs jobs being faked by the plugin
 */
public class JobsHook {

    /**
     * Fake perform a Jobs action for a player
     *
     * @param player the player who would have performed the action
     * @param block the block to hand over
     */
    public static void addJobsExp(Player player, Block block) {
        JobsPlayer jPlayer = Jobs.getPlayerManager().getJobsPlayer(player);
        BlockActionInfo bInfo = new BlockActionInfo(block, ActionType.BREAK);
        Jobs.action(jPlayer, bInfo, block);
    }
}
