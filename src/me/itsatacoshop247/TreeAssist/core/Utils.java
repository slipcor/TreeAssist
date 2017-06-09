package me.itsatacoshop247.TreeAssist.core;

import com.gmail.nossr50.api.AbilityAPI;
import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.config.experience.ExperienceConfig;
import me.itsatacoshop247.TreeAssist.TreeAssist;
import me.itsatacoshop247.TreeAssist.core.Language.MSG;
import me.itsatacoshop247.TreeAssist.trees.AbstractGenericTree;
import me.itsatacoshop247.TreeAssist.trees.CustomTree;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
    public static List<Material> naturalMaterials = new ArrayList<>();

	static {
        // elements
        naturalMaterials.add(Material.AIR);
        naturalMaterials.add(Material.FIRE);
        naturalMaterials.add(Material.WATER);
        naturalMaterials.add(Material.STATIONARY_WATER);
        naturalMaterials.add(Material.SNOW_BLOCK);
        naturalMaterials.add(Material.SNOW);

        // ground materials

        naturalMaterials.add(Material.STONE);
        naturalMaterials.add(Material.GRASS);
        naturalMaterials.add(Material.DIRT);
        naturalMaterials.add(Material.SAND);
        naturalMaterials.add(Material.STAINED_CLAY);
        naturalMaterials.add(Material.MYCEL);

        // natural growing things

        naturalMaterials.add(Material.SAPLING);
        naturalMaterials.add(Material.LEAVES);
        naturalMaterials.add(Material.LEAVES_2);
        naturalMaterials.add(Material.YELLOW_FLOWER);
        naturalMaterials.add(Material.RED_ROSE);
        naturalMaterials.add(Material.DOUBLE_PLANT);
        naturalMaterials.add(Material.BROWN_MUSHROOM);
        naturalMaterials.add(Material.RED_MUSHROOM);
        naturalMaterials.add(Material.LONG_GRASS);
        naturalMaterials.add(Material.DEAD_BUSH);
        naturalMaterials.add(Material.SUGAR_CANE_BLOCK);
        naturalMaterials.add(Material.VINE);
        naturalMaterials.add(Material.WATER_LILY);
        naturalMaterials.add(Material.HUGE_MUSHROOM_1);
        naturalMaterials.add(Material.HUGE_MUSHROOM_2);
        naturalMaterials.add(Material.MELON_BLOCK);
        naturalMaterials.add(Material.PUMPKIN);
        naturalMaterials.add(Material.COCOA);

        // blocks that are used in farms

        naturalMaterials.add(Material.TORCH);
        naturalMaterials.add(Material.RAILS);
	}

	public static List<Integer> toolgood = Arrays.asList(271, 275, 258, 286,
			279);
	public static List<Integer> toolbad = Arrays.asList(256, 257, 267, 268,
			269, 270, 272, 273, 274, 276, 277, 278, 283, 284, 285, 290, 291,
			292, 293, 294);


    public static void removeCustomGroup(Player player) {
        if (CustomTree.customTreeBlocks.size() != CustomTree.customLogs.size() ||
                CustomTree.customLogs.size() != CustomTree.customSaplings.size()) {
            player.sendMessage(Language.parse(MSG.ERROR_CUSTOM_LISTS));
            return;
        }
    }

    public static void removeRequiredTool(Player player) {
        ItemStack inHand = player.getItemInHand();
        if (inHand == null || inHand.getType() == Material.AIR) {
            player.sendMessage(Language.parse(MSG.ERROR_EMPTY_HAND));
            return;
        }

        String definition = null;

        List<?> fromConfig = Utils.plugin.getConfig().getList("Tools.Tools List");
        if (fromConfig.contains(inHand.getType().name())) {
            fromConfig.remove(inHand.getType().name());
            definition = inHand.getType().name();
        } else if (fromConfig.contains(inHand.getTypeId())) {
            fromConfig.remove(fromConfig.contains(inHand.getTypeId()));
            definition = String.valueOf(fromConfig.contains(inHand.getTypeId()));
        } else if (fromConfig.contains(String.valueOf(inHand.getTypeId()))) {
            fromConfig.remove(String.valueOf(fromConfig.contains(inHand.getTypeId())));
            definition = String.valueOf(fromConfig.contains(inHand.getTypeId()));
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
            Object cLog = CustomTree.customLogs.get(i);
            if (cLog instanceof Integer) {
                // LOG definition only has ID, no data!
                int id = (Integer) cLog;
                if (log.getTypeId() == id) {
                    Object cLeaf = CustomTree.customTreeBlocks.get(i);
                    if (cLeaf instanceof Integer) {
                        // LEAF definition only has ID, no data!
                        int leafId = (Integer) cLeaf;
                        if (leafId == leaf.getTypeId()) {
                            Object cSapling = CustomTree.customSaplings.get(i);
                            if (cSapling instanceof Integer) {
                                // SAPLING definition only has ID, no data!
                                int saplingId = (Integer) cSapling;
                                if (saplingId == sapling.getTypeId()) {
                                    player.sendMessage(Language.parse(MSG.ERROR_CUSTOM_EXISTS));
                                    return;
                                }
                                addLog(log.getTypeId() + ":" + log.getData().getData());
                                addSapling(sapling.getTypeId() + ":" + sapling.getData().getData());
                                addLeaf(leaf.getTypeId() + ":" + leaf.getData().getData());
                                plugin.saveConfig();
                                plugin.reloadLists();
                                player.sendMessage(Language.parse(MSG.INFO_CUSTOM_ADDED));
                                return;
                            }
                            // SAPLING definition contains data!
                            String saplingDef = (String) cSapling;
                            if (saplingDef.equals(sapling.getTypeId() + ":" + sapling.getData().getData())) {
                                player.sendMessage(Language.parse(MSG.ERROR_CUSTOM_EXISTS));
                                return;
                            }
                            addLog(log.getTypeId() + ":" + log.getData().getData());
                            addSapling(sapling.getTypeId() + ":" + sapling.getData().getData());
                            addLeaf(leaf.getTypeId() + ":" + leaf.getData().getData());
                            plugin.saveConfig();
                            plugin.reloadLists();
                            player.sendMessage(Language.parse(MSG.INFO_CUSTOM_ADDED));
                            return;
                        }
                        continue;
                    }
                    // LEAF definition contains data

                    String leafDef = (String) cLeaf;
                    if (leafDef.equals(leaf.getTypeId() + ":" + leaf.getData().getData())) {
                        Object cSapling = CustomTree.customSaplings.get(i);
                        if (cSapling instanceof Integer) {
                            // SAPLING definition only has ID, no data!
                            int saplingId = (Integer) cSapling;
                            if (saplingId == sapling.getTypeId()) {
                                player.sendMessage(Language.parse(MSG.ERROR_CUSTOM_EXISTS));
                                return;
                            }
                            addLog(log.getTypeId() + ":" + log.getData().getData());
                            addSapling(sapling.getTypeId() + ":" + sapling.getData().getData());
                            addLeaf(leaf.getTypeId() + ":" + leaf.getData().getData());
                            plugin.saveConfig();
                            plugin.reloadLists();
                            player.sendMessage(Language.parse(MSG.INFO_CUSTOM_ADDED));
                            return;
                        }
                        // SAPLING definition contains data!
                        String saplingDef = (String) cSapling;
                        if (saplingDef.equals(sapling.getTypeId() + ":" + sapling.getData().getData())) {
                            player.sendMessage(Language.parse(MSG.ERROR_CUSTOM_EXISTS));
                            return;
                        }
                        addLog(log.getTypeId() + ":" + log.getData().getData());
                        addSapling(sapling.getTypeId() + ":" + sapling.getData().getData());
                        addLeaf(leaf.getTypeId() + ":" + leaf.getData().getData());
                        plugin.saveConfig();
                        plugin.reloadLists();
                        player.sendMessage(Language.parse(MSG.INFO_CUSTOM_ADDED));
                        return;
                    }
                    continue;
                }
                continue;
            }
            // LOG definition contains data!
            String def = (String) cLog;
            if (def.equals(log.getTypeId() + ":" + log.getData().getData())) {
                Object cLeaf = CustomTree.customTreeBlocks.get(i);
                if (cLeaf instanceof Integer) {
                    // LEAF definition only has ID, no data!
                    int leafId = (Integer) cLeaf;
                    if (leafId == leaf.getTypeId()) {
                        Object cSapling = CustomTree.customSaplings.get(i);
                        if (cSapling instanceof Integer) {
                            // SAPLING definition only has ID, no data!
                            int saplingId = (Integer) cSapling;
                            if (saplingId == sapling.getTypeId()) {
                                player.sendMessage(Language.parse(MSG.ERROR_CUSTOM_EXISTS));
                                return;
                            }
                            addLog(log.getTypeId() + ":" + log.getData().getData());
                            addSapling(sapling.getTypeId() + ":" + sapling.getData().getData());
                            addLeaf(leaf.getTypeId() + ":" + leaf.getData().getData());
                            plugin.saveConfig();
                            plugin.reloadLists();
                            player.sendMessage(Language.parse(MSG.INFO_CUSTOM_ADDED));
                            return;
                        }
                        // SAPLING definition contains data!
                        String saplingDef = (String) cSapling;
                        if (saplingDef.equals(sapling.getTypeId() + ":" + sapling.getData().getData())) {
                            player.sendMessage(Language.parse(MSG.ERROR_CUSTOM_EXISTS));
                            return;
                        }
                        addLog(log.getTypeId() + ":" + log.getData().getData());
                        addSapling(sapling.getTypeId() + ":" + sapling.getData().getData());
                        addLeaf(leaf.getTypeId() + ":" + leaf.getData().getData());
                        plugin.saveConfig();
                        plugin.reloadLists();
                        player.sendMessage(Language.parse(MSG.INFO_CUSTOM_ADDED));
                        return;
                    }
                    continue;
                }
                // LEAF definition contains data

                String leafDef = (String) cLeaf;
                if (leafDef.equals(leaf.getTypeId() + ":" + leaf.getData().getData())) {
                    Object cSapling = CustomTree.customSaplings.get(i);
                    if (cSapling instanceof Integer) {
                        // SAPLING definition only has ID, no data!
                        int saplingId = (Integer) cSapling;
                        if (saplingId == sapling.getTypeId()) {
                            player.sendMessage(Language.parse(MSG.ERROR_CUSTOM_EXISTS));
                            return;
                        }
                        addLog(log.getTypeId() + ":" + log.getData().getData());
                        addSapling(sapling.getTypeId() + ":" + sapling.getData().getData());
                        addLeaf(leaf.getTypeId() + ":" + leaf.getData().getData());
                        plugin.saveConfig();
                        plugin.reloadLists();
                        player.sendMessage(Language.parse(MSG.INFO_CUSTOM_ADDED));
                        return;
                    }
                    // SAPLING definition contains data!
                    String saplingDef = (String) cSapling;
                    if (saplingDef.equals(sapling.getTypeId() + ":" + sapling.getData().getData())) {
                        player.sendMessage(Language.parse(MSG.ERROR_CUSTOM_EXISTS));
                        return;
                    }
                    addLog(log.getTypeId() + ":" + log.getData().getData());
                    addSapling(sapling.getTypeId() + ":" + sapling.getData().getData());
                    addLeaf(leaf.getTypeId() + ":" + leaf.getData().getData());
                    plugin.saveConfig();
                    plugin.reloadLists();
                    player.sendMessage(Language.parse(MSG.INFO_CUSTOM_ADDED));
                    return;
                }
                continue;
            }
        }
        addLog(log.getTypeId() + ":" + log.getData().getData());
        addSapling(sapling.getTypeId() + ":" + sapling.getData().getData());
        addLeaf(leaf.getTypeId() + ":" + leaf.getData().getData());
        plugin.saveConfig();
        plugin.reloadLists();
        player.sendMessage(Language.parse(MSG.INFO_CUSTOM_ADDED));
    }

    private static void addLog(String val) {
        List<String> values = new ArrayList<String>();
        for (Object o : CustomTree.customLogs) {
            values.add(String.valueOf(o));
        }
        values.add(val);
        plugin.getConfig().set("Modding.Custom Logs", values);
    }

    private static void addSapling(String val) {
        List<String> values = new ArrayList<String>();
        for (Object o : CustomTree.customSaplings) {
            values.add(String.valueOf(o));
        }
        values.add(val);
        plugin.getConfig().set("Modding.Custom Saplings", values);
    }

    private static void addLeaf(String val) {
        List<String> values = new ArrayList<String>();
        for (Object o : CustomTree.customTreeBlocks) {
            values.add(String.valueOf(o));
        }
        values.add(val);
        plugin.getConfig().set("Modding.Custom Tree Blocks", values);
    }

    public static void addRequiredTool(Player player) {
        ItemStack item = player.getItemInHand();
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
            } else if (obj instanceof Integer) {
                Integer value = (Integer) obj;
                try {
                    Material mat = Material.getMaterial(value);
                    result.add(mat.name());
                } catch (Exception e) {
                    result.add(String.valueOf(obj));
                }
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
		if (fromConfig.contains(inHand.getType().name())
				|| fromConfig.contains(inHand.getTypeId())) {
			return true;
		}
	
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
		return (toolbad.contains(itemStack.getTypeId()) || toolgood
				.contains(itemStack.getTypeId()));
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
        int toAdd = ExperienceConfig.getInstance().getWoodcuttingTreeXP(tree.getSpecies());
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
	 * Should the given data be replanted?
	 * 
	 * @param data
	 *            the log data
	 * @return if a sapling should be replanted
	 */
	public static boolean replantType(byte data) {
        if (data == 0) {
            return Utils.plugin.getConfig()
                    .getBoolean("Sapling Replant.Tree Types to Replant.Acacia");
        }
        if (data == 1) {
            return Utils.plugin.getConfig()
                    .getBoolean("Sapling Replant.Tree Types to Replant.Dark Oak");
        }
        if (data == 99) {
            return Utils.plugin.getConfig()
                    .getBoolean("Sapling Replant.Tree Types to Replant.Brown Shroom");
        }
        if (data == 100) {
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


	public static void initiateList(String string, List<Integer> validTypes) {
		for (Object obj : Utils.plugin.getConfig().getList(string)) {
			if (obj instanceof Integer) {
				validTypes.add((Integer) obj);
				continue;
			}
			if (obj.equals("LIST ITEMS GO HERE")) {
				List<Object> list = new ArrayList<Object>();
				list.add(-1);
				Utils.plugin.getConfig().set(string, list);
				Utils.plugin.saveConfig();
				break;
			}
			validTypes.add(Integer.parseInt(((String) obj).split(":")[0]));
		}
	}

	public static TreeAssist plugin;
}
