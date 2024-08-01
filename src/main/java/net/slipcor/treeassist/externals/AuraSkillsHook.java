package net.slipcor.treeassist.externals;

import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.skill.Skills;
import org.bukkit.entity.Player;

/**
 * Hook into AureliumSkills, giving EXP
 */
public class AuraSkillsHook {

    /**
     * Add Aurelium Skills EXP
     *
     * @param player the player who should gain the EXP
     * @param value  the EXP amount to give
     */
    public static void addAuraExp(Player player, Double value) {
        AuraSkillsApi.get().getUserManager().getUser(player.getUniqueId()).addSkillXp(Skills.FORAGING, value);
    }
}
