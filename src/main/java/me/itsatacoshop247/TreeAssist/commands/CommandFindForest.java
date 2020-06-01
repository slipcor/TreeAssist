package me.itsatacoshop247.TreeAssist.commands;

import me.itsatacoshop247.TreeAssist.core.Language;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.ChatColor;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class CommandFindForest extends AbstractCommand {
    Map<String, List<Biome>> biomeMap = new HashMap<>();

    public CommandFindForest() {
        super(new String[]{"treeassist.findforest"});

        biomeMap.put("ACACIA", Arrays.asList(Biome.SAVANNA));
        biomeMap.put("BIRCH", Arrays.asList(Biome.BIRCH_FOREST, Biome.BIRCH_FOREST_HILLS));
        biomeMap.put("DARK_OAK", Arrays.asList(Biome.DARK_FOREST));
        biomeMap.put("OAK", Arrays.asList(Biome.FOREST));
        biomeMap.put("JUNGLE", Arrays.asList(Biome.JUNGLE, Biome.JUNGLE_HILLS));
        biomeMap.put("SPRUCE", Arrays.asList(Biome.TAIGA, Biome.GIANT_TREE_TAIGA));
        biomeMap.put("MUSHROOM", Arrays.asList(Biome.MUSHROOM_FIELDS, Biome.MUSHROOM_FIELD_SHORE));
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

        int distanceSquared = Integer.MAX_VALUE;
        Block foundBlock = null;

        for (int x = -20; x <= 20; x += 1 + (Math.abs(x) / 5)) {
            for (int z = -20; z <= 20; z += 1 + (Math.abs(z) / 5)) {
                Block block = player.getLocation().getBlock().getRelative(x * 50, 0, z * 50);

                if (biomes.contains(block.getBiome())) {
                    if (block.getLocation().distanceSquared(player.getLocation()) < distanceSquared) {
                        distanceSquared = (int) block.getLocation().distanceSquared(player.getLocation());
                        foundBlock = block;
                    }
                }
            }
        }
        if (distanceSquared < Integer.MAX_VALUE) {
            player.sendMessage(Language.parse(Language.MSG.SUCCESSFUL_FINDFOREST,
                    foundBlock.getX() + "/" + foundBlock.getY() + "/" + foundBlock.getZ()));
        } else {
            sender.sendMessage(Language.parse(Language.MSG.ERROR_FINDFOREST, args[0]));
        }
    }

    @Override
    public List<String> completeTab(String[] args) {
        List<String> results = new ArrayList<>();

        if (args.length < 2 || args[1].equals("")) {
            // list first argument possibilities
            results.addAll(biomeMap.keySet());

            Collections.sort(results);
            return results;
        }

        if (args.length > 2) {
            return results; // don't go too far!
        }

        for (String treeType : biomeMap.keySet()) {
            if (treeType.startsWith(args[1].toUpperCase())) {
                results.add(treeType);
            }
        }
        Collections.sort(results);
        return results;
    }

    @Override
    public List<String> getMain() {
        return Collections.singletonList("findforest");
    }

    @Override
    public List<String> getShort() {
        return Collections.singletonList("!ff");
    }

    @Override
    public String getShortInfo() {
        return "/treeassist findforest [treetype] - find biome based on tree type";
    }
}
