package net.slipcor.treeassist.listeners;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.yml.Language;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

/**
 * Hook class to hook into the Placeholder API
 */
public class PlaceholderAPIListener extends PlaceholderExpansion {
    long lastError = 0;

    @Override
    public String getIdentifier() {
        return "ta";
    }

    @Override
    public String getAuthor() {
        return "SLiPCoR";
    }

    @Override
    public String getVersion() {
        return "0.0.1";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, String s) {
        if (s.equals("global")) {
            // toggle status (global)
            return TreeAssist.instance.isDisabled("global", player.getName()) ? Language.MSG.INFO_VALUE_OFF.parse() : Language.MSG.INFO_VALUE_ON.parse();
        }

        if (s.equals("world") && player instanceof Player) {
            // toggle status (world)
            return TreeAssist.instance.isDisabled(((Player) player).getWorld().getName(), player.getName()) ? Language.MSG.INFO_VALUE_OFF.parse() : Language.MSG.INFO_VALUE_ON.parse();
        }

        if (s.equals("cooldown") && player instanceof Player) {
            // cooldown status
            return String.valueOf(TreeAssist.instance.getCoolDown((Player) player));
        }

        return null;
    }
}
