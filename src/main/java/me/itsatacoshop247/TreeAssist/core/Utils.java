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

    // if it's not one of these blocks, it's safe to assume its a house/building
    private static List<Material> naturalMaterials = new ArrayList<>();
    private static List<Material> logMaterials = new ArrayList<>();
    private static List<Material> leafMaterials = new ArrayList<>();
    private static List<Material> saplingMaterials = new ArrayList<>();
    private static List<Material> mushroomMaterials = new ArrayList<>();

    private static Boolean useFallingBlock = null;

    private static List<FallingBlock> fallingBlocks = new ArrayList<>();

    public static Map<String, TreeConfig> treeConfigs = new HashMap<>();

	static {
        // elements
        naturalMaterials.add(Material.AIR);
        naturalMaterials.add(Material.FIRE);
        naturalMaterials.add(Material.WATER);
        naturalMaterials.add(Material.SNOW_BLOCK);
        naturalMaterials.add(Material.SNOW);

        // ground materials

        naturalMaterials.add(Material.STONE);
        naturalMaterials.add(Material.GRASS_BLOCK);
        naturalMaterials.add(Material.DIRT);
        naturalMaterials.add(Material.SAND);
        naturalMaterials.add(Material.TERRACOTTA);
        naturalMaterials.add(Material.BROWN_TERRACOTTA);
        naturalMaterials.add(Material.YELLOW_TERRACOTTA);
        naturalMaterials.add(Material.ORANGE_TERRACOTTA);
        naturalMaterials.add(Material.WHITE_TERRACOTTA);
        naturalMaterials.add(Material.RED_TERRACOTTA);
        naturalMaterials.add(Material.MYCELIUM);
        naturalMaterials.add(Material.PODZOL);

        // natural growing things

        naturalMaterials.add(Material.OAK_SAPLING);
        naturalMaterials.add(Material.SPRUCE_SAPLING);
        naturalMaterials.add(Material.BIRCH_SAPLING);
        naturalMaterials.add(Material.JUNGLE_SAPLING);
        naturalMaterials.add(Material.ACACIA_SAPLING);
        naturalMaterials.add(Material.DARK_OAK_SAPLING);
        naturalMaterials.add(Material.OAK_LEAVES);
        naturalMaterials.add(Material.SPRUCE_LEAVES);
        naturalMaterials.add(Material.BIRCH_LEAVES);
        naturalMaterials.add(Material.JUNGLE_LEAVES);
        naturalMaterials.add(Material.ACACIA_LEAVES);
        naturalMaterials.add(Material.JUNGLE_LEAVES);
        naturalMaterials.add(Material.DANDELION);
        naturalMaterials.add(Material.POPPY);
        naturalMaterials.add(Material.BLUE_ORCHID);
        naturalMaterials.add(Material.ALLIUM);
        naturalMaterials.add(Material.AZURE_BLUET);
        naturalMaterials.add(Material.RED_TULIP);
        naturalMaterials.add(Material.ORANGE_TULIP);
        naturalMaterials.add(Material.WHITE_TULIP);
        naturalMaterials.add(Material.PINK_TULIP);
        naturalMaterials.add(Material.OXEYE_DAISY);
        naturalMaterials.add(Material.SUNFLOWER);
        naturalMaterials.add(Material.LILAC);
        naturalMaterials.add(Material.GRASS);
        naturalMaterials.add(Material.TALL_GRASS);
        naturalMaterials.add(Material.FERN);
        naturalMaterials.add(Material.ROSE_BUSH);
        naturalMaterials.add(Material.PEONY);
        naturalMaterials.add(Material.BROWN_MUSHROOM);
        naturalMaterials.add(Material.RED_MUSHROOM);
        naturalMaterials.add(Material.FERN);
        naturalMaterials.add(Material.DEAD_BUSH);
        naturalMaterials.add(Material.SUGAR_CANE);
        naturalMaterials.add(Material.VINE);
        naturalMaterials.add(Material.LILY_PAD);
        naturalMaterials.add(Material.BROWN_MUSHROOM_BLOCK);
        naturalMaterials.add(Material.RED_MUSHROOM_BLOCK);
        naturalMaterials.add(Material.MUSHROOM_STEM);
        naturalMaterials.add(Material.MELON);
        naturalMaterials.add(Material.PUMPKIN);
        naturalMaterials.add(Material.COCOA);

        // blocks that are used in farms

        naturalMaterials.add(Material.TORCH);
        naturalMaterials.add(Material.RAIL);
        naturalMaterials.add(Material.HOPPER);
        naturalMaterials.add(Material.DISPENSER);
        
        // types of logs
        
        logMaterials.add(Material.OAK_LOG);
        logMaterials.add(Material.SPRUCE_LOG);
        logMaterials.add(Material.BIRCH_LOG);
        logMaterials.add(Material.JUNGLE_LOG);
        logMaterials.add(Material.ACACIA_LOG);
        logMaterials.add(Material.DARK_OAK_LOG);
        
        // types of leaves
        
        leafMaterials.add(Material.OAK_LEAVES);
        leafMaterials.add(Material.SPRUCE_LEAVES);
        leafMaterials.add(Material.BIRCH_LEAVES);
        leafMaterials.add(Material.JUNGLE_LEAVES);
        leafMaterials.add(Material.ACACIA_LEAVES);
        leafMaterials.add(Material.DARK_OAK_LEAVES);
        
        // types of saplings
        
        saplingMaterials.add(Material.OAK_SAPLING);
        saplingMaterials.add(Material.SPRUCE_SAPLING);
        saplingMaterials.add(Material.BIRCH_SAPLING);
        saplingMaterials.add(Material.JUNGLE_SAPLING);
        saplingMaterials.add(Material.ACACIA_SAPLING);
        saplingMaterials.add(Material.DARK_OAK_SAPLING);
        
        // types of mushroom
        
        mushroomMaterials.add(Material.MUSHROOM_STEM);
        mushroomMaterials.add(Material.BROWN_MUSHROOM_BLOCK);
        mushroomMaterials.add(Material.RED_MUSHROOM_BLOCK);
	}

	public static List<Material> toolgood = Arrays.asList(Material.WOODEN_AXE, Material.STONE_AXE, Material.IRON_AXE, Material.GOLDEN_AXE,
			Material.DIAMOND_AXE);
	public static List<Material> toolbad = Arrays.asList(Material.IRON_SHOVEL, Material.IRON_PICKAXE, Material.IRON_SWORD, Material.WOODEN_SWORD,
			Material.WOODEN_SHOVEL, Material.WOODEN_PICKAXE, Material.STONE_SWORD, Material.STONE_SHOVEL, Material.STONE_PICKAXE,
			Material.DIAMOND_SWORD, Material.DIAMOND_SHOVEL, Material.DIAMOND_PICKAXE, Material.GOLDEN_SWORD, Material.GOLDEN_SHOVEL,
			Material.GOLDEN_PICKAXE, Material.WOODEN_HOE, Material.STONE_HOE, Material.IRON_HOE, Material.GOLDEN_HOE, Material.DIAMOND_HOE);


    public static void removeRequiredTool(Player player, TreeConfig treeConfig) {
        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand == null)
        	inHand = player.getInventory().getItemInOffHand();
        if (inHand == null || inHand.getType() == Material.AIR) {
            player.sendMessage(Language.parse(MSG.ERROR_EMPTY_HAND));
            return;
        }

        String definition = null;

        List<String> fromConfig = treeConfig.getStringList(TreeConfig.CFG.TOOL_LIST, new ArrayList<>());
        if (fromConfig.contains(inHand.getType().name())) {
            fromConfig.remove(inHand.getType().name());
            definition = inHand.getType().name();
        } else {
            for (Object obj : fromConfig) {
                if (!(obj instanceof String)) {
                    continue; // skip item IDs
                }
                String tool = (String) obj;
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

        player.sendMessage(Language.parse(MSG.SUCCESSFUL_REMOVETOOL, definition));
        return;


    }

    public static void addRequiredTool(Player player, TreeConfig treeConfig) {
        //TODO: find out which tree to alter
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null)
        	item = player.getInventory().getItemInOffHand();
        if (item == null || item.getType() == Material.AIR) {
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

        List<?> fromConfig = treeConfig.getStringList(TreeConfig.CFG.TOOL_LIST, new ArrayList<>());
        for (Object obj : fromConfig) {
            if (obj instanceof String) {
                result.add(String.valueOf(obj));
            }
        }
        result.add(entry.toString());
        treeConfig.getYamlConfiguration().set("Tools.Tools List", result);
        treeConfig.save();
        player.sendMessage(Language.parse(MSG.SUCCESSFUL_ADDTOOL, entry.toString()));
    }

    public static boolean isAir(final Material mat) {
        return mat == null || mat == Material.AIR || mat == Material.CAVE_AIR || mat == Material.VOID_AIR;
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
		return (toolbad.contains(itemStack.getType()) || toolgood
				.contains(itemStack.getType()));
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

	public final static BlockFace[] NEIGHBORFACES = {BlockFace.NORTH,BlockFace.EAST,BlockFace.SOUTH,BlockFace.WEST,
	BlockFace.NORTH_EAST,BlockFace.SOUTH_EAST,BlockFace.NORTH_WEST,BlockFace.SOUTH_WEST};
	


    /**
     * Should the given material be replanted?
     *
     * @param material
     *            the log material
     * @return if a sapling should be replanted
     */
    @Deprecated
    public static boolean replantType(Material material) {
        if (material == Material.BROWN_MUSHROOM_BLOCK) {
            return Utils.plugin.getConfig()
                    .getBoolean("Sapling Replant.Tree Types to Replant.Brown Shroom");
        }
        if (material == Material.RED_MUSHROOM_BLOCK) {
            return Utils.plugin.getConfig()
                    .getBoolean("Sapling Replant.Tree Types to Replant.Red Shroom");
        }
        return false;
    }

    /**
     * Should the given species be replanted?
     *
     * @param species
     *            the tree species
     * @return if a sapling should be replanted
     */
    @Deprecated
    public static boolean replantType(TreeSpecies species) {
        if (species == TreeSpecies.GENERIC) {
            return Utils.plugin.getConfig()
                    .getBoolean("Sapling Replant.Tree Types to Replant.Oak");
        }
        if (species == TreeSpecies.REDWOOD) {
            return Utils.plugin.getConfig()
                    .getBoolean("Sapling Replant.Tree Types to Replant.Spruce");
        }
        if (species == TreeSpecies.BIRCH) {
            return Utils.plugin.getConfig()
                    .getBoolean("Sapling Replant.Tree Types to Replant.Birch");
        }
        if (species == TreeSpecies.JUNGLE) {
            return Utils.plugin.getConfig()
                    .getBoolean("Sapling Replant.Tree Types to Replant.Jungle");
        }
        if (species == TreeSpecies.ACACIA) {
            return Utils.plugin.getConfig()
                    .getBoolean("Sapling Replant.Tree Types to Replant.Acacia");
        }
        if (species == TreeSpecies.DARK_OAK) {
            return Utils.plugin.getConfig()
                    .getBoolean("Sapling Replant.Tree Types to Replant.Dark Oak");
        }
        return false;
    }

	public static TreeAssist plugin;

    public static int versionCompare(String theirs, String ours) {
        String[] aTheirs = theirs.split(".");
        String[] aOurs = ours.split(("."));
        int i = 0;
        while (i < aOurs.length) {
            if (aTheirs.length<=i) {
                return -1;
            }
            try {
                int iTheirs = Integer.parseInt(aTheirs[i], 36);
                int iOurs = Integer.parseInt(aOurs[i], 36);

                if (iTheirs != iOurs) {
                    return iTheirs - iOurs;
                }
            } catch (Exception e) {
                return 0; // something fancy, assume special version that should be a snapshot
            }
            i++;
        }
        return 0;
    }
    
    public static boolean isNatural(Material material) {
    	return naturalMaterials.contains(material);
    }
    
    public static boolean isLog(Material material) {
    	return logMaterials.contains(material);
    }
    
    public static boolean isLegacyLog(Material material) {
    	return isLog(material) && !isLegacyLog2(material);
    }
    
    public static boolean isLegacyLog2(Material material) {
    	return material == Material.ACACIA_LOG || material == Material.DARK_OAK_LOG;
    }
    
    public static boolean isLeaf(Material material) {
    	return leafMaterials.contains(material);
    }
    
    public static boolean isSapling(Material material) {
    	return saplingMaterials.contains(material);
    }
    
    public static boolean isMushroom(Material material) {
    	return mushroomMaterials.contains(material);
    }
    
    public static Material resolveLegacySapling(int damage) {
    	switch(damage) {
    	case 0:
    		return Material.OAK_SAPLING;
    	case 1:
    		return Material.SPRUCE_SAPLING;
    	case 2:
    		return Material.BIRCH_SAPLING;
    	case 3:
    		return Material.JUNGLE_SAPLING;
    	case 4:
    		return Material.ACACIA_SAPLING;
    	case 5:
    		return Material.DARK_OAK_SAPLING;
    	default:
    		return Material.OAK_SAPLING;
    	}
    }
    
    public static Material getLogForSpecies(TreeSpecies species) {
    	if(species == TreeSpecies.GENERIC)
    		return Material.OAK_LOG;
    	else if(species == TreeSpecies.REDWOOD)
    		return Material.SPRUCE_LOG;
    	else if(species == TreeSpecies.BIRCH)
    		return Material.BIRCH_LOG;
    	else if(species == TreeSpecies.JUNGLE)
    		return Material.JUNGLE_LOG;
    	else if(species == TreeSpecies.ACACIA)
    		return Material.ACACIA_LOG;
    	else if(species == TreeSpecies.DARK_OAK)
    		return Material.DARK_OAK_LOG;
    	else
    		return Material.OAK_LOG;
    }
    
    public static Material getLeavesForSpecies(TreeSpecies species) {
    	if(species == TreeSpecies.GENERIC)
    		return Material.OAK_LEAVES;
    	else if(species == TreeSpecies.REDWOOD)
    		return Material.SPRUCE_LEAVES;
    	else if(species == TreeSpecies.BIRCH)
    		return Material.BIRCH_LEAVES;
    	else if(species == TreeSpecies.JUNGLE)
    		return Material.JUNGLE_LEAVES;
    	else if(species == TreeSpecies.ACACIA)
    		return Material.ACACIA_LEAVES;
    	else if(species == TreeSpecies.DARK_OAK)
    		return Material.DARK_OAK_LEAVES;
    	else
    		return Material.OAK_LEAVES;
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
    
    public static Material findMushroomTreeType(Block block) {
    	while(block.getType() == Material.MUSHROOM_STEM)
    		block = block.getRelative(BlockFace.UP);
    	return block.getType();
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
