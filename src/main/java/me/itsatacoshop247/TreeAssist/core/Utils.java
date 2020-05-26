package me.itsatacoshop247.TreeAssist.core;

import me.itsatacoshop247.TreeAssist.TreeAssist;
import me.itsatacoshop247.TreeAssist.core.Language.MSG;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public final class Utils {
	private Utils() {
	}

    public static TreeAssist plugin;
    private static Boolean useFallingBlock = null;

    private static List<FallingBlock> fallingBlocks = new ArrayList<>();

    public static Map<String, TreeConfig> treeConfigs = new HashMap<>();

    public static void addRequiredTool(Player player, TreeConfig treeConfig) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            item = player.getInventory().getItemInOffHand();
        }
        if (item.getType() == Material.AIR) {
            player.sendMessage(Language.parse(MSG.ERROR_EMPTY_HAND));
            return;
        }
        if (isRequiredTool(item, treeConfig)) {
            player.sendMessage(Language.parse(MSG.ERROR_ADDTOOL_ALREADY));
            return;
        }
        StringBuffer entry = new StringBuffer();

        try {
            entry.append(item.getType().name());
        } catch (Exception e) {
            final String msg = "Could not retrieve item type name: " + String.valueOf(item.getType());
            plugin.getLogger().severe(msg);
            player.sendMessage(Language.parse(MSG.ERROR_ADDTOOL_OTHER, msg));
            return;
        }

        boolean found = false;

        for (Enchantment ench : item.getEnchantments().keySet()) {
            if (found) {
                player.sendMessage(Language.parse(MSG.WARNING_ADDTOOL_ONLYONE, ench.getName()));
                break;
            }
            entry.append(':');
            entry.append(ench.getName());
            entry.append(':');
            entry.append(item.getEnchantmentLevel(ench));
            found = true;
        }
        List<String> result = new ArrayList<String>();

        List<String> fromConfig = treeConfig.getStringList(TreeConfig.CFG.TOOL_LIST, new ArrayList<>());
        result.addAll(fromConfig);
        result.add(entry.toString());
        treeConfig.getYamlConfiguration().set(TreeConfig.CFG.TOOL_LIST.getNode(), result);
        treeConfig.save();
        player.sendMessage(Language.parse(MSG.SUCCESSFUL_ADDTOOL, entry.toString()));
    }

    public static void removeRequiredTool(Player player, TreeConfig treeConfig) {
        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand.getType() == Material.AIR) {
            inHand = player.getInventory().getItemInOffHand();
        }
        if (inHand.getType() == Material.AIR) {
            player.sendMessage(Language.parse(MSG.ERROR_EMPTY_HAND));
            return;
        }

        String definition = null;

        List<String> fromConfig = treeConfig.getStringList(TreeConfig.CFG.TOOL_LIST, new ArrayList<>());
        if (fromConfig.contains(inHand.getType().name())) {
            fromConfig.remove(inHand.getType().name());
            definition = inHand.getType().name();
        } else {
            for (String tool : fromConfig) {
                if (!tool.startsWith(inHand.getType().name())) {
                    continue; // skip other names
                }

                String[] values = tool.split(":");

                if (values.length < 2) {
                    definition = tool;
                    // (found) name
                } else {

                    for (Enchantment ench : inHand.getEnchantments().keySet()) {
                        if (!ench.getName().equalsIgnoreCase(values[1])) {
                            continue; // skip other enchantments
                        }
                        int level = 0;
                        if (values.length < 3) { // has correct enchantment, no level needed
                            definition = tool;
                        } else {
                            try {
                                level = Integer.parseInt(values[2]);
                            } catch (Exception e) { // invalid level defined, defaulting to no
                                definition = tool;
                                // level
                            }

                            if (level > inHand.getEnchantments().get(ench)) {
                                continue; // enchantment too low
                            }
                            definition = tool;
                        }

                    }
                }
            }
            if (definition == null) {
                player.sendMessage(Language.parse(MSG.ERROR_REMOVETOOL_NOTDONE));
                return;
            } else {
                fromConfig.remove(definition);
            }
        }

        treeConfig.getYamlConfiguration().set(TreeConfig.CFG.TOOL_LIST.getNode(), fromConfig);
        treeConfig.save();
        player.sendMessage(Language.parse(MSG.SUCCESSFUL_REMOVETOOL, definition));
    }

    public static boolean isAir(final Material mat) {
        return mat == null || mat == Material.AIR || mat == Material.CAVE_AIR || mat == Material.VOID_AIR;
    }

    public static boolean isLeaf(Material material) {
        return TreeStructure.allExtras.contains(material);
    }

    public static boolean isLog(Material material) {
        return TreeStructure.allTrunks.contains(material);
    }

	/**
	 * Check if the player has a needed tool
	 * 
	 * @param inHand
	 *            the held item
	 * @return if the player has a needed tool
	 */
	public static boolean isRequiredTool(final ItemStack inHand, TreeConfig treeConfig) {
		List<String> fromConfig = treeConfig.getStringList(TreeConfig.CFG.TOOL_LIST, new ArrayList<>());
		if (fromConfig.contains(inHand.getType().name())) {
			return true;
		}
	
		for (Object obj : fromConfig) {
			if (!(obj instanceof String)) {
				continue; // skip item IDs
			}
			String tool = (String) obj;
			if (!tool.equalsIgnoreCase(inHand.getType().name())) {
				continue; // skip other names
			}
	
			String[] values = tool.split(":");
	
			if (values.length < 2) {
				return true; // no enchantment found, defaulting to plain
								// (found) name
			}
	
			for (Enchantment ench : inHand.getEnchantments().keySet()) {
				if (!ench.getName().equalsIgnoreCase(values[1])) {
					continue; // skip other enchantments
				}
				int level = 0;
				if (values.length < 3) {
					return true; // has correct enchantment, no level needed
				}
				try {
					level = Integer.parseInt(values[2]);
				} catch (Exception e) {
					return true; // invalid level defined, defaulting to no
									// level
				}
	
				if (level > inHand.getEnchantments().get(ench)) {
					continue; // enchantment too low
				}
				return true;
			}
		}
	
		return false;
	}

	public static boolean isVanillaTool(final ItemStack itemStack) {
	    String type = itemStack.getType().name().toLowerCase();

		return type.endsWith("_axe")
                || type.endsWith("_hoe")
                || type.endsWith("_pickaxe")
                || type.endsWith("_sword")
                || type.endsWith("_shovel");
	}

    public static String joinArray(final Object[] array, final String glue) {
        final StringBuilder result = new StringBuilder("");
        for (final Object o : array) {
            result.append(glue);
            result.append(o);
        }
        if (result.length() <= glue.length()) {
            return result.toString();
        }
        return result.substring(glue.length());
    }
    
    public static boolean isSapling(Material material) {
	    if (material == Material.AIR) {
	        return false;
        }
	    for (TreeConfig config : treeConfigs.values()) {
	        if (material == config.getMaterial(TreeConfig.CFG.REPLANTING_MATERIAL)) {
	            return true;
            }
        }
    	return false;
    }
    
    public static boolean isMushroom(Material material) {
	    switch (material) {
            case BROWN_MUSHROOM:
            case BROWN_MUSHROOM_BLOCK:
            case MUSHROOM_STEM:
            case RED_MUSHROOM:
            case RED_MUSHROOM_BLOCK:
                return true;
        }
    	return false;
    }
    
    public static Material getSaplingForSpecies(TreeSpecies species) {
    	if(species == TreeSpecies.GENERIC)
    		return Material.OAK_SAPLING;
    	else if(species == TreeSpecies.REDWOOD)
    		return Material.SPRUCE_SAPLING;
    	else if(species == TreeSpecies.BIRCH)
    		return Material.BIRCH_SAPLING;
    	else if(species == TreeSpecies.JUNGLE)
    		return Material.JUNGLE_SAPLING;
    	else if(species == TreeSpecies.ACACIA)
    		return Material.ACACIA_SAPLING;
    	else if(species == TreeSpecies.DARK_OAK)
    		return Material.DARK_OAK_SAPLING;
    	else
    		return Material.OAK_SAPLING;
    }

    public static void reloadTreeDefinitions(Config config) {
        TreeStructure.allTrunks.clear();
        TreeStructure.allExtras.clear();
        treeConfigs.clear();

        File folder = new File(plugin.getDataFolder().getPath(), "trees");

        Map<String, TreeConfig> processing = new HashMap<>();

        for (File file : folder.listFiles()) {
            if (file.isDirectory()) {
                for (File subFile : file.listFiles()) {
                    if (subFile.getName().toLowerCase().endsWith(".yml")) {
                        String subNode = subFile.getName().toLowerCase().replace(".yml", "");
                        TreeConfig subTree = new TreeConfig(subFile);

                        processing.put(subNode, subTree);
                    }
                }
            } else if (file.getName().toLowerCase().endsWith(".yml")){
                String node = file.getName().toLowerCase().replace(".yml", "");
                TreeConfig tree = new TreeConfig(file);

                processing.put(node, tree);
            }
        }

        // Check for updates before we even try to load
        for (String key : processing.keySet()) {
            TreeConfig treeConfig = processing.get(key);
            TreeConfigUpdater.check(treeConfig, key);
            treeConfig.load();
        }

        int attempts = 100;

        looping: while (--attempts > 0 && processing.size() > 0) {
            process: for (String key : processing.keySet()) {
                if (treeConfigs.containsKey(key)) {
                    // We already pre-loaded it completely!
                    processing.remove(key);
                    continue looping;
                }
                TreeConfig treeConfig = processing.get(key);
                String parentKey = treeConfig.getYamlConfiguration().getString("Parent", null);
                if (parentKey == null) {
                    // We are a parent. We do not need to look for others before us
                    treeConfigs.put(key, treeConfig);
                    processing.remove(key);
                    continue looping;
                }
                if (treeConfigs.containsKey(parentKey)) {
                    // we can now read the parent and apply defaults!
                    System.out.println("loading defaults of " + parentKey + " into " + key);
                    treeConfig.loadDefaults(treeConfigs.get(parentKey));
                    treeConfigs.put(key, treeConfig);
                    processing.remove(key);
                    continue looping;
                }
                // Otherwise we skip around until we find a required parent, hopefully...
            }
        }

        for (String key : processing.keySet()) {
            plugin.getLogger().severe("Parent file not found for: " + key);
        }
    }

    public static void breakBlock(Player player, Block block) {
        breakBlock(player, block, null);
    }

    public static void breakBlock(Block block) {
        breakBlock(null, block, null);
    }

    public static void breakBlock(Player player, Block block, ItemStack tool) {
        if (useFallingBlock == null) {
            useFallingBlock = Utils.plugin.getTreeAssistConfig().getBoolean(Config.CFG.MAIN_USE_FALLING_BLOCKS);
        }

        if (useFallingBlock) {
            Collection<ItemStack> drops = tool == null ? block.getDrops() : block.getDrops(tool);

            BlockData data = block.getBlockData();

            block.setType(Material.AIR);

            FallingBlock falling = block.getWorld().spawnFallingBlock(block.getLocation(), data);

            fallingBlocks.add(falling);

            if (player != null) {
                player.sendBlockChange(block.getLocation(), block.getBlockData());
            }

            for(ItemStack item : drops) {
                block.getWorld().dropItemNaturally(block.getLocation(), item);
            }
        } else {
            if (tool == null) {
                block.breakNaturally();
            } else {
                block.breakNaturally(tool);
            }
        }
    }

    /**
     * thanks to filbert66 for this determination method!
     *
     * @param tool the itemstack being used
     * @return the seconds that it will take to destroy
     */
    public static int calculateCooldown(ItemStack tool, List<Block> blockList) {

        Material element = (tool != null ? tool.getType() : null);

        float singleTime;

        switch (element) {
            case GOLDEN_AXE:
                singleTime = 0.25F;
                break;
            case DIAMOND_AXE:
                singleTime = 0.4F;
                break;
            case IRON_AXE:
                singleTime = 0.5F;
                break;
            case STONE_AXE:
                singleTime = 0.75F;
                break;
            case WOODEN_AXE:
                singleTime = 1.5F;
                break;

            default:
                singleTime = 3.0F;
                break;
        }

        float efficiencyFactor = 1.0F;
        if (tool != null && tool.hasItemMeta()) {
            int efficiencyLevel = tool.getItemMeta().getEnchantLevel(
                    Enchantment.DIG_SPEED);
            for (int i = 0; i < efficiencyLevel; i++) {
                efficiencyFactor /= 1.3F;
            }
        }

        int numLogs = 0;
        for (Block b : blockList) {
            if (isLeaf(b.getType())) {
                numLogs++;
            }
        }

        return (int) (numLogs * singleTime * efficiencyFactor);
    }

    public static boolean removeIfFallen(final Entity item) {
        if (fallingBlocks.contains(item)) {
            fallingBlocks.remove(item);
            item.remove();
            return true;
        }
        return false;
    }

    public static boolean matchContains(List<String> list, String needle, boolean partial) {
        if (list.contains(needle)) {
            // the entry is contained perfectly
            return true;
        }
        for (String entry : list) {
            if (partial && entry.contains(needle)) {
                return true;
            }
            if (entry.contains("*")) {
                String compare = entry.replace("*", "");
                if (needle.contains(compare)) {
                    return true;
                }
            } else if (entry.equals(needle)) {
                return true;
            }
        }
        return false;
    }

    public static TreeConfig findConfigByDroppedSapling(Material material) {
        for (TreeConfig config : treeConfigs.values()) {
            if (material != Material.AIR && config.getBoolean(TreeConfig.CFG.REPLANTING_DROPPED_SAPLINGS)) {
                Material sapling = config.getMaterial(TreeConfig.CFG.REPLANTING_MATERIAL);
                if (material == sapling) {
                    return config;
                }
            }
        }
        return null;
    }
}
