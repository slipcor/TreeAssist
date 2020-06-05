package net.slipcor.treeassist.commands;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.configs.MainConfig;
import net.slipcor.treeassist.core.Language;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class CommandReload extends AbstractCommand {
    public CommandReload() {
        super(new String[]{"treeassist.reload"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.parse(Language.MSG.ERROR_PERMISSION_RELOAD));
            return;
        }
        TreeAssist.instance.blockList.save(true);
        TreeAssist.instance.getMainConfig().load();
        TreeAssist.instance.reloadLists();
        Language.init(TreeAssist.instance, TreeAssist.instance.getMainConfig().getString(MainConfig.CFG.GENERAL_LANGUAGE, "lang_en"));
        sender.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_RELOAD));
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
}
