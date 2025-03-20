package net.slipcor.treeassist.utils;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.discovery.TreeStructure;
import net.slipcor.treeassist.yml.Language;
import net.slipcor.treeassist.yml.TreeConfig;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ToolUtils {
    private ToolUtils() {}

    /**
     * Calculate cooldown seconds based on tools used and blocks mined
     *
     * @param tool the item being used
     * @return the seconds that it would take to destroy regularly
     */
    public static int calculateCoolDown(ItemStack tool, List<Block> blockList) {

        Material element = (tool != null ? tool.getType() : Material.AIR);

        float logTime;

        switch (element) {
            case GOLDEN_AXE:
                logTime = 0.25F;
                break;
            case NETHERITE_AXE:
                logTime = 0.35F;
                break;
            case DIAMOND_AXE:
                logTime = 0.4F;
                break;
            case IRON_AXE:
                logTime = 0.5F;
                break;
            case STONE_AXE:
                logTime = 0.75F;
                break;
            case WOODEN_AXE:
                logTime = 1.5F;
                break;
            default:
                logTime = 3.0F;
                break;
        }

        float strippedLogTime;

        switch (element) {
            case GOLDEN_AXE:
                strippedLogTime = 0.15F;
                break;
            case NETHERITE_AXE:
            case DIAMOND_AXE:
                strippedLogTime = 0.2F;
                break;
            case IRON_AXE:
                strippedLogTime = 0.25F;
                break;
            case STONE_AXE:
                strippedLogTime = 0.4F;
                break;
            case WOODEN_AXE:
                strippedLogTime = 0.75F;
                break;
            default:
                strippedLogTime = 1.5F;
                break;
        }

        float leafTime;

        switch (element) {
            case WOODEN_SWORD:
            case STONE_SWORD:
            case IRON_SWORD:
            case NETHERITE_SWORD:
            case DIAMOND_SWORD:
            case GOLDEN_SWORD:
                leafTime = 0.2f;
                break;
            case GOLDEN_HOE:
            case NETHERITE_HOE:
            case DIAMOND_HOE:
            case IRON_HOE:
            case SHEARS:
                leafTime = 0.05F;
                break;
            case STONE_HOE:
                leafTime = 0.1F;
                break;
            case WOODEN_HOE:
                leafTime = 0.2F;
                break;
            default:
                leafTime = 0.35F;
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

        float times = 0;

        for (Block b : blockList) {
            if (b.getType().name().contains("STRIPPED")) {
                times += strippedLogTime;
            } else if (b.getType().name().contains("LEAVES")) {
                times += leafTime;
            } else if (MaterialUtils.isLog(b.getType())) {
                times += logTime;
            }
        }

        return (int) (times * efficiencyFactor);
    }

    /**
	 * Check if the player has a matching tool
	 *
	 * @param inHand the held item
     * @param treeConfig the config to check against
	 * @return whether the player has a matching tool
	 */
	public static boolean isMatchingTool(final ItemStack inHand, TreeConfig treeConfig) {
        TreeStructure.debug.i("in hand: " + inHand.getType());
		List<String> fromConfig = treeConfig.getStringList(TreeConfig.CFG.TOOL_LIST);
		if (fromConfig.contains(inHand.getType().toString())) {
			return true;
		} else {
            TreeStructure.debug.i("valid: " + inHand.getType());
		    for (String mat : fromConfig) {
		        TreeStructure.debug.i(mat);
            }
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
				if (!(ench.getKey().getKey()).replace(':', '~').equalsIgnoreCase(values[1])) {
					continue; // skip other enchantments
				}
				int level;
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

    /**
     * Check whether a given item is considered a tool normally
     *
     * @param itemStack the item to check
     * @return whether the item is a tool item
     */
	public static boolean isVanillaTool(final ItemStack itemStack) {
	    String type = itemStack.getType().name().toLowerCase();

		return type.endsWith("_axe")
                || type.endsWith("_hoe")
                || type.endsWith("_pickaxe")
                || type.endsWith("_sword")
                || type.endsWith("_shovel");
	}

    /**
     * Add a tool to a tree config
     *
     * @param player the player issuing the command
     * @param treeConfig the tree config to change
     */
    public static void toolAdd(Player player, TreeConfig treeConfig) {
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) {
            item = player.getInventory().getItemInOffHand();
        }
        if (item.getType() == Material.AIR) {
            TreeAssist.instance.sendPrefixed(player, Language.MSG.ERROR_EMPTY_HAND.parse());
            return;
        }
        if (isMatchingTool(item, treeConfig)) {
            TreeAssist.instance.sendPrefixed(player, Language.MSG.ERROR_ADDTOOL_ALREADY.parse());
            return;
        }
        StringBuilder entry = new StringBuilder();

        try {
            entry.append(item.getType().name());
        } catch (Exception e) {
            final String msg = "Could not retrieve item type name: " + item.getType();
            TreeAssist.instance.getLogger().severe(msg);
            TreeAssist.instance.sendPrefixed(player, Language.MSG.ERROR_ADDTOOL_OTHER.parse(msg));
            return;
        }

        boolean found = false;

        for (Enchantment ench : item.getEnchantments().keySet()) {
            if (found) {
                TreeAssist.instance.sendPrefixed(player, Language.MSG.WARNING_ADDTOOL_ONLYONE.parse(ench.getKey().getKey()));
                break;
            }
            entry.append(':');
            entry.append((ench.getKey()).toString().replace(':', '~'));
            entry.append(':');
            entry.append(item.getEnchantmentLevel(ench));
            found = true;
        }

        List<String> fromConfig = treeConfig.getStringList(TreeConfig.CFG.TOOL_LIST, new ArrayList<>());
        List<String> result = new ArrayList<>(fromConfig);
        result.add(entry.toString());
        treeConfig.getYamlConfiguration().set(TreeConfig.CFG.TOOL_LIST.getNode(), result);
        treeConfig.save();
        TreeAssist.instance.sendPrefixed(player, Language.MSG.SUCCESSFUL_ADDTOOL.parse(entry.toString()));
    }

    /**
     * Remove a tool from the tree config
     *
     * @param player the player issuing the command
     * @param treeConfig the tree config to alter
     */
    public static void toolRemove(Player player, TreeConfig treeConfig) {
        ItemStack inHand = player.getInventory().getItemInMainHand();
        if (inHand.getType() == Material.AIR) {
            inHand = player.getInventory().getItemInOffHand();
        }
        if (inHand.getType() == Material.AIR) {
            TreeAssist.instance.sendPrefixed(player, Language.MSG.ERROR_EMPTY_HAND.parse());
            return;
        }

        String definition = null;

        List<String> fromConfig = treeConfig.getStringList(TreeConfig.CFG.TOOL_LIST, new ArrayList<>());

        if (fromConfig.contains(inHand.getType().name())) {
            fromConfig.remove(inHand.getType().name());
            definition = inHand.getType().name();
        } else if (fromConfig.contains(inHand.getType().getKey().toString())) {
            fromConfig.remove(inHand.getType().getKey().toString());
            definition = inHand.getType().getKey().toString();
        } else {
            for (String tool : fromConfig) {
                if (!tool.startsWith(inHand.getType().name()) && !tool.startsWith(inHand.getType().getKey().toString())) {
                    continue; // skip other names
                }

                String[] values = tool.split(":");

                if (values.length < 2) {
                    definition = tool;
                    // (found) name
                } else {

                    for (Enchantment ench : inHand.getEnchantments().keySet()) {
                        if (!values[1].contains(ench.getKey().getKey().replace(':', '~'))) {
                            continue; // skip other enchantments
                        }
                        int level = 0;
                        if (values.length >= 3) {
                            try {
                                level = Integer.parseInt(values[2]);
                            } catch (Exception e) { // invalid level defined, defaulting to no
                                definition = tool;
                                // level
                            }

                            if (level > inHand.getEnchantments().get(ench)) {
                                continue; // enchantment too low
                            }
                        }
                        definition = tool;

                    }
                }
            }
            if (definition == null) {
                TreeAssist.instance.sendPrefixed(player, Language.MSG.ERROR_REMOVETOOL_NOTDONE.parse());
                return;
            } else {
                fromConfig.remove(definition);
            }
        }

        treeConfig.getYamlConfiguration().set(TreeConfig.CFG.TOOL_LIST.getNode(), fromConfig);
        treeConfig.save();
        TreeAssist.instance.sendPrefixed(player, Language.MSG.SUCCESSFUL_REMOVETOOL.parse(definition));
    }

    public static boolean receivesDamage(TreeConfig config, ItemStack tool) {
        if (tool == null) {
            return false;
        }
        if (!config.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_APPLY_FULL_TOOL_DAMAGE)) {
            return false;
        }
        if (tool.hasItemMeta() && tool.getItemMeta() != null) {
            return !tool.getItemMeta().isUnbreakable();
        }
        return true;
    }

    public static String printTool(ItemStack tool) {
        if (tool == null) {
            return "null";
        }
        return tool.getType().name();
    }

    public static boolean willBreak(ItemStack tool, Player player) {
        if (tool != null && tool.getType().getMaxDurability() > 0 && tool.getDurability() == tool.getType().getMaxDurability()) {

            TreeStructure.debug.i("removing item: " + player.getInventory().getItemInMainHand().getType().name() +
                    " (durability " + tool.getDurability() + "==" + tool.getType().getMaxDurability());
            player.getInventory().remove(tool);
            return true;
        }
        return false;
    }

    public static int calculateDamage(TreeConfig config, ItemStack tool) {
        if (tool == null) {
            return 0;
        }
        if (tool.containsEnchantment(Enchantment.DURABILITY)) {
            int damageChance = (int) (100d / ((double) tool
                    .getEnchantmentLevel(Enchantment.DURABILITY) + 1d));

            int random = new Random().nextInt(100);

            if (random >= damageChance) {
                return 0;
            }
        }

        int ench = 100;

        if (tool.getEnchantments().containsKey(Enchantment.DURABILITY)) {
            ench = 100 / (tool.getEnchantmentLevel(Enchantment.DURABILITY) + 1);
        }

        if ((new Random()).nextInt(100) > ench) {
            return 0;
        }

        if (config.getMaterials(TreeConfig.CFG.TOOL_LIST).contains(tool.getType())) {
            return 1;
        }

        if (isVanillaTool(tool)) {
            return 2;
        }

        return 0;
    }

    public static void commitDamage(ItemStack tool, int damagePredicted) {
        if (tool == null) {
            return;
        }
        ItemMeta meta = tool.getItemMeta();
        if (meta != null) {
            ((Damageable)meta).setDamage(((Damageable)meta).getDamage() + damagePredicted);
            tool.setItemMeta(meta);
        }
    }

    public static int calculateDamage(TreeConfig config, ItemStack tool, TreeStructure tree) {
        if (tree == null || !tree.isValid()) {
            return 0;
        }

        int result = 0;

        for (int count = tree.trunk.size(); count>0; count --) {
            result += calculateDamage(config, tool);
        }

        return result;
    }

    public static boolean wouldBreak(ItemStack tool, int damage) {
        if (tool == null || tool.getItemMeta() == null) {
            return false;
        }
        if (TreeAssist.instance.getPlayerListener().isDebugTool(tool)) {
            return false;
        }
        return tool.getType().getMaxDurability() <=
                ((Damageable) tool.getItemMeta()).getDamage() + damage;
    }
}
