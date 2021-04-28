package net.slipcor.treeassist.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.utils.MaterialUtils;
import net.slipcor.treeassist.yml.Language;
import net.slipcor.treeassist.yml.MainConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandForceBreak extends CoreCommand {
    public CommandForceBreak(CorePlugin plugin) {
        super(plugin, "treeassist.forcebreak", Language.MSG.ERROR_INVALID_ARGUMENT_COUNT);
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_PERMISSION_FORCEBREAK.parse());
            return;
        }
        if (sender instanceof Player) {
            final Player player = (Player) sender;

            int radius = TreeAssist.instance.config().getInt(MainConfig.CFG.COMMANDS_FORCE_BREAK_DEFAULT_RADIUS, 10);

            if (args.length > 1) {
                try {
                    radius = Math.max(1, Integer.parseInt(args[1]));
                    int configValue = TreeAssist.instance.config().getInt(MainConfig.CFG.COMMANDS_FORCE_BREAK_MAX_RADIUS, 30);
                    if (radius > configValue) {
                        TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_OUT_OF_RANGE.parse(String.valueOf(configValue)));
                        return;
                    }
                } catch (Exception e) {
                }
            }

            TreeAssist.instance.setCoolDownOverride(player.getName(), true);

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Block b = player.getLocation().add(x, y, z).getBlock();
                        if (MaterialUtils.isLog(b.getType())) {
                            if (b.getRelative(BlockFace.DOWN).getType() == Material.DIRT ||
                                    b.getRelative(BlockFace.DOWN).getType() == Material.GRASS_BLOCK ||
                                    b.getRelative(BlockFace.DOWN).getType() == Material.SAND ||
                                    b.getRelative(BlockFace.DOWN).getType() == Material.PODZOL) {
                                BlockBreakEvent bbe = new BlockBreakEvent(b, player);
                                TreeAssist.instance.getServer().getPluginManager().callEvent(bbe);
                            }
                        }
                    }
                }
            }

            Bukkit.getScheduler().runTaskLater(TreeAssist.instance, () -> TreeAssist.instance.setCoolDownOverride(player.getName(), false), 200);

            return;
        }
        TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_ONLY_PLAYERS.parse());
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("forcebreak");
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
    public List<String> completeTab(String[] strings) {
        return new ArrayList<>(); // we have no arguments
    }
}
