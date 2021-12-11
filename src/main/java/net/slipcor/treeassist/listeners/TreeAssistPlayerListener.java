package net.slipcor.treeassist.listeners;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.core.TreeAssistDebugger;
import net.slipcor.treeassist.discovery.TreeStructure;
import net.slipcor.treeassist.events.TASaplingBreakEvent;
import net.slipcor.treeassist.utils.BlockUtils;
import net.slipcor.treeassist.utils.MaterialUtils;
import net.slipcor.treeassist.yml.Language;
import net.slipcor.treeassist.yml.MainConfig;
import net.slipcor.treeassist.yml.TreeConfig;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.TreeType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TreeAssistPlayerListener implements Listener {
    public static TreeAssistDebugger debug;

    public TreeAssist plugin;

    private final String debugToolDisplayName = "" + ChatColor.GREEN + ChatColor.ITALIC + "TreeAssist Debug";
    private final String growToolDisplayName = "" + ChatColor.GREEN + ChatColor.ITALIC + "TreeAssist Grow";
    private final String protectToolDisplayName = "" + ChatColor.GREEN + ChatColor.ITALIC + "TreeAssist Protect";

    public TreeAssistPlayerListener(TreeAssist instance) {
        plugin = instance;
    }

    private final static List<BlockBreakEvent> ignoring = new ArrayList<>();

    public static void ignore(BlockBreakEvent event) {
        ignoring.add(event);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (!plugin.isActive(event.getPlayer().getWorld())) {
            debug.i("not in this world: " + event.getPlayer().getWorld().getName());
            return;
        }

        if (event.hasItem() && event.hasBlock()) {
            if (this.isProtectTool(event.getPlayer().getInventory().getItemInMainHand())) {
                Block clicked = event.getClickedBlock();

                if (clicked != null && MaterialUtils.isSapling(clicked.getType())) {
                    if (plugin.saplingLocationList.contains(clicked.getLocation())) {
                        plugin.saplingLocationList.remove(clicked.getLocation());
                        TreeAssist.instance.sendPrefixed(event.getPlayer(),
                                Language.MSG.SUCCESSFUL_PROTECT_OFF.parse());
                    } else {
                        plugin.saplingLocationList.add(clicked.getLocation());
                        TreeAssist.instance.sendPrefixed(event.getPlayer(),
                                Language.MSG.SUCCESSFUL_PROTECT_ON.parse());
                    }
                }
            } else if (this.isGrowTool(event.getPlayer().getInventory().getItemInMainHand())) {
                if (event.getBlockFace().equals(BlockFace.UP)) {
                    ItemStack tool = event.getPlayer().getInventory().getItemInMainHand();
                    TreeType type = TreeType.valueOf(tool.getItemMeta().getLore().get(0));

                    Block destination = event.getClickedBlock().getRelative(BlockFace.UP);
                    for (int i=0; i< 20; i++) {
                        if (destination.getWorld().generateTree(destination.getLocation(), type)) {
                            return;
                        }
                    }

                    TreeAssist.instance.sendPrefixed(event.getPlayer(), Language.MSG.ERROR_GROW.parse());
                }
            } else if (this.isDebugTool(event.getPlayer().getInventory().getItemInMainHand())) {
                Block clicked = event.getClickedBlock();

                if (clicked != null && MaterialUtils.isLog(clicked.getType())) {
                    TreeStructure tree = TreeStructure.discover(event.getPlayer(), clicked);
                    if (tree == null) {
                        return;
                    }
                    if (tree.trunk != null) {
                        tree.trunk.add(clicked);
                    }
                    event.setCancelled(true);
                    if (tree.discoveryResult != null) {
                        tree.discoveryResult.debugShow(event.getPlayer());
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!plugin.isActive(event.getPlayer().getWorld())) {
            debug.i("not in this world: " + event.getPlayer().getWorld().getName());
            return;
        }

        if (TreeStructure.allSaplings.contains(event.getBlockReplacedState().getType())) {
            debug.i("onBlockPlace: this block was a sapling (" + event.getBlockReplacedState().getType());
            return;
        }

        if (plugin.config().getBoolean(MainConfig.CFG.PLACED_BLOCKS_ACTIVE) &&
                (TreeStructure.allTrunks.contains(event.getBlock().getType()))) {
            Block block = event.getBlock();
            plugin.blockList.addBlock(block);
            plugin.blockList.save();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (ignoring.contains(event)) {
            ignoring.remove(event);
            debug.i("skip this one!");
            return;
        }

        if (!plugin.Enabled) {
            return;
        }

        if (!plugin.isActive(event.getBlock().getWorld())) {
            debug.i("not in this world: " + event.getBlock().getWorld().getName());
            return;
        }

        if (MaterialUtils.isSapling(event.getBlock().getType())) {
            if (plugin.saplingLocationList.contains(event.getBlock().getLocation())) {
                TreeAssist.instance.sendPrefixed(event.getPlayer(), Language.MSG.INFO_SAPLING_PROTECTED.parse());
                event.setCancelled(true);
                return;
            }
            TASaplingBreakEvent saplingBreakEvent = new TASaplingBreakEvent(event.getBlock(), event.getBlock().getType());

            TreeAssist.instance.getServer().getPluginManager().callEvent(saplingBreakEvent);

            if (saplingBreakEvent.isCancelled()) {
                debug.i("Another plugin prevented sapling breaking!");
                TreeAssist.instance.sendPrefixed(event.getPlayer(), Language.MSG.INFO_SAPLING_PROTECTED.parse());
                event.setCancelled(true);
                return;
            }

            for (TreeConfig config : TreeAssist.treeConfigs.values()) {
                if (event.getBlock().getType() == config.getMaterial(TreeConfig.CFG.REPLANTING_MATERIAL)) {
                    if (config.getBoolean(TreeConfig.CFG.REPLANTING_FORCE_PROTECT)) {
                        TreeAssist.instance.sendPrefixed(event.getPlayer(), Language.MSG.INFO_NEVER_BREAK_SAPLINGS.parse());
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }

        if (!TreeStructure.allTrunks.contains(event.getBlock().getType())) {
            debug.i("Not a tree block: " + event.getBlock().getType());
            return;
        }

        if (plugin.config().getBoolean(MainConfig.CFG.PLACED_BLOCKS_ACTIVE)) {
            if (plugin.blockList.isPlayerPlaced(event.getBlock())) {
                debug.i("User placed block. Removing!");
                plugin.blockList.removeBlock(event.getBlock());
                plugin.blockList.save();
                debug.explain(TreeAssistDebugger.ErrorType.AUTOCHOP, event.getBlock(), "This is a player placed block! Not a tree to autochop!");
                debug.explain(TreeAssistDebugger.ErrorType.CLEANUP, event.getBlock(), "This is a player placed block! Not a tree to cleanup!");
                debug.explain(TreeAssistDebugger.ErrorType.SAPLING, event.getBlock(), "This is a player placed block! Not a valid tree to replant!");
                return;
            }
        }

        Player player = event.getPlayer();

        TreeStructure tree = TreeStructure.discover(player, event.getBlock());

        if (tree == null) {
            debug.explain(TreeAssistDebugger.ErrorType.AUTOCHOP, event.getBlock(), "This is not a valid tree to autochop!");
            debug.explain(TreeAssistDebugger.ErrorType.CLEANUP, event.getBlock(), "This is not a valid tree to cleanup!");
            debug.explain(TreeAssistDebugger.ErrorType.SAPLING, event.getBlock(), "This is not a valid tree to replant!");
            return;
        }

        if (tree.discoveryResult.isCancel()) {
            event.setCancelled(true);
        }

        tree.discoveryResult.commitActions(event.getBlock(), player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPickUpEvent(EntityPickupItemEvent event) {
        if (!plugin.isActive(event.getItem().getWorld())) {
            debug.i("not in this world: " + event.getItem().getWorld().getName());
            return;
        }

        debug.i("Picking up : " + event.getItem().getType() + " >> " + event.getItem().getClass());
        if (event.getItem() instanceof FallingBlock) {
            debug.i("Falling block picked up!");
            if (BlockUtils.removeIfFallen((FallingBlock) event.getItem())) {
                event.setCancelled(true);
                debug.i("Event cancelled!");
            } else {
                debug.i("Event not cancelled!");
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.config().getBoolean(MainConfig.CFG.GENERAL_TOGGLE_DEFAULT)) {
            plugin.toggleGlobal(event.getPlayer().getName());
        }
        if (event.getPlayer().isOp() && plugin.getUpdater() != null) {
            plugin.getUpdater().message(event.getPlayer());
        }
    }

    private final static Map<Player, TreeStructure> destroyers = new HashMap<>();

    public static void addDestroyer(Player player, TreeStructure tree) {
        destroyers.put(player, tree);
    }

    public static void removeDestroyer(Player player, TreeStructure tree) {
        if (player == null) {
            return;
        }
        if (tree.equals(destroyers.get(player))) {
            destroyers.remove(player);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onToolClick(InventoryClickEvent event) {
        if (!plugin.isActive(event.getWhoClicked().getWorld())) {
            debug.i("not in this world: " + event.getWhoClicked().getWorld().getName());
            return;
        }

        if (destroyers.containsKey((Player) event.getWhoClicked())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onToolSwitch(PlayerSwapHandItemsEvent event) {
        if (!plugin.isActive(event.getPlayer().getWorld())) {
            debug.i("not in this world: " + event.getPlayer().getWorld().getName());
            return;
        }

        if (destroyers.containsKey(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onToolDrop(PlayerDropItemEvent event) {
        if (!plugin.isActive(event.getPlayer().getWorld())) {
            debug.i("not in this world: " + event.getPlayer().getWorld().getName());
            return;
        }

        if (destroyers.containsKey(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

    public ItemStack getDebugTool() {
        ItemStack item = new ItemStack(Material.GOLDEN_HOE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(debugToolDisplayName);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getGrowTool(TreeType treeType) {
        ItemStack item = new ItemStack(Material.GOLDEN_HOE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(growToolDisplayName);
        ArrayList<String> lore = new ArrayList<>();
        lore.add(treeType.name());
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack getProtectionTool() {
        ItemStack item = new ItemStack(Material.GOLDEN_HOE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(protectToolDisplayName);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isDebugTool(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(debugToolDisplayName);
    }

    public boolean isGrowTool(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(growToolDisplayName);
    }

    public boolean isProtectTool(ItemStack item) {
        return item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equals(protectToolDisplayName);
    }
}
