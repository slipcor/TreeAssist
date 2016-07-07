package me.itsatacoshop247.TreeAssist.trees;

import me.itsatacoshop247.TreeAssist.TreeAssist;
import me.itsatacoshop247.TreeAssist.core.Debugger;
import me.itsatacoshop247.TreeAssist.core.Language;
import me.itsatacoshop247.TreeAssist.core.Language.MSG;
import me.itsatacoshop247.TreeAssist.core.Utils;
import me.itsatacoshop247.TreeAssist.events.TATreeBrokenEvent;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Tree;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public abstract class BaseTree {
    protected enum TreeType {
        OAK, SPRUCE, BIRCH, JUNGLE, SHROOM, CUSTOM, ONESEVEN;
    }

    public static Debugger debug;

    protected boolean valid = false;
    protected List<Block> removeBlocks = new ArrayList<Block>();
    protected List<Block> totalBlocks = new ArrayList<Block>();

    protected Block bottom;
    protected Block top;

    protected boolean fastDecaying = false;

    private static void checkAndDoSaplingProtect(Player player, Block block,
                                                 BlockBreakEvent event) {
        Material blockMat = block.getType();
        if (blockMat != Material.LOG && blockMat != Material.LOG_2
                && !CustomTree.isCustomLog(block)) {
            if (blockMat == Material.SAPLING) {
                if (Utils.plugin.getConfig().getBoolean(
                        "Sapling Replant.Block all breaking of Saplings")) {
                    player.sendMessage(
                            Language.parse(MSG.INFO_NEVER_BREAK_SAPLINGS));
                    event.setCancelled(true);
                } else if (Utils.plugin.saplingLocationList.contains(block
                        .getLocation())) {
                    if (player.getGameMode() == GameMode.CREATIVE) {
                        Utils.plugin.saplingLocationList.remove(block
                                .getLocation());
                        return;
                    }
                    player.sendMessage(Language.parse(MSG.INFO_SAPLING_PROTECTED));
                    event.setCancelled(true);
                }
            } else if (blockMat == Material.GRASS || blockMat == Material.DIRT
                    || blockMat == Material.CLAY) {
                if (Utils.plugin.saplingLocationList.contains(block
                        .getRelative(BlockFace.UP, 1).getLocation())) {
                    if (player.getGameMode() == GameMode.CREATIVE) {
                        Utils.plugin.saplingLocationList.remove(block
                                .getRelative(BlockFace.UP, 1).getLocation());
                        return;
                    }

                    player.sendMessage(Language.parse(MSG.INFO_SAPLING_PROTECTED));
                    event.setCancelled(true);
                }
            } else if (blockMat != Material.SAPLING
                    && Utils.plugin.saplingLocationList.contains(block
                    .getLocation())) {
                Utils.plugin.saplingLocationList.remove(block.getLocation());
            }
        }
    }

    private static BaseTree getTreeByBlockBreakEvent(BlockBreakEvent event) {

        Block block = event.getBlock();
        TreeType type = getTreeTypeByBlock(block);
        if (type == null) {
            checkAndDoSaplingProtect(event.getPlayer(), block, event);
            return null;
        }

        debug.i(type.name());

        switch (type) {
            case OAK:
            case BIRCH:
            case SPRUCE:
            case JUNGLE:
                Tree tree = (Tree) block.getState().getData();
                return new VanillaTree(tree.getSpecies());
            case ONESEVEN:
                Tree tree2 = (Tree) block.getState().getData();
                return new VanillaOneSevenTree(tree2.getSpecies());
            case SHROOM:
                return new MushroomTree(block.getType());
            case CUSTOM:
                return new CustomTree(block.getType(), block.getData());
            default:
                return null;
        }
    }

    private static TreeType getTreeTypeByBlock(Block block) {
        if (block.getType() == Material.LOG) {
            switch (block.getData()) {
                case 0:
                case 1:
                case 2:
                case 3:
                    return TreeType.values()[block.getData()];
                default:
                    return null;
            }
        } else if (block.getType() == Material.LOG_2) {
            return TreeType.ONESEVEN;
        } else if (CustomTree.isCustomLog(block)) {
            return TreeType.CUSTOM;
        }
        switch (block.getType()) {

            case HUGE_MUSHROOM_1:
            case HUGE_MUSHROOM_2:
                return TreeType.SHROOM;
            default:
                return null;
        }
    }

    public static BaseTree calculate(BlockBreakEvent event) {

        debug.i("calculating " + event.getBlock().getLocation().toString());

        TreeAssist plugin = Utils.plugin;

        if (!plugin.isActive(event.getPlayer().getWorld())) {
            return new InvalidTree();
        }
        debug.i("BlockBreak!");

        BaseTree resultTree = getTreeByBlockBreakEvent(event);

        if (resultTree == null) {
            debug.i("getTreeByBlockBreakEvent == null");
            return new InvalidTree(); // not a tree block!
        }

        if (resultTree.isValid()) {
            debug.i("already know it!");
            return resultTree;
        }

        Block block = event.getBlock();

        if (!plugin.getConfig().getBoolean("Main.Ignore User Placed Blocks")) {
            if (plugin.blockList.isPlayerPlaced(block)) {
                debug.i("placed blocks. Removing!");
                plugin.blockList.removeBlock(block);
                plugin.blockList.save();
                return new InvalidTree(); // placed block. ignore!
            }

        }

        Player player = event.getPlayer();

        if (!resultTree.hasPerms(player)) {
            debug.i("No permission!");
            if (plugin.getConfig().getBoolean("Sapling Replant.Enforce")) {
                maybeReplant(plugin, event, resultTree, player, block);
            }
            if (plugin.isForceAutoDestroy()) {
                resultTree.findYourBlocks(block);
                debug.i("But still, remove later, maybe");
                if (resultTree.isValid()) {
                    resultTree.removeLater();
                    debug.i("Not maybe. For sure!");
                }
                return resultTree;
            }
            return new InvalidTree();
        }

        final String lore = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("Automatic Tree Destruction.Required Lore", ""));

        if (!"".equals(lore)) {
            debug.i("Lore needed!");
            ItemStack item = player.getItemInHand();
            if (item == null || !item.hasItemMeta() || !item.getItemMeta().hasLore() || !item.getItemMeta().getLore().contains(lore)) {
                debug.i("Lore not found: " + lore);


                if (plugin.getConfig().getBoolean("Sapling Replant.Enforce")) {
                    maybeReplant(plugin, event, resultTree, player, block);
                }
                if (plugin.isForceAutoDestroy()) {
                    resultTree.findYourBlocks(block);
                    debug.i("But still, remove later, maybe");
                    if (resultTree.isValid()) {
                        resultTree.removeLater();
                        debug.i("Not maybe. For sure!");
                    }
                    return resultTree;
                }
                return new InvalidTree();
            }
        }


        if (!plugin.getConfig().getBoolean("Automatic Tree Destruction.When Sneaking")) {
            if (event.getPlayer().isSneaking()) {
                debug.i("Sneak prevention!");
                if (plugin.getConfig().getBoolean("Sapling Replant.Enforce")) {
                    maybeReplant(plugin, event, resultTree, player, block);
                }
                if (plugin.isForceAutoDestroy()) {
                    resultTree.findYourBlocks(block);
                    debug.i("But still, remove later, maybe");
                    if (resultTree.isValid()) {
                        resultTree.removeLater();
                        debug.i("Not maybe. For sure!");
                    }
                    return resultTree;
                }
                return new InvalidTree();
            }
        }

        if (Utils.plugin.hasCoolDown(player)) {
            debug.i("Cooldown!");
            if (plugin.getConfig().getBoolean("Sapling Replant.Enforce")) {
                maybeReplant(plugin, event, resultTree, player, block);
            }
            player.sendMessage(Language.parse(MSG.INFO_COOLDOWN_STILL));
            player.sendMessage(Language.parse(MSG.INFO_COOLDOWN_VALUE, String.valueOf(Utils.plugin.getCoolDown(player))));
            if (plugin.isForceAutoDestroy()) {
                resultTree.findYourBlocks(block);
                debug.i("But still, remove later, maybe");
                if (resultTree.isValid()) {
                    resultTree.removeLater();
                    debug.i("Not maybe. For sure!");
                }
                return resultTree;
            }
            return new InvalidTree();
        }

        resultTree.bottom = block;
        resultTree.top = block;
        if (Utils.mcMMOTreeFeller(player)) {
            debug.i("MCMMO Tree Feller!");
            maybeReplant(plugin, event, resultTree, player, block);
            if (plugin.isForceAutoDestroy()) {
                resultTree.findYourBlocks(block);
                debug.i("But still, remove later, maybe");
                if (resultTree.isValid()) {
                    resultTree.removeLater();
                    debug.i("Not maybe. For sure!");
                }
            }
            return resultTree;
        }

        if (!plugin.getConfig().getBoolean("Main.Destroy Only Blocks Above")) {
            resultTree.bottom = resultTree.getBottom(block);
        }
        resultTree.top = resultTree.getTop(block);
        if (resultTree.bottom == null) {
            debug.i("bottom is null!");
            return new InvalidTree();// not a valid tree
        }

        if (plugin.getConfig().getBoolean("Main.Automatic Tree Destruction")) {
            if (resultTree.top == null) {
                debug.i("and not a tree anyways...");
                return new InvalidTree(); // not a valid tree
            }
            if (resultTree.top.getY() - resultTree.bottom.getY() < 3) {
                debug.i("and too short anyways...");
                return new InvalidTree(); // not a valid tree
            }
        }
        resultTree.getTrunks();

        boolean success = false;
        boolean damage = false;

        if (!event.isCancelled()
                && plugin.getConfig().getBoolean(
                "Main.Automatic Tree Destruction")) {
            if (plugin.getConfig().getBoolean(
                    "Tools.Tree Destruction Require Tools")) {
                if (!Utils.isRequiredTool(player.getItemInHand())) {
                    debug.i("Player has not the right tool!");
                    if (plugin.isForceAutoDestroy()) {
                        resultTree.findYourBlocks(block);
                        debug.i("But still, remove later, maybe");
                        if (resultTree.isValid()) {
                            resultTree.removeLater();
                            debug.i("Not maybe. For sure!");
                        }
                        return resultTree;
                    }
                    return new InvalidTree();
                }
            }

            String[] directions = {"NORTH", "SOUTH", "EAST", "WEST",
                    "NORTH_EAST", "NORTH_WEST", "SOUTH_EAST", "SOUTH_WEST"};

            for (int x = 0; x < directions.length; x++) {
                if (!Utils.validTypes.contains(block.getRelative(
                        BlockFace.valueOf(directions[x])).getTypeId())) {
                    if (!((block.getRelative(BlockFace.valueOf(directions[x]))
                            .getType() == Material.LOG && (block.getData() == 1 || block
                            .getData() == 3)) || (block
                            .getRelative(BlockFace.valueOf(directions[x]))
                            .getType() == Material.LOG_2 && block
                            .getData() == 1))) {
                        debug.i("invalid because of invalid type: "
                                + block.getRelative(
                                BlockFace.valueOf(directions[x]))
                                .getType() + ":" + block.getData());
                        return new InvalidTree(); // not a valid tree
                    }
                }
            }

            if (!plugin.isDisabled(player.getWorld().getName(), player.getName())) {
                success = resultTree.willBeDestroyed();
                damage = plugin.getConfig().getBoolean(
                        "Main.Apply Full Tool Damage");
            }
        }
        if (success) {
            debug.i("success!");

            debug.i("replant perms?");

            BaseTree tree = maybeReplant(plugin, event, resultTree, player,
                    block);
            if (tree != null && !(tree instanceof InvalidTree)) {
                return tree;
            }

            if (!Utils.plugin.getConfig().getBoolean("Modding.Disable Durability Fix")) {

                int durability = player.getItemInHand().getDurability();
                int maxDurability = player.getItemInHand().getType().getMaxDurability();

                if (((durability > maxDurability) || player.getItemInHand().getDurability() < 0)
                                && Utils.isVanillaTool(player.getItemInHand())) {
                    debug.i("removing item: " + player.getItemInHand().getType().name() +
                            " (durability " + durability + ">" + maxDurability);
                    player.setItemInHand(new ItemStack(Material.AIR));
                }
            }
            resultTree.findYourBlocks(block);
            if (resultTree.isValid()) {
                debug.i("removing...");
                resultTree.removeLater(player, damage, player.getItemInHand());
                return resultTree;
            }
            debug.i("... but invalid -.-");
            return new InvalidTree();
        }

        debug.i("no success!");

        BaseTree tree = maybeReplant(plugin, event, resultTree, player, block);
        if (tree != null) {
            return tree;
        }
        if (plugin.isForceAutoDestroy()) {
            resultTree.findYourBlocks(block);
            debug.i("But still, remove later, maybe");
            if (resultTree.isValid()) {
                resultTree.removeLater();
                debug.i("Not maybe. For sure!");
            }
            return resultTree;
        }
        return new InvalidTree();
    }

    private static BaseTree maybeReplant(TreeAssist plugin,
                                         BlockBreakEvent event, BaseTree resultTree, Player player,
                                         Block block) {
        if (resultTree.top == null) {
            return new InvalidTree();
        }

        if (!resultTree.isBottom(block)) {
            block = resultTree.bottom;
            if (block == null) {
                //plugin.getLogger().warning("Block null!");
                return new InvalidTree();
            }
        }

        Material below = block.getRelative(BlockFace.DOWN).getType();
        if (!(below == Material.DIRT || below == Material.GRASS || below == Material.CLAY)) {
            return resultTree;
        }

        if (plugin.getConfig().getBoolean("Main.Sapling Replant")
                && !event.isCancelled() && (resultTree.willReplant()) &&
                !plugin.getListener().isNoReplace(player.getName())) {

            if (!plugin.getConfig().getBoolean("Main.Use Permissions")
                    || player.hasPermission("treeassist.replant")) {

                debug.i("replant perms ok!");

                if (plugin.getConfig().getBoolean(
                        "Tools.Sapling Replant Require Tools")) {
                    if (!Utils.isRequiredTool(player.getItemInHand())) {
                        if (plugin.isForceAutoDestroy()) {
                            resultTree.findYourBlocks(block);
                            if (resultTree.isValid()) {
                                resultTree.removeLater();
                            }
                            return resultTree;
                        }
                        debug.i("no sapling without tool");
                        return new InvalidTree();
                    }
                }
                int delay = plugin
                        .getConfig()
                        .getInt("Sapling Replant.Delay until Sapling is replanted (seconds) (minimum 1 second)");
                if (delay < 1) {
                    delay = 1;
                }
                if (resultTree.isBottom(block)) {
                    // block is bottom
                    resultTree.handleSaplingReplace(delay);
                } else if (!plugin.getConfig().getBoolean(
                        "Sapling Replant.Bottom Block has to be Broken First")) {
                    // block is not bottom, but not needed
                    resultTree.handleSaplingReplace(delay);
                } // else: no sapling, because bottom block was needed and wasnt
                // destroyed
                else {

                    debug.i("not the needed bottom!");
                }
            }
        }
        return null;
    }

    abstract protected void debug();

    abstract protected boolean isBottom(Block block);

    abstract protected List<Block> calculate(Block bottom, Block top);

    abstract protected boolean checkFail(Block block);

    abstract protected Block getBottom(Block block);

    abstract protected Block getTop(Block block);

    abstract protected void getTrunks();

    abstract protected void handleSaplingReplace(int delay);

    abstract protected boolean hasPerms(Player player);

    abstract protected int isLeaf(Block block);

    abstract protected boolean willBeDestroyed();

    abstract protected boolean willReplant();

    abstract public boolean isValid();

    /**
     * Break a block and apply damage to the tool
     *
     * @param block  the block to break
     * @param tool   the item held to break with
     * @param player the breaking player
     */
    private void breakBlock(final Block block, final ItemStack tool,
                            final Player player) {
    	
    	if ((tool != null) && (tool.getDurability() > tool.getType().getMaxDurability())) return;

    	TATreeBrokenEvent event = new TATreeBrokenEvent(block, player, tool);
    	Utils.plugin.getServer().getPluginManager().callEvent(event);
    	if (event.isCancelled()) return;
    	
        boolean leaf = isLeaf(block) > 0;
        Material maat = block.getType();
        byte data = block.getState().getData().getData();

        debug.i("breaking. leaf: " + leaf);
        Tree tree;

        if (block.getState().getData() instanceof Tree) {
            tree = (Tree) block.getState().getData();
        } else {
            tree = null;
        }
        if (!leaf && Utils.plugin.mcMMO && player != null) {
            debug.i("Adding mcMMO EXP!");
            Utils.mcMMOaddExp(player, block);
        } else if (!leaf) {
            debug.i("mat: " + maat.name());
            debug.i("data: " + data);
            debug.i("mcMMO: " + Utils.plugin.mcMMO);
            debug.i("player: " + String.valueOf(player));
        }

        int chance = 100;

        if (tool != null && !leaf) {
            chance = Utils.plugin.getConfig().getInt(
                    "Tools.Drop Chance." + tool.getType().name(), 100);
            if (chance < 1) {
                chance = 1;
            }
        }

        if (chance > 99 || (new Random()).nextInt(100) < chance) {
            Utils.plugin.blockList.logBreak(block, player);
            block.breakNaturally(tool);
            player.sendBlockChange(block.getLocation(), Material.AIR, (byte) 0);

            if (leaf) {
                ConfigurationSection cs = Utils.plugin.getConfig()
                        .getConfigurationSection("Custom Drops");

                debug.i("custom drop count: " + cs.getKeys(false).size());

                for (String key : cs.getKeys(false)) {
                    int customChance = (int) (cs.getDouble(key, 0.0d) * 100000d);

                    if ((new Random()).nextInt(100000) < customChance) {
                        debug.i("dropping: " + key);
                        if (key.equalsIgnoreCase("LEAVES") && tree != null) {
                            debug.i(">1 : " + data);
                            block.getWorld().dropItemNaturally(
                                    block.getLocation(),
                                    new ItemStack(Material.LEAVES, 1, tree.getSpecies().getData()));
                        } else {
                            try {
                                Material mat = Material.valueOf(key
                                        .toUpperCase());
                                debug.i(">2 : " + mat.name());
                                block.getWorld()
                                        .dropItemNaturally(block.getLocation(),
                                                new ItemStack(mat));
                            } catch (Exception e) {
                                Utils.plugin.getLogger().warning(
                                        "Invalid config value: Custom Drops."
                                                + key
                                                + " is not a valid Material!");
                            }
                        }
                    }
                }
            } else {
                debug.i("mat: " + maat.name());
                debug.i("data: " + data);
            }
        } else {
            block.setType(Material.AIR);
            player.sendBlockChange(block.getLocation(), Material.AIR, (byte) 0);
        }

        if (!leaf && tool != null && player != null) {
            if (tool.containsEnchantment(Enchantment.DURABILITY)) {
                int damageChance = (int) (100d / ((double) tool
                        .getEnchantmentLevel(Enchantment.DURABILITY) + 1d));

                int random = new Random().nextInt(100);

                if (random >= damageChance) {
                    return; // nodamage -> out!
                }
            }

            int ench = 100;

            if (tool.getEnchantments().containsKey(Enchantment.DURABILITY)) {
                ench = 100 / (tool.getEnchantmentLevel(Enchantment.DURABILITY) + 1);
            }

            if ((new Random()).nextInt(100) > ench) {
                return; // no damage
            }

            if (Utils.toolgood.contains(tool.getTypeId())) {
                tool.setDurability((short) (tool.getDurability() + 1));
            } else if (Utils.toolbad.contains(tool.getTypeId())) {
                tool.setDurability((short) (tool.getDurability() + 2));
            }
        }
    }

    /**
     * remove the blocks being removed from the total blocks list
     *
     * @param removeBlocks the blocks being removed
     * @param totalBlocks  all blocks
     */
    private void removeRemovals(List<Block> removeBlocks,
                                List<Block> totalBlocks) {
        for (Block block : removeBlocks) {
            totalBlocks.remove(block);
        }
    }

    protected void findYourBlocks(Block block) {
        bottom = getBottom(block);
        top = getTop(block);

        totalBlocks = new ArrayList<Block>();

        if (bottom == null) {
            debug.i("bottom null!");
            removeBlocks = new ArrayList<Block>();
            return;
        }
        if (top == null) {
            debug.i("top null!");
            removeBlocks = new ArrayList<Block>();
            return;
        }
        valid = true;
    }

    protected boolean leafCheck(final Block block) {
        int total = 0;

        debug.i("lC: " + block.getX() + "/" + block.getY() + "/" + block.getZ());

        for (int x = -2; x < 3; x++) {
            for (int z = -2; z < 3; z++) {
                for (int y = -1; y < 1; y++) {
                    total += isLeaf(block.getRelative(x, y, z));
                }
            }
            if (total > 3) {
                debug.i("lC inner true");
                return true;
            }
        }

        debug.i("lC final " + (total > 3));
        return total > 3;
    }

    protected void removeLater() {
        removeBlocks = calculate(bottom, top);

        final int delay = Utils.plugin.getConfig().getBoolean("Main.Initial Delay") ? Utils.plugin.getConfig().getInt(
                "Automatic Tree Destruction.Initial Delay (seconds)") * 20 : 0;
        final int offset = Utils.plugin.getConfig().getInt(
                "Automatic Tree Destruction.Delay (ticks)");

        class RemoveRunner extends BukkitRunnable {
            private final BaseTree me;

            RemoveRunner(BaseTree tree) {
                me = tree;
            }

            @Override
            public void run() {
                for (Block block : removeBlocks) {
                    if (block.getType() == Material.SAPLING || block.getType() == Material.BROWN_MUSHROOM
                            || block.getType() == Material.RED_MUSHROOM) {
                        debug.i("removeLater: skip breaking sapling");
                    } else if (!Utils.plugin.getConfig().getBoolean(
                            "Automatic Tree Destruction.Remove Leaves") &&
                            isLeaf(block) == 1) {
                        debug.i("removeLater: skip breaking leaf");
                    } else {
                        if (!fastDecaying && isLeaf(block) == 1) {
                            Utils.plugin.getListener().breakRadiusIfLeaf(block);
                            fastDecaying = true;
                        }
                        TATreeBrokenEvent event = new TATreeBrokenEvent(block, null, null);
                        Utils.plugin.getServer().getPluginManager().callEvent(event);
                        if (!event.isCancelled())
                        {
                        	Utils.plugin.blockList.logBreak(block, null);
                        	block.breakNaturally();
                        }
                    }
                    removeBlocks.remove(block);
                    return;

                }
                me.valid = false;
                try {
                    this.cancel();
                } catch (Exception e) {

                }
            }

        }

        (new RemoveRunner(this)).runTaskTimer(Utils.plugin, delay, offset + 1);
    }

    protected void removeLater(final Player player, final boolean damage,
                               final ItemStack playerTool) {
        if (!valid) {
            (new Exception("invalid tree!!")).printStackTrace();
            return;
        }

        debug.i("valid tree! removing!");

        // valid tree, first calculate all blocks to remove
        if (removeBlocks.size() == 0) {
            removeBlocks = calculate(bottom, top);
            debug.i("recalculated tree of size: " + removeBlocks.size());
            removeBlocks.remove(bottom);
        }
        removeBlocks.remove(bottom);

        debug.i("size: " + removeBlocks.size());
        debug.i("from: " + bottom.getY());
        debug.i("to: " + top.getY());

        if (totalBlocks.size() == 0) {
            totalBlocks = (bottom == getBottom(bottom)) ? new ArrayList<Block>()
                    : calculate(getBottom(bottom), top);
        }

        if (totalBlocks.size() > 1) {
            removeRemovals(removeBlocks, totalBlocks);
        }

        final int delay = Utils.plugin.getConfig().getBoolean("Main.Initial Delay") ? Utils.plugin.getConfig().getInt(
                "Automatic Tree Destruction.Initial Delay (seconds)") * 20 : 0;
        final int offset = Utils.plugin.getConfig().getInt(
                "Automatic Tree Destruction.Delay (ticks)");

        final ItemStack tool = (damage && player.getGameMode() != GameMode.CREATIVE) ? playerTool
                : null;

        Utils.plugin.setCoolDown(player, this);

        class InstantRunner extends BukkitRunnable {

            @Override
            public void run() {
                if (offset < 0) {
                    for (Block block : removeBlocks) {
                        if (block.getType() == Material.SAPLING || block.getType() == Material.BROWN_MUSHROOM
                                || block.getType() == Material.RED_MUSHROOM) {
                            debug.i("InstantRunner: skipping breaking a sapling");
                            continue;
                        } else if (!Utils.plugin.getConfig().getBoolean(
                                "Automatic Tree Destruction.Remove Leaves") &&
                                isLeaf(block) == 1) {
                            debug.i("InstantRunner: skip breaking leaf");
                            continue;
                        }
                        if (!fastDecaying && isLeaf(block) == 1) {
                            Utils.plugin.getListener().breakRadiusIfLeaf(block);
                            fastDecaying = true;
                        }
                        if (tool == null) {
                        	TATreeBrokenEvent event = new TATreeBrokenEvent(block, player, tool);
                        	Utils.plugin.getServer().getPluginManager().callEvent(event);
                        	if (!event.isCancelled())
                        	{
                        		Utils.plugin.blockList.logBreak(block, player);
                        		block.breakNaturally();
                                player.sendBlockChange(block.getLocation(), Material.AIR, (byte) 0);
                            }
                        } else {
                            debug.i("InstantRunner: 1");
                            breakBlock(block, tool, player);
                            if (tool.getType().getMaxDurability() > 0 && tool.getDurability() == tool.getType().getMaxDurability()) {

                                debug.i("removing item: " + player.getItemInHand().getType().name() +
                                        " (durability " + tool.getDurability() + "==" + tool.getType().getMaxDurability());
                                player.getInventory().remove(tool);
                                this.cancel();
                            }
                        }
                    }
                    removeBlocks.clear();
                } else {
                    for (Block block : removeBlocks) {
                        if (block.getType() == Material.SAPLING || block.getType() == Material.BROWN_MUSHROOM
                                || block.getType() == Material.RED_MUSHROOM) {
                            debug.i("InstantRunner: skipping breaking a sapling");
                            continue;
                        } else if (!Utils.plugin.getConfig().getBoolean(
                                "Automatic Tree Destruction.Remove Leaves") &&
                                isLeaf(block) == 1) {
                            debug.i("InstantRunner: skip breaking leaf");
                            continue;
                        }
                        if (tool == null) {
                        	TATreeBrokenEvent event = new TATreeBrokenEvent(block, player, tool);
                        	Utils.plugin.getServer().getPluginManager().callEvent(event);
                        	if (!event.isCancelled())
                        	{
                        		Utils.plugin.blockList.logBreak(block, player);
                        		block.breakNaturally();
                                player.sendBlockChange(block.getLocation(), Material.AIR, (byte) 0);
                            }
                        } else {
                            debug.i("InstantRunner: 2");
                            breakBlock(block, tool, player);
                            if (tool.getType().getMaxDurability() > 0 && tool.getDurability() == tool.getType().getMaxDurability()) {
                                debug.i("removing item: " + player.getItemInHand().getType().name() +
                                        " (durability " + tool.getDurability() + "==" + tool.getType().getMaxDurability());
                                player.getInventory().remove(tool);
                                this.cancel();
                            }
                        }
                        removeBlocks.remove(block);
                        return;
                    }
                }
                try {
                    this.cancel();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        (new InstantRunner()).runTaskTimer(Utils.plugin, delay, offset);

        class CleanRunner extends BukkitRunnable {
            private final BaseTree me;

            CleanRunner(BaseTree tree) {
                me = tree;
            }

            @Override
            public void run() {
                if (offset < 0) {
                    for (Block block : totalBlocks) {
                        if (block.getType() == Material.SAPLING || block.getType() == Material.BROWN_MUSHROOM
                                || block.getType() == Material.RED_MUSHROOM) {
                            debug.i("CleanRunner: skipping breaking a sapling");
                            continue;
                        } else if (!Utils.plugin.getConfig().getBoolean(
                                "Automatic Tree Destruction.Remove Leaves") &&
                                isLeaf(block) == 1) {
                            debug.i("CleanRunner: skip breaking leaf");
                            continue;
                        }
                        if (!fastDecaying && isLeaf(block) == 1) {
                            Utils.plugin.getListener().breakRadiusIfLeaf(block);
                            fastDecaying = true;
                        }
                        debug.i("CleanRunner: 1");
                        breakBlock(block, null, null);
                    }
                    removeBlocks.clear();
                } else {
                    for (Block block : totalBlocks) {
                        if (!fastDecaying && isLeaf(block) == 1) {
                            Utils.plugin.getListener().breakRadiusIfLeaf(block);
                            fastDecaying = true;
                        }
                        if (block.getType() == Material.SAPLING || block.getType() == Material.BROWN_MUSHROOM
                                || block.getType() == Material.RED_MUSHROOM) {
                            debug.i("CleanRunner: skipping breaking a sapling");
                            continue;
                        }
                        debug.i("CleanRunner: 2");
                        breakBlock(block, null, null);
                        totalBlocks.remove(block);
                        return;
                    }
                }

                me.valid = false;
                try {
                    this.cancel();
                } catch (Exception e) {

                }
            }

        }

        (new CleanRunner(this)).runTaskTimer(Utils.plugin, delay + 200L, offset);
    }

    public boolean contains(Block block) {

        List<Block> myRemoveBlocks = new ArrayList<Block>();
        for (Block iBlock : removeBlocks) {
            myRemoveBlocks.add(iBlock);
        }

        Iterator<Block> i = myRemoveBlocks.iterator();
        try {
            while (i.hasNext()) {

                Block b = i.next();
                if (block.getType() == Material.AIR ||
                        block.getType() == Material.SAPLING || block.getType() == Material.BROWN_MUSHROOM
                        || block.getType() == Material.RED_MUSHROOM) {
                    removeBlocks.remove(b);
                }
            }
        } catch (ConcurrentModificationException cme) {

        }
        myRemoveBlocks = new ArrayList<Block>();
        for (Block iBlock : totalBlocks) {
            myRemoveBlocks.add(iBlock);
        }

        i = myRemoveBlocks.iterator();
        while (i.hasNext()) {

            Block b = i.next();
            if (block.getType() == Material.AIR ||
                    block.getType() == Material.SAPLING || block.getType() == Material.BROWN_MUSHROOM
                    || block.getType() == Material.RED_MUSHROOM) {
                totalBlocks.remove(b);
            }
        }
        if (removeBlocks.size() < 1 && totalBlocks.size() < 1) {
            this.valid = false;
            return false;
        }


        return removeBlocks.contains(block) || totalBlocks.contains(block);
    }

    /**
     * thanks to filbert66 for this determination method!
     *
     * @param tool the itemstack being used
     * @return the seconds that it will take to destroy
     */
    public int calculateCooldown(ItemStack tool) {

        Material element = (tool != null ? tool.getType() : null);

        float singleTime;

        switch (element) {
            case GOLD_AXE:
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
            case WOOD_AXE:
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
            debug.i("tool efficiency factor: " + efficiencyFactor);
        }

        int numLogs = 0;
        for (Block b : removeBlocks) {
            if (isLeaf(b) > 0) {
                numLogs++;
            }
        }

        debug.i("breakTime (" + removeBlocks.size() + " blocks): " + numLogs
                * singleTime * efficiencyFactor);

        return (int) (numLogs * singleTime * efficiencyFactor);
    }

}
