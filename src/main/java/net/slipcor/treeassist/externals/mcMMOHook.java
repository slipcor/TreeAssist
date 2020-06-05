package net.slipcor.treeassist.externals;

import com.gmail.nossr50.api.AbilityAPI;
import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.util.player.UserManager;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.core.TreeStructure;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 * Hook into mcMMO to add Experience for players
 */
public class mcMMOHook {

    /**
     * Add mcMMO exp for destroying a block
     *
     * @param player the player to give exp
     * @param block the block being destroyed
     */
    public static void mcMMOAddExp(Player player, Block block) {

        if (player == null) {
            TreeStructure.debug.i("no Player!!");
            return;
        }

        if (player.isOnline()) {
            TreeStructure.debug.i("adding EXP!");

            ExperienceAPI.addXpFromBlock(block.getState(), UserManager.getPlayer(player));
        } else {
            TreeStructure.debug.i("adding offline EXP!");

            ExperienceAPI.addXpFromBlock(block.getState(), UserManager.getOfflinePlayer(player));
        }
    }
    /**
     * check if a player is using the tree feller ability atm
     *
     * @param player
     *            the player to check
     * @return if a player is using tree feller
     */
    public static boolean mcMMOTreeFeller(Player player) {
        boolean isMcMMOEnabled = TreeAssist.instance.getServer().getPluginManager()
                .isPluginEnabled("mcMMO");

        if (!isMcMMOEnabled) {
            return false;
        }

        return AbilityAPI.treeFellerEnabled(player);
    }
}
