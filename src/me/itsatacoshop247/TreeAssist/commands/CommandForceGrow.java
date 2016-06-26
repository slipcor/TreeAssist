package me.itsatacoshop247.TreeAssist.commands;

import me.itsatacoshop247.TreeAssist.core.Language;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Sapling;

import java.util.Collections;
import java.util.List;

public class CommandForceGrow extends AbstractCommand {
    public CommandForceGrow() {
        super(new String[]{"treeassist.forcegrow"});
    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.parse(Language.MSG.ERROR_PERMISSION_FORCEGROW));
            return;
        }
        if (sender instanceof Player) {
            Player player = (Player) sender;

            int radius = Utils.plugin.getConfig().getInt("Main.Force Grow Default Radius", 10);

            if (args.length > 1) {
                try {
                    radius = Math.max(1, Integer.parseInt(args[1]));
                    int configValue = Utils.plugin.getConfig().getInt("Main.Force Grow Max Radius", 30);
                    if (radius > configValue) {
                        sender.sendMessage(Language.parse(Language.MSG.ERROR_OUT_OF_RANGE, String.valueOf(configValue)));
                    }
                } catch (Exception e) {
                }
            }

            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    nextBlock:
                    for (int z = -radius; z <= radius; z++) {
                        if (player.getLocation().add(x, y, z).getBlock().getType() == Material.SAPLING) {
                            Block block = player.getLocation().add(x, y, z).getBlock();
                            BlockState state = block.getState();
                            MaterialData data = state.getData();
                            Sapling sap = (Sapling) data;
                            byte specific = sap.getData();

                            TreeType type = TreeType.TREE;

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

                            block.setType(Material.AIR);
                            for (int i = 0; i < 20; i++) {
                                if (block.getWorld().generateTree(block.getLocation(), type)) {
                                    continue nextBlock;
                                }
                            }
                            block.setType(Material.SAPLING);
                            sap.setData(specific);
                            block.getState().setData(sap);
                            block.getState().update();
                        }
                    }
                }
            }

            return;
        }
        sender.sendMessage(Language.parse(Language.MSG.ERROR_ONLY_PLAYERS));
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("forcegrow");
    }

    @Override
    public String getName() {
        return getClass().getName();
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
    public CommandTree<String> getSubs() {
        return new CommandTree<>(null);
    }
}
