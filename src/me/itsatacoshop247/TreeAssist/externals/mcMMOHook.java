package me.itsatacoshop247.TreeAssist.externals;

import com.gmail.nossr50.api.AbilityAPI;
import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.skills.SkillType;
import me.itsatacoshop247.TreeAssist.core.Utils;
import me.itsatacoshop247.TreeAssist.trees.AbstractGenericTree;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Tree;
import org.bukkit.plugin.Plugin;

public class mcMMOHook {

    /**
     * Add mcMMO exp for destroying a block
     *
     * @param player
     *            the player to give exp
     * @param block
     *            the block being destroyed
     */
    public static void mcMMOaddExp(Player player, Block block) {
        Plugin mcmmo = Utils.plugin.getServer().getPluginManager().getPlugin("mcMMO");

        if (player == null) {
            AbstractGenericTree.debug.i("no Player!!");
            return;
        }

        MaterialData state = block.getState().getData();

        if (!(state instanceof Tree)) {
            AbstractGenericTree.debug.i("no Tree!!");
            return;
        }

        int toAdd = ExperienceConfig.getInstance().getXp(SkillType.WOODCUTTING, state);
        if (player.isOnline()) {
            AbstractGenericTree.debug.i("adding " + toAdd + " EXP!");
            ExperienceAPI.addXP(player, "Woodcutting", toAdd);
        } else {
            AbstractGenericTree.debug.i("adding " + toAdd + " offline EXP!");
            ExperienceAPI.addRawXPOffline(player.getName(), "Woodcutting", mcmmo.getConfig()
                    .getInt("Experience.Woodcutting.Dark_Oak"));
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
        boolean isMcMMOEnabled = Utils.plugin.getServer().getPluginManager()
                .isPluginEnabled("mcMMO");

        if (!isMcMMOEnabled) {
            return false;
        }

        return AbilityAPI.treeFellerEnabled(player);
    }
}
