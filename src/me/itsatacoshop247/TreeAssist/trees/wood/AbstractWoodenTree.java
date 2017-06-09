package me.itsatacoshop247.TreeAssist.trees.wood;

import me.itsatacoshop247.TreeAssist.TreeAssistProtect;
import me.itsatacoshop247.TreeAssist.TreeAssistReplant;
import me.itsatacoshop247.TreeAssist.core.Utils;
import me.itsatacoshop247.TreeAssist.trees.AbstractGenericTree;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Tree;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractWoodenTree extends AbstractGenericTree {
    protected final List<Block> leaves = new ArrayList<>();
    private final TreeSpecies species;
    private final String destroySetting;
    private final String permissionString;
    final Material logMaterial;
    private final Material leafMaterial;

    AbstractWoodenTree(TreeSpecies species, String destroySetting, String permissionString) {
        this.species = species;
        this.destroySetting = destroySetting;
        this.permissionString = permissionString;
        if (species == TreeSpecies.DARK_OAK || species == TreeSpecies.ACACIA) {
            logMaterial = Material.LOG_2;
            leafMaterial = Material.LEAVES_2;
        } else {
            logMaterial = Material.LOG;
            leafMaterial = Material.LEAVES;
        }
    }

    protected List<Block> calculate(final Block bottom, final Block top) {
        List<Block> list = new ArrayList<Block>();
        checkBlock(list, bottom, top, true);
        list.addAll(leaves);
        return list;
    }

    public void checkBlock(List<Block> list, Block block, Block top, boolean deep) {

        //debug.i("cB " + Debugger.parse(block.getLocation()));

        if (block.getType() != logMaterial) {
//			debug.i("no log: " + block.getType().name());
            if (isLeaf(block) > 0) {
                if (!leaves.contains(block)) {
                    leaves.add(block);
//					debug.i("cB: adding leaf " + block.getY());
                }
            }
//			debug.i("out!");
            return;
        }

        Tree tree = (Tree) block.getState().getData();
        if (tree.getSpecies() != species) {
//			debug.i("cB not custom log; data wrong! " + block.getData() + "!=" + top.getData());
            return;
        }

        if (block.getRelative(0, 1, 0).getType() == logMaterial) { // might
            // be a
            // trunk
//			debug.i("trunk?");
            // one above is a tree block
            if (block.getX() != top.getX() && block.getZ() != top.getZ()) {
//				debug.i("not main!");

                if (checkFail(block)) {
                    return;
                }
            }
        }

        if (list.contains(block)) {
//			debug.i("already added!");
            return;
        } else {
//			debug.i(">>>>>>>>>> adding! <<<<<<<<<<<");
            list.add(block);
        }

        if (!deep) {
//			debug.i("not deep, out!");
            return;
        }

        for (BlockFace face : Utils.NEIGHBORFACES) {
            checkBlock(list, block.getRelative(face).getRelative(BlockFace.UP), top, false);
        }

        if (block.getY() > top.getY()) {
//			debug.i("over the top! (hah) out!");
            return;
        }
        checkBlock(list, block.getRelative(0, 1, 0), top, true);
    }

    @Override
    protected boolean checkFail(Block block) {
        int failCount = 0;
        for (int cont = -4; cont < 5; cont++) {
            if (block.getRelative(0, cont, 0).getType() == logMaterial) {
                failCount++;
            }
        }
        if (failCount > 4) {
            debug.i("fail count "+failCount+"! out!");
            return true;
        }
        return false;
    }

    @Override
    protected void debug() {
        System.out.print("Tree: "+this.getClass().getName());
        System.out.print("logMat: " + logMaterial);
        System.out.print("leafMat: " + leafMaterial);
        System.out.print("TreeSpecies: " + species);

        System.out.print("removeBlocks: " + removeBlocks.size());
        System.out.print("totalBlocks: " + totalBlocks.size());

        System.out.print("valid: " + valid);
        System.out.print("top: " + (top == null ? "null" : top.toString()));
        System.out.print("bottom: " + (bottom == null ? "null" : bottom.toString()));
    }

    @Override
    protected Block getBottom(Block block) {
        int counter = 1;
        do {
            if (block.getRelative(0, 0 - counter, 0).getType() == logMaterial) {
                counter++;
            } else {

                bottom = block.getRelative(0, 1 - counter, 0);
                if (bottom.getRelative(BlockFace.DOWN).getType() != Material.DIRT &&
                        bottom.getRelative(BlockFace.DOWN).getType() != Material.GRASS &&
                        bottom.getRelative(BlockFace.DOWN).getType() != Material.CLAY &&
                        bottom.getRelative(BlockFace.DOWN).getType() != Material.SAND) {
                    return null; // the tree is already broken.
                }
                return bottom;
            }
        } while (block.getY() - counter > 0);

        bottom = null;
        return null;
    }

    @Override
    protected Block getTop(Block block) {
        int maxY = block.getWorld().getMaxHeight() + 10;
        int counter = 1;

        while (block.getY() + counter < maxY) {
            if (block.getRelative(0, counter, 0).getType() == leafMaterial) {
                top = block.getRelative(0, counter - 1, 0);
                break;
            } else {
                counter++;
            }
        }
        return (top != null && leafCheck(top)) ? top.getRelative(0, 1, 0) : null;
    }

    @Override
    protected void handleSaplingReplace(int delay) {
        handleSaplingReplace(delay, bottom);
    }

    protected void handleSaplingReplace(int delay, Block bottom) {
        if (bottom == null) {
            //debugger.i("no null sapling !!!");
            return;
        }
        // make sure that the block is not being removed later

        removeBlocks.remove(bottom);
        totalBlocks.remove(bottom);

        Runnable b = new TreeAssistReplant(Utils.plugin, bottom, species);
        Utils.plugin.getServer()
                .getScheduler()
                .scheduleSyncDelayedTask(Utils.plugin, b,
                        20 * delay);

        if (Utils.plugin.getConfig()
                .getInt("Sapling Replant.Time to Protect Sapling (Seconds)") > 0) {
            Utils.plugin.saplingLocationList.add(bottom.getLocation());
            Runnable X = new TreeAssistProtect(Utils.plugin,
                    bottom.getLocation());

            Utils.plugin.getServer()
                    .getScheduler()
                    .scheduleSyncDelayedTask(
                            Utils.plugin,
                            X,
                            20 * Utils.plugin.getConfig()
                                    .getInt("Sapling Replant.Time to Protect Sapling (Seconds)"));
        }
    }

    @Override
    protected boolean hasPerms(Player player) {
        if (!Utils.plugin.getConfig().getBoolean("Main.Use Permissions")) {
            return true;
        }
        return player.hasPermission("treeassist.destroy."+permissionString);
    }

    @Override
    protected boolean isBottom(Block block) {
        return block.equals(bottom);
    }

    @Override
    protected int isLeaf(Block block) {
        if (block.getType() == leafMaterial) {
            return 1;
        }
        return 0;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    protected boolean willBeDestroyed() {
        return Utils.plugin.getConfig()
                .getBoolean("Automatic Tree Destruction.Tree Types."+destroySetting);
    }

    @Override
    protected boolean willReplant() {
        return Utils.replantType(species);
    }
}
