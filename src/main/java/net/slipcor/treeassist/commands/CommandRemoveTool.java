package net.slipcor.treeassist.commands;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.core.Language;
import net.slipcor.treeassist.configs.TreeConfig;
import net.slipcor.treeassist.utils.StringUtils;
import net.slipcor.treeassist.utils.ToolUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandRemoveTool extends AbstractCommand {
    public CommandRemoveTool() {
        super(new String[]{"treeassist.removetool"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.parse(Language.MSG.ERROR_PERMISSION_REMOVETOOL));
            return;

        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.DARK_RED + this.getShortInfo());
            return;
        }

        if (sender instanceof Player) {
            Player player = (Player) sender;
            for (TreeConfig config : TreeAssist.treeConfigs.values()) {
                if (StringUtils.matchContains(config.getStringList(TreeConfig.CFG.TRUNK_MATERIALS, new ArrayList<>()), args[1], true)) {
                    ToolUtils.toolRemove(player, config);
                }
            }
            return;
        }
        sender.sendMessage(Language.parse(Language.MSG.ERROR_ONLY_PLAYERS));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("removetool");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!rt");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist removetool {trunk block type} - remove a required tool";
    }
}
