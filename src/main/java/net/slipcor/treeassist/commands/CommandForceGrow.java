package net.slipcor.treeassist.commands;

import net.slipcor.core.CoreCommand;
import net.slipcor.core.CorePlugin;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.discovery.TreeStructure;
import net.slipcor.treeassist.utils.BlockUtils;
import net.slipcor.treeassist.utils.MaterialUtils;
import net.slipcor.treeassist.yml.Language;
import net.slipcor.treeassist.yml.MainConfig;
import org.bukkit.*;
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

        int loc_x = 0;
        int loc_y = 0;
        int loc_z = 0;
        int radius = TreeAssist.instance.config().getInt(MainConfig.CFG.COMMANDS_FORCE_GROW_DEFAULT_RADIUS, 10);
        String worldName;

        if (args.length > 4) {
            worldName = args[1];
            loc_x = Integer.parseInt(args[2]);
            loc_y = Integer.parseInt(args[3]);
            loc_z = Integer.parseInt(args[4]);
            if (args.length > 5) {
                radius = Integer.parseInt(args[5]);
            }
        } else if (sender instanceof Player) {
            Player player = (Player) sender;
            loc_x = player.getLocation().getBlockX();
            loc_y = player.getLocation().getBlockY();
            loc_z = player.getLocation().getBlockZ();
            worldName = player.getWorld().getName();
            if (args.length > 1) {
                radius = Integer.parseInt(args[1]);
            }
        } else {
            TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_ONLY_PLAYERS.parse());
            return;
        }

        World world = Bukkit.getWorld(worldName);
        Block center = world.getBlockAt(loc_x, loc_y, loc_z);
        radius = Math.max(1, radius);
        int configValue = TreeAssist.instance.config().getInt(MainConfig.CFG.COMMANDS_FORCE_GROW_MAX_RADIUS, 30);
        if (radius > configValue) {
            TreeAssist.instance.sendPrefixed(sender, Language.MSG.ERROR_OUT_OF_RANGE.parse(String.valueOf(configValue)));
            return;
        }
        TreeStructure.debug.i("force grow attempt around: " + center);

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                nextBlock:
                for (int z = -radius; z <= radius; z++) {
                    Block block = center.getRelative(x, y, z);
                    if (MaterialUtils.isSapling(block.getType())) {
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
                        TreeStructure.debug.i("force growing " + BlockUtils.printBlock(block));
                        block.setType(Material.AIR, true);
                        for (int i = 0; i < 20; i++) {
                            if (block.getWorld().generateTree(block.getLocation(), type)) {
                                TreeStructure.debug.i("force grow successful ");
                                continue nextBlock;
                            }
                        }
                        block.setType(oldSapling);
                        TreeStructure.debug.i("force grow failed");
                    }
                }
            }
        }
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
