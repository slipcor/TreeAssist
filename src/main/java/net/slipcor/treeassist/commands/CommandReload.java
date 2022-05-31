package net.slipcor.treeassist.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.utils.BlockUtils;
import net.slipcor.treeassist.yml.Language;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandReload extends CoreCommand {
    public CommandReload(CorePlugin plugin) {
        super(plugin, "treeassist.reload", Language.MSG.ERROR_INVALID_ARGUMENT_COUNT);
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_PERMISSION_RELOAD.parse());
            return;
        }
        TreeAssist.instance.blockList.save(true);
        TreeAssist.instance.config().load();
        TreeAssist.instance.reloadLists();
        TreeAssist.instance.loadCommands();
        BlockUtils.useFallingBlock = null; // reset this value to allow re-loading
        String error = TreeAssist.instance.loadLanguage();
        if (error != null) {
            TreeAssist.instance.sendPrefixed(sender, ChatColor.RED + error);
            return;
        }
        TreeAssist.instance.loadToggles();
        TreeAssist.instance.sendPrefixed(sender, Language.MSG.SUCCESSFUL_RELOAD.parse());
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("reload");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!rl");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist reload - reload the plugin";
    }

    @Override
    public List<String> completeTab(String[] strings) {
        return new ArrayList<>(); // we have no arguments
    }
}
