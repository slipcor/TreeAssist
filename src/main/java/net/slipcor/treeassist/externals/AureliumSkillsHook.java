package net.slipcor.treeassist.externals;

import com.archyx.aureliumskills.api.AureliumAPI;
import com.archyx.aureliumskills.skills.Skills;
import org.bukkit.entity.Player;

/**
 * Hook into AureliumSkills, giving EXP
 */
public class AureliumSkillsHook {

    /**
     * Add Aurelium Skills EXP
     *
     * @param player the player who should gain the EXP
     * @param value  the EXP amount to give
     */
    public static void addAureliumExp(Player player, Double value) {
        AureliumAPI.addXp(player, Skills.FORAGING, value);
    }
}
