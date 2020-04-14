package me.itsatacoshop247.TreeAssist.externals;

import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.actions.BlockActionInfo;
import com.gamingmesh.jobs.container.ActionType;
import com.gamingmesh.jobs.container.JobsPlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class JobsHook {

    public static void addJobsExp(Player player, Block block) {
        JobsPlayer jPlayer = Jobs.getPlayerManager().getJobsPlayer(player);
        BlockActionInfo bInfo = new BlockActionInfo(block, ActionType.BREAK);
        Jobs.action(jPlayer, bInfo, block);
    }
}
