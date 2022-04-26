package net.slipcor.treeassist.discovery;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.core.TreeAssistDebugger;
import net.slipcor.treeassist.utils.BlockUtils;
import net.slipcor.treeassist.utils.CommandUtils;
import net.slipcor.treeassist.yml.MainConfig;
import net.slipcor.treeassist.yml.TreeConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;

public class DiscoveryResult {
    public static TreeAssistDebugger debug;

    final TreeConfig config;
    final TreeStructure tree;
    private boolean actions;
    private boolean replant;
    private FailReason reason = null;
    private String failExtra = null;

    private boolean cancel = false;
    private boolean valid = true;

    ItemStack item;
    int damagePredicted;

    public DiscoveryResult(TreeConfig config, TreeStructure tree, FailReason reason) {
        this.config = config;
        this.tree = tree;
        this.reason = reason;
        this.valid = false;

        this.actions = false;
    }

    public DiscoveryResult(TreeConfig config, TreeStructure tree, FailReason reason, String failInformation) {
        this(config, tree, reason);

        failExtra = failInformation;
    }

    public DiscoveryResult(TreeConfig config, TreeStructure tree, boolean cancel) {
        this.config = config;
        this.tree = tree;
        this.cancel = cancel;

        this.actions = false;
    }

    public DiscoveryResult(TreeConfig config, TreeStructure tree, boolean cancel, ItemStack item, int damagePredicted) {
        this.config = config;
        this.tree = tree;
        this.cancel = cancel;

        this.actions = true;
        this.item = item;
        this.damagePredicted = damagePredicted;
    }

    public TreeConfig getConfig() {
        return config;
    }

    public TreeStructure getTree() {
        return tree;
    }

    public boolean isCancel() {
        return cancel;
    }

    public void commitActions(Block block, Player player) {
        if (actions) {
            tree.maybeReplant(player, block);
            if (TreeAssist.instance.config().getBoolean(MainConfig.CFG.DESTRUCTION_ONLY_ABOVE)) {
                tree.removeBlocksBelow(block);
            }
            TreeAssist.instance.treeAdd(tree);
            BlockUtils.callExternals(block, player, true);
            CommandUtils.commitTree(player, config);
            tree.removeTreeLater(player, item, damagePredicted);
        }

        TreeConfig matchingTreeConfig = config;
        TreeStructure matchingTreeStructure = tree;

        if (matchingTreeConfig != null) {
            debug.i("Fallback to enforcing something!");
            if (replant || matchingTreeConfig.getBoolean(TreeConfig.CFG.REPLANTING_ENFORCE) ||
                    TreeAssist.instance.getBlockListener().isReplant(player.getName())) {
                matchingTreeStructure.maybeReplant(player, block);
            }

            if (matchingTreeConfig.getBoolean(TreeConfig.CFG.AUTOMATIC_DESTRUCTION_FORCED_REMOVAL)) {
                if (matchingTreeStructure.extras == null) {
                    matchingTreeStructure.extras = new HashSet<>();
                }
                TreeAssist.instance.treeAdd(matchingTreeStructure);
                if (TreeAssist.instance.config().getBoolean(MainConfig.CFG.DESTRUCTION_ONLY_ABOVE)) {
                    matchingTreeStructure.removeBlocksBelow(block);
                }
                BlockUtils.callExternals(block, player, true);
                CommandUtils.commitTree(player, matchingTreeConfig);
                matchingTreeStructure.removeTreeLater(null, null, 0);
            } else {
                // do we maybe need to place saplings still?
                matchingTreeStructure.plantSaplings();
            }
        }
    }

    public void debugShow(final Player player) {
        if (config == null || tree == null || !tree.isValid() ) {
            TreeAssist.instance.sendPrefixed(player, "Invalid tree!");
            if (tree != null && tree.discoveryResult != null) {
                TreeAssist.instance.sendPrefixed(player, "Reason: " +  tree.discoveryResult.reason);
                if (tree.discoveryResult.getInformation() != null) {
                    TreeAssist.instance.sendPrefixed(player, "More Info: " + tree.discoveryResult.getInformation());
                }
            }
            return;
        }

        TreeAssist.instance.sendPrefixed(player, "Showing valid tree #" + System.identityHashCode(tree));

        for (Block block : tree.trunk) {
            player.sendBlockChange(block.getLocation(), Material.BROWN_STAINED_GLASS.createBlockData());
        }

        for (Block block : tree.extras) {
            player.sendBlockChange(block.getLocation(), Material.LIME_STAINED_GLASS.createBlockData());
        }
        if (tree.branchMap != null) {
            for (List<Block> list : tree.branchMap.values()) {
                for (Block block : list) {
                    player.sendBlockChange(block.getLocation(), Material.YELLOW_STAINED_GLASS.createBlockData());
                }
            }
        }
        Bukkit.getScheduler().runTaskLater(TreeAssist.instance, new Runnable() {
            @Override
            public void run() {
                TreeAssist.instance.sendPrefixed(player, "Removing valid tree #" + System.identityHashCode(tree));

                for (Block block : tree.trunk) {
                    player.sendBlockChange(block.getLocation(), block.getType().createBlockData());
                }

                for (Block block : tree.extras) {
                    player.sendBlockChange(block.getLocation(), block.getType().createBlockData());
                }

                if (tree.branchMap != null) {
                    for (List<Block> list : tree.branchMap.values()) {
                        for (Block block : list) {
                            player.sendBlockChange(block.getLocation(), block.getType().createBlockData());
                        }
                    }
                }
            }
        }, 200L);
    }

    public FailReason getReason() {
        return reason;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public boolean isValid() {
        return this.valid;
    }

    public String getInformation() {
        return failExtra;
    }

    public ItemStack getTool() {
        return item;
    }

    public void setReason(FailReason reason) {
        this.reason = reason;
    }

    public void setOnlyReplant() {
        this.actions = false;
        this.replant = true;
    }
}
