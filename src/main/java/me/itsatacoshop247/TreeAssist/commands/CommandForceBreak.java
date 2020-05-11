package me.itsatacoshop247.TreeAssist.commands;

import me.itsatacoshop247.TreeAssist.core.Config;
import me.itsatacoshop247.TreeAssist.core.Language;
import me.itsatacoshop247.TreeAssist.core.Utils;
import net.royawesome.jlibnoise.module.combiner.Max;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Collections;
import java.util.List;

public class CommandForceBreak extends AbstractCommand {
    public CommandForceBreak() {
        super(new String[]{"treeassist.forcebreak"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.parse(Language.MSG.ERROR_PERMISSION_FORCEBREAK));
            return;
        }
        if (sender instanceof Player) {
            final Player player = (Player) sender;

            int radius = Utils.plugin.getTreeAssistConfig().getInt(Config.CFG.MAIN_FORCE_BREAK_DEFAULT_RADIUS, 10);

            if (args.length > 1) {
                try {
                    radius = Math.max(1, Integer.parseInt(args[1]));
                    int configValue = Utils.plugin.getTreeAssistConfig().getInt(Config.CFG.MAIN_FORCE_BREAK_MAX_RADIUS, 30);
                    if (radius > configValue) {
                        sender.sendMessage(Language.parse(Language.MSG.ERROR_OUT_OF_RANGE, String.valueOf(configValue)));
                    }
                } catch (Exception e) {
                }
            }

            Utils.plugin.setCoolDownOverride(player.getName(), true);

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    nextBlock:
                    for (int z = -radius; z <= radius; z++) {
                        Block b = player.getLocation().add(x, y, z).getBlock();
                        if (Utils.isLog(b.getType())) {
                            if (b.getRelative(BlockFace.DOWN).getType() == Material.DIRT ||
                                    b.getRelative(BlockFace.DOWN).getType() == Material.GRASS_BLOCK ||
                                    b.getRelative(BlockFace.DOWN).getType() == Material.SAND ||
                                    b.getRelative(BlockFace.DOWN).getType() == Material.PODZOL) {
                                BlockBreakEvent bbe = new BlockBreakEvent(b, player);
                                Utils.plugin.getServer().getPluginManager().callEvent(bbe);
                            }
                        }
                    }
                }
            }

            Bukkit.getScheduler().runTaskLater(Utils.plugin, new Runnable() {
                @Override
                public void run() {
                    Utils.plugin.setCoolDownOverride(player.getName(), false);
                }
            }, Math.min(10, Utils.plugin.getTreeAssistConfig().getInt(Config.CFG.AUTOMATIC_TREE_DESTRUCTION_INITIAL_DELAY_TIME, 10) + 10) * 20);

            return;
        }
        sender.sendMessage(Language.parse(Language.MSG.ERROR_ONLY_PLAYERS));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("forcebreak");
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!fb");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist forcebreak - force break trees around you";
    }

    @Override
    public CommandTree<String> getSubs() {
        return new CommandTree<>(null);
    }
}
