package net.slipcor.treeassist.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.utils.MaterialUtils;
import net.slipcor.treeassist.yml.Language;
import net.slipcor.treeassist.yml.MainConfig;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.Sapling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandForceGrow extends CoreCommand {
    public CommandForceGrow(CorePlugin plugin) {
        super(plugin, "treeassist.forcegrow", Language.MSG.ERROR_INVALID_ARGUMENT_COUNT);
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_PERMISSION_FORCEGROW.parse());
            return;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;

            int radius = TreeAssist.instance.config().getInt(MainConfig.CFG.COMMANDS_FORCE_GROW_DEFAULT_RADIUS, 10);

            if (args.length > 1) {
                try {
                    radius = Math.max(1, Integer.parseInt(args[1]));
                    int configValue = TreeAssist.instance.config().getInt(MainConfig.CFG.COMMANDS_FORCE_GROW_MAX_RADIUS, 30);
                    if (radius > configValue) {
                        TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_OUT_OF_RANGE.parse(String.valueOf(configValue)));
                        return;
                    }
                } catch (Exception e) {
                }
            }

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    nextBlock:
                    for (int z = -radius; z <= radius; z++) {
                        if (MaterialUtils.isSapling(player.getLocation().add(x, y, z).getBlock().getType())) {
                            Block block = player.getLocation().add(x, y, z).getBlock();
                            BlockState state = block.getState();
                            Sapling sap = (Sapling) state.getData();

                            TreeType type = args.length > 2 ? TreeType.BIG_TREE : TreeType.TREE;

                            if (sap.getSpecies() != TreeSpecies.GENERIC) {
                                type = TreeType.valueOf(sap.getSpecies().name());
                            }
                            if (type == TreeType.JUNGLE) {
                                type = TreeType.SMALL_JUNGLE;
                            }

                            for (int offset = 0; offset < 7; offset++) {
                                if (block.getRelative(BlockFace.UP, offset).getType() == Material.DIRT) {
                                    continue nextBlock;
                                }
                            }

                            Material oldSapling = block.getType();
                            block.setType(Material.AIR, true);
                            for (int i = 0; i < 20; i++) {
                                if (block.getWorld().generateTree(block.getLocation(), type)) {
                                    continue nextBlock;
                                }
                            }
                            block.setType(oldSapling);
                        }
                    }
                }
            }

            return;
        }
        TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_ONLY_PLAYERS.parse());
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("forcegrow");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!fg");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist forcegrow - force saplings around you to grow";
    }

    @Override
    public List<String> completeTab(String[] strings) {
        return new ArrayList<>(); // we have no arguments
    }
}
