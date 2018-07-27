package me.itsatacoshop247.TreeAssist.commands;

import me.itsatacoshop247.TreeAssist.core.Language;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class CommandFindForest extends AbstractCommand {
    Map<String, List<Biome>> biomeMap = new HashMap<>();

    public CommandFindForest() {
        super(new String[]{"treeassist.findforest"});

        biomeMap.put("ACACIA", Arrays.asList(new Biome[]{Biome.SAVANNA}));
        biomeMap.put("BIRCH", Arrays.asList(new Biome[]{Biome.BIRCH_FOREST, Biome.BIRCH_FOREST_HILLS}));
        biomeMap.put("DARK_OAK", Arrays.asList(new Biome[]{Biome.DARK_FOREST}));
        biomeMap.put("OAK", Arrays.asList(new Biome[]{Biome.FOREST}));
        biomeMap.put("JUNGLE", Arrays.asList(new Biome[]{Biome.JUNGLE, Biome.JUNGLE_HILLS}));
        biomeMap.put("SPRUCE", Arrays.asList(new Biome[]{Biome.TAIGA, Biome.GIANT_TREE_TAIGA}));
        biomeMap.put("MUSHROOM", Arrays.asList(new Biome[]{Biome.MUSHROOM_FIELDS, Biome.MUSHROOM_FIELD_SHORE}));

    }

    @Override
    public void commit(CommandSender sender, String[] args) {
        if (!hasPerms(sender)) {
            sender.sendMessage(Language.parse(Language.MSG.ERROR_PERMISSION_FINDFOREST));
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage(Language.parse(Language.MSG.ERROR_ONLY_PLAYERS));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.DARK_RED + this.getShortInfo());
            return;
        }

        List<Biome> biomes = biomeMap.get(args[1].toUpperCase());

        if (biomes == null) {
            String list = Utils.joinArray(biomeMap.keySet().toArray(), ", ");
            sender.sendMessage(Language.parse(Language.MSG.ERROR_INVALID_ARGUMENT_LIST, list));
            return;
        }

        Player player = (Player) sender;

        for (int diff = 1; diff < 20; diff++) {
            for (int x = -diff; x <= diff; x += 1 + (diff / 5)) {
                for (int z = -diff; z <= diff; z += 1 + (diff / 5)) {
                    Block block = player.getLocation().getBlock().getRelative(x * 50, 0, z * 50);
                    while (block.getType() != Material.AIR) {
                        block = block.getRelative(BlockFace.UP);
                    }
                    if (biomes.contains(block.getBiome())) {
                        player.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_FINDFOREST,
                                block.getX() + "/" + block.getY() + "/" + block.getZ()));
                        return;
                    }
                }
            }
        }

        sender.sendMessage(Language.parse(Language.MSG.ERROR_FINDFOREST, args[0]));

    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("findforest");
    }

    @Override
    public String getName() {
        return getClass().getName();
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!ff");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist findforest [treetype] - find biome based on tree type";
    }

    @Override
    public CommandTree<String> getSubs() {
        final CommandTree<String> result = new CommandTree<>(null);
        result.define(new String[]{"ACACIA"});
        result.define(new String[]{"BIRCH"});
        result.define(new String[]{"DARK_OAK"});
        result.define(new String[]{"OAK"});
        result.define(new String[]{"JUNGLE"});
        result.define(new String[]{"SPRUCE"});
        result.define(new String[]{"MUSHROOM"});
        return result;
    }
}
