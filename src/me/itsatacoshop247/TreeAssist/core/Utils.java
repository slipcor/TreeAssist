package me.itsatacoshop247.TreeAssist.core;

import com.gmail.nossr50.api.AbilityAPI;
import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.skills.SkillType;
import me.itsatacoshop247.TreeAssist.TreeAssist;
import me.itsatacoshop247.TreeAssist.core.Language.MSG;
import me.itsatacoshop247.TreeAssist.trees.AbstractGenericTree;
import me.itsatacoshop247.TreeAssist.trees.CustomTree;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Tree;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Utils {
	private Utils() {
	}

    // if it's not one of these blocks, it's safe to assume its a house/building
    private static List<Material> naturalMaterials = new ArrayList<>();
    private static List<Material> logMaterials = new ArrayList<>();
    private static List<Material> leafMaterials = new ArrayList<>();
    private static List<Material> saplingMaterials = new ArrayList<>();
    private static List<Material> mushroomMaterials = new ArrayList<>();

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


    public static void removeCustomGroup(Player player) {
        if (CustomTree.customTreeBlocks.size() != CustomTree.customLogs.size() ||
                CustomTree.customLogs.size() != CustomTree.customSaplings.size()) {
            player.sendMessage(Language.parse(MSG.ERROR_CUSTOM_LISTS));
            return;
        }

        final ItemStack sapling = player.getInventory().getItem(0);
        if (sapling == null || sapling.getType() == Material.AIR) {
            player.sendMessage(Language.parse(MSG.ERROR_CUSTOM_EXPLANATION));
            return;
        }
        final ItemStack log = player.getInventory().getItem(1);
        if (log == null || log.getType() == Material.AIR) {
            player.sendMessage(Language.parse(MSG.ERROR_CUSTOM_EXPLANATION));
            return;
        }
        final ItemStack leaf = player.getInventory().getItem(2);
        if (leaf == null || leaf.getType() == Material.AIR) {
            player.sendMessage(Language.parse(MSG.ERROR_CUSTOM_EXPLANATION));
            return;
        }

        for (int i = 0; i < CustomTree.customTreeBlocks.size(); i++) {
            Material customMaterial = Material.matchMaterial(CustomTree.customLogs.get(i));
            if (customMaterial != null && log.getType() == customMaterial) {
                Material customLeaf = Material.matchMaterial(CustomTree.customTreeBlocks.get(i));
                if (customLeaf != null && customLeaf == leaf.getType()) {
                    Material customSapling = Material.matchMaterial(CustomTree.customSaplings.get(i));
                    if (customSapling != null && customSapling == sapling.getType()) {
                        removeLeaf(i);
                        removeLog(i);
                        removeSapling(i);
                        plugin.saveConfig();
                        plugin.reloadLists();
                        player.sendMessage(Language.parse(MSG.INFO_CUSTOM_REMOVED));
                        return;
                    }
                }
            }
        }
        player.sendMessage(Language.parse(MSG.ERROR_CUSTOM_NOT_FOUND));
    }

    public static void removeRequiredTool(Player player) {
        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand == null)
        	inHand = player.getInventory().getItemInOffHand();
        if (inHand == null || inHand.getType() == Material.AIR) {
            player.sendMessage(Language.parse(MSG.ERROR_EMPTY_HAND));
            return;
        }

        String definition = null;

        List<?> fromConfig = Utils.plugin.getConfig().getList("Tools.Tools List");
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

    public static void addCustomGroup(Player player) {
        if (CustomTree.customTreeBlocks.size() != CustomTree.customLogs.size() ||
                CustomTree.customLogs.size() != CustomTree.customSaplings.size()) {
            player.sendMessage(Language.parse(MSG.ERROR_CUSTOM_LISTS));
            return;
        }

        final ItemStack sapling = player.getInventory().getItem(0);
        if (sapling == null || sapling.getType() == Material.AIR) {
            player.sendMessage(Language.parse(MSG.ERROR_CUSTOM_EXPLANATION));
            return;
        }
        final ItemStack log = player.getInventory().getItem(1);
        if (log == null || log.getType() == Material.AIR) {
            player.sendMessage(Language.parse(MSG.ERROR_CUSTOM_EXPLANATION));
            return;
        }
        final ItemStack leaf = player.getInventory().getItem(2);
        if (leaf == null || leaf.getType() == Material.AIR) {
            player.sendMessage(Language.parse(MSG.ERROR_CUSTOM_EXPLANATION));
            return;
        }

        for (int i = 0; i < CustomTree.customTreeBlocks.size(); i++) {
            Material customLog = Material.matchMaterial(CustomTree.customLogs.get(i));
            if (customLog != null && log.getType() == customLog) {
                Material customLeaf = Material.matchMaterial(CustomTree.customTreeBlocks.get(i));
                if (customLeaf != null && customLeaf == leaf.getType()) {
                    Material customSapling = Material.matchMaterial(CustomTree.customSaplings.get(i));
                    if (customSapling != null && customSapling == sapling.getType()) {
                        player.sendMessage(Language.parse(MSG.ERROR_CUSTOM_EXISTS));
                        return;
                    }
                }
            }
        }
        addLog(log.getType());
        addSapling(sapling.getType());
        addLeaf(leaf.getType());
        plugin.saveConfig();
        plugin.reloadLists();
        player.sendMessage(Language.parse(MSG.INFO_CUSTOM_ADDED));
    }

    private static void addLog(Material mat) {
        List<String> values = new ArrayList<>();
        for (String s : CustomTree.customLogs) {
            values.add(s);
        }
        values.add(mat.getKey().getNamespace()+":"+mat.getKey().getKey());
        plugin.getTreeAssistConfig().set("Modding.Custom Logs", values);
    }

    private static void addSapling(Material mat) {
        List<String> values = new ArrayList<>();
        for (String s : CustomTree.customSaplings) {
            values.add(s);
        }
        values.add(mat.getKey().getNamespace()+":"+mat.getKey().getKey());
        plugin.getTreeAssistConfig().set("Modding.Custom Saplings", values);
    }

    private static void addLeaf(Material mat) {
        List<String> values = new ArrayList<>();
        for (String s : CustomTree.customTreeBlocks) {
            values.add(s);
        }
        values.add(mat.getKey().getNamespace()+":"+mat.getKey().getKey());
        plugin.getTreeAssistConfig().set("Modding.Custom Tree Blocks", values);
    }

    public static void addRequiredTool(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item == null)
        	item = player.getInventory().getItemInOffHand();
        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage(Language.parse(MSG.ERROR_EMPTY_HAND));
            return;
        }
        if (isRequiredTool(item)) {
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
        List<?> fromConfig = Utils.plugin.getConfig().getList("Tools.Tools List");
        for (Object obj : fromConfig) {
            if (obj instanceof String) {
                result.add(String.valueOf(obj));
            }
        }
        result.add(entry.toString());
        Utils.plugin.getConfig().set("Tools.Tools List", result);
        Utils.plugin.saveConfig();
        player.sendMessage(Language.parse(MSG.SUCCESSFUL_ADDTOOL, entry.toString()));
    }
	/**
	 * Check if the player has a needed tool
	 * 
	 * @param inHand
	 *            the held item
	 * @return if the player has a needed tool
	 */
	public static boolean isRequiredTool(final ItemStack inHand) {
		List<?> fromConfig = Utils.plugin.getConfig().getList("Tools.Tools List");
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

	/**
	 * Add mcMMO exp for destroying a block
	 * 
	 * @param player
	 *            the player to give exp
	 * @param block
	 *            the block being destroyed
	 */
	public static void mcMMOaddExp(Player player, Block block) {
		Plugin mcmmo = Utils.plugin.getServer().getPluginManager().getPlugin("mcMMO");

		if (player == null) {
            AbstractGenericTree.debug.i("no Player!!");
            return;
        }

        MaterialData state = block.getState().getData();

        if (!(state instanceof Tree)) {
            AbstractGenericTree.debug.i("no Tree!!");
            return;
        }

        Tree tree = (Tree) state;
        int toAdd = ExperienceConfig.getInstance().getXp(SkillType.WOODCUTTING, tree);
        if (player.isOnline()) {
            AbstractGenericTree.debug.i("adding " + toAdd + " EXP!");
            ExperienceAPI.addXP(player, "Woodcutting", toAdd);
        } else {
            AbstractGenericTree.debug.i("adding " + toAdd + " offline EXP!");
            ExperienceAPI.addRawXPOffline(player.getName(), "Woodcutting", mcmmo.getConfig()
                    .getInt("Experience.Woodcutting.Dark_Oak"));
        }
    }

	public final static BlockFace[] NEIGHBORFACES = {BlockFace.NORTH,BlockFace.EAST,BlockFace.SOUTH,BlockFace.WEST,
	BlockFace.NORTH_EAST,BlockFace.SOUTH_EAST,BlockFace.NORTH_WEST,BlockFace.SOUTH_WEST};
	
	
	/**
	 * check if a player is using the tree feller ability atm
	 * 
	 * @param player
	 *            the player to check
	 * @return if a player is using tree feller
	 */
	public static boolean mcMMOTreeFeller(Player player) {
		boolean isMcMMOEnabled = Utils.plugin.getServer().getPluginManager()
				.isPluginEnabled("mcMMO");
	
		if (!isMcMMOEnabled) {
			return false;
		}
	
		return AbilityAPI.treeFellerEnabled(player);
	}

    /**
     * Should the given material be replanted?
     *
     * @param material
     *            the log material
     * @return if a sapling should be replanted
     */
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


	public static void initiateList(String string, List<String> validTypes) {
		for (Object obj : Utils.plugin.getConfig().getList(string)) {
			if (obj instanceof Integer) {
				continue;
			}
			if (obj.equals("LIST ITEMS GO HERE")) {
				List<Object> list = new ArrayList<Object>();
				list.add("INVALID");
				Utils.plugin.getConfig().set(string, list);
				Utils.plugin.saveConfig();
				break;
			}
			validTypes.add(((String) obj).split(":")[0]);
		}
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

    private static void removeLog(int number) {
        List<String> values = new ArrayList<>();
        int pos = 0;
        for (String s : CustomTree.customLogs) {
            if (number != pos++) {
                values.add(s);
            }
        }
        plugin.getTreeAssistConfig().set("Modding.Custom Logs", values);
    }

    private static void removeSapling(int number) {
        List<String> values = new ArrayList<>();
        int pos = 0;
        for (String s : CustomTree.customSaplings) {
            if (number != pos++) {
                values.add(s);
            }
        }
        plugin.getTreeAssistConfig().set("Modding.Custom Saplings", values);
    }

    private static void removeLeaf(int number) {
        List<String> values = new ArrayList<>();
        int pos = 0;
        for (String s : CustomTree.customTreeBlocks) {
            if (number != pos++) {
                values.add(s);
            }
        }
        plugin.getTreeAssistConfig().set("Modding.Custom Tree Blocks", values);
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

    /**
     * For backwards compatibility, we make sure that we only load strings!
     *
     * @param config the config to access
     * @param node the node to list
     * @return a sanitized string list
     */
    public static List<String> getStringList(FileConfiguration config, String node) {
        List<String> result = new ArrayList<>();
        List<?> list = config.getList(node);
        if (list == null) {
            return result;
        }
        for (Object o : list) {
            if (o instanceof String) {
                result.add((String) o);
            }
        }
        return result;
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
}
