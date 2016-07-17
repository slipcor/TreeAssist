package me.itsatacoshop247.TreeAssist.trees;

import me.itsatacoshop247.TreeAssist.TreeAssistProtect;
import me.itsatacoshop247.TreeAssist.TreeAssistReplant;
import me.itsatacoshop247.TreeAssist.core.Debugger;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.material.Tree;

import java.util.ArrayList;
import java.util.List;

public class VanillaTree extends BaseTree implements INormalTree {
    public static Debugger debugger;
    private final TreeSpecies species;
    Block[] bottoms = null;

    public VanillaTree(TreeSpecies species) {
        this.species = species;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    protected boolean hasPerms(Player player) {
        if (!Utils.plugin.getConfig().getBoolean("Main.Use Permissions")) {
            return true;
        }
        if (species == TreeSpecies.GENERIC) {
            return player.hasPermission("treeassist.destroy.oak");
        }
        if (species == TreeSpecies.REDWOOD) {
            return player.hasPermission("treeassist.destroy.spruce");
        }
        if (species == TreeSpecies.BIRCH) {
            return player.hasPermission("treeassist.destroy.birch");
        }
        if (species == TreeSpecies.JUNGLE) {
            return player.hasPermission("treeassist.destroy.jungle");
        }
        return false;
    }

    @Override
    protected Block getBottom(Block block) {
        int counter = 1;
        do {
            if (block.getRelative(0, 0 - counter, 0).getType() == Material.LOG) {
                counter++;
            } else {

                bottom = block.getRelative(0, 1 - counter, 0);
                if (bottom.getRelative(BlockFace.DOWN).getType() != Material.DIRT &&
                        bottom.getRelative(BlockFace.DOWN).getType() != Material.GRASS &&
                        bottom.getRelative(BlockFace.DOWN).getType() != Material.CLAY) {
                    return null; // the tree is already broken.
                }
                return bottom;
            }
        } while (block.getY() - counter > 0);

        bottom = null;
        return bottom;
    }

    @Override
    protected Block getTop(Block block) {
        int maxY = block.getWorld().getMaxHeight() + 10;
        int counter = 1;

        while (block.getY() + counter < maxY) {
            if (block.getRelative(0, counter, 0).getType() == Material.LEAVES) {
                top = block.getRelative(0, counter - 1, 0);
                break;
            } else {
                counter++;
            }
        }
        return (top != null && leafCheck(top)) ? top.getRelative(0, 1, 0) : null;
    }

    @Override
    protected List<Block> calculate(final Block bottom, final Block top) {
        List<Block> list = new ArrayList<Block>();
        checkBlock(list, bottom, top, true);
        return list;
    }

    @Override
    protected int isLeaf(Block block) {
        if (block.getType() == Material.LEAVES) {
            return 1;
        }
        return 0;
    }

    @Override
    protected void getTrunks() {
        bottoms = new Block[4];
        bottoms[0] = bottom;
        int j = 1;

        if (bottom == null) {
            return;
        }

        for (BlockFace face : Utils.NEIGHBORFACES) {
            if (bottom.getRelative(face).getType() == Material.LOG && j < 4) {
                bottoms[j] = bottom.getRelative(face);
                j++;
            }
            if (j == 4) {
                break;
            }
        }
    }

    @Override
    protected boolean willBeDestroyed() {
        switch (species) {
            case GENERIC:
                return Utils.plugin.getConfig()
                        .getBoolean("Automatic Tree Destruction.Tree Types.Oak");
            case REDWOOD:
                return Utils.plugin.getConfig()
                        .getBoolean("Automatic Tree Destruction.Tree Types.Spruce");
            case BIRCH:
                return Utils.plugin.getConfig()
                        .getBoolean("Automatic Tree Destruction.Tree Types.Birch");
            case JUNGLE:
                return Utils.plugin.getConfig()
                        .getBoolean("Automatic Tree Destruction.Tree Types.Jungle");
            default:
                return true; // ugly branch messes
        }
    }

    @Override
    protected boolean willReplant() {
        return Utils.replantType(species);
    }

    @Override
    protected void handleSaplingReplace(int delay) {
        if (bottoms != null) {
            if (species == TreeSpecies.JUNGLE && !Utils.plugin.getConfig().getBoolean(
                    "Sapling Replant.Tree Types to Replant.BigJungle")) {
                debugger.i("no big jungle sapling !!!");
                return;
            }
            if (species == TreeSpecies.REDWOOD && !Utils.plugin.getConfig().getBoolean(
                    "Sapling Replant.Tree Types to Replant.BigSpruce")) {
                debugger.i("no big spruce sapling !!!");
                return;
            }
            if (species == TreeSpecies.BIRCH && !Utils.plugin.getConfig().getBoolean(
                    "Sapling Replant.Tree Types to Replant.BigBirch")) {
                debugger.i("no big birch sapling !!!");
                return;
            }
            if (species == TreeSpecies.GENERIC && !Utils.plugin.getConfig().getBoolean(
                    "Sapling Replant.Tree Types to Replant.BigOak")) {
                debugger.i("no big oak sapling !!!");
                return;
            }
            for (Block bottom : bottoms) {
                replaceSapling(delay, bottom);
                debugger.i("go !!!");
            }
        }
        replaceSapling(delay, bottom);
    }

    private void replaceSapling(int delay, Block bottom) {
        if (bottom == null) {
            debugger.i("no null sapling !!!");
            return;
        }
        // make sure that the block is not being removed later

        if (bottoms != null) {
            for (Block b : bottoms) {
                removeBlocks.remove(b);
                totalBlocks.remove(b);
            }
        }

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
    public void checkBlock(List<Block> list, Block block,
                           Block top, boolean deep) {

//		debug.i("cB " + Debugger.parse(block.getLocation()));
        if (block.getType() != Material.LOG) {
//			debug.i("no log: " + block.getType().name());
            if (isLeaf(block) > 0) {
                if (!list.contains(block)) {
                    list.add(block);
//					debug.i("cB: adding leaf " + block.getY());
                }
            }
//			debug.i("out!");
            return;
        }

        Tree tree = (Tree) block.getState().getData();
        if (tree.getSpecies() != species) {
//			debug.i("cB not custom log; data wrong! " + block.getData() + "!=" + top.getData());
            if (top.getData() != 0 || block.getData() <= 3) {
//				debug.i("out!");
                return;
            }
        }

        if (block.getX() == top.getX() && block.getZ() == top.getZ()) {
//			debug.i("main trunk!");
            if (!deep) {
                // something else caught the main, return, this will be done later!
//				debug.i("not deep; out!");
                return;
            }
        }


        if (block.getRelative(0, 1, 0).getType() == Material.LOG) { // might
            // be a
            // trunk
//			debug.i("trunk?");
            // one above is a tree block
            if (block.getX() != top.getX() && block.getZ() != top.getZ()) {
//				debug.i("not main!");

                boolean diff = true;
                for (int Cx = -1; Cx < 2; Cx++) {
                    for (int Cz = -1; Cz < 2; Cz++) {
                        if (block.getX() - Cx == top.getX()
                                && block.getZ() - Cz == top.getZ()) {
                            diff = false;
                            Cx = 2;
                            Cz = 2;
                        }
                    }
                }
                if (diff) {
                    if (checkFail(block)) {
                        return;
                    }
                }
            }
        }

        boolean isBig = bottoms != null;

        boolean destroyBig = false;
        if (tree.getSpecies() == TreeSpecies.JUNGLE
                && Utils.plugin.getConfig()
                .getBoolean("Automatic Tree Destruction.Tree Types.BigJungle")) {
            destroyBig = true;
        }
        if (tree.getSpecies() == TreeSpecies.REDWOOD &&
                Utils.plugin.getConfig()
                        .getBoolean("Automatic Tree Destruction.Tree Types.BigSpruce")) {
            destroyBig = true;
        }
        if (tree.getSpecies() == TreeSpecies.BIRCH &&
                Utils.plugin.getConfig()
                        .getBoolean("Automatic Tree Destruction.Tree Types.BigBirch")) {
            destroyBig = true;
        }
        if (tree.getSpecies() == TreeSpecies.GENERIC &&
                Utils.plugin.getConfig()
                        .getBoolean("Automatic Tree Destruction.Tree Types.BigOak")) {
            destroyBig = true;
        }

        if (!destroyBig && isBig) {
//			debug.i("!destroy & isBig; out!");
            return;
        }

        if (list.contains(block)) {
//			debug.i("already added!");
            return;
        } else {
//			debug.i(">>>>>>>>>> adding! <<<<<<<<<<<");
            list.add(block);
        }

        for (BlockFace face : Utils.NEIGHBORFACES) {
            checkBlock(list, block.getRelative(face), top, false);

            checkBlock(list, block.getRelative(face).getRelative(BlockFace.DOWN), top, false);
            checkBlock(list, block.getRelative(face).getRelative(BlockFace.UP), top, false);
            if (isBig) {
                checkBlock(list, block.getRelative(face, 2), top, false);
            }
        }

        if (!deep) {
//			debug.i("not deep, out!");
            return;
        }

        if (block.getY() > top.getY()) {
//			debug.i("over the top! (hah) out!");
            return;
        }

        if (destroyBig) {
            checkBlock(list, block.getRelative(-2, 0, -2), top, false);
            checkBlock(list, block.getRelative(-1, 0, -2), top, false);
            checkBlock(list, block.getRelative(0, 0, -2), top, false);
            checkBlock(list, block.getRelative(1, 0, -2), top, false);
            checkBlock(list, block.getRelative(2, 0, -2), top, false);
            checkBlock(list, block.getRelative(2, 0, -1), top, false);
            checkBlock(list, block.getRelative(2, 0, 0), top, false);
            checkBlock(list, block.getRelative(2, 0, 1), top, false);
            checkBlock(list, block.getRelative(2, 0, 2), top, false);
            checkBlock(list, block.getRelative(1, 0, 2), top, false);
            checkBlock(list, block.getRelative(0, 0, 2), top, false);
            checkBlock(list, block.getRelative(-1, 0, 2), top, false);
            checkBlock(list, block.getRelative(-2, 0, 2), top, false);
            checkBlock(list, block.getRelative(-2, 0, 1), top, false);
            checkBlock(list, block.getRelative(-2, 0, 0), top, false);
            checkBlock(list, block.getRelative(-2, 0, -1), top, false);
        }
        checkBlock(list, block.getRelative(0, 1, 0), top, true);
    }

    protected boolean checkFail(Block block) {
        Tree tree = (Tree) block.getState().getData();
        if (tree.getSpecies() == TreeSpecies.JUNGLE) {
            if (bottoms == null) {
                if (block.getLocation().distanceSquared(bottom.getLocation()) > 4) {
                    return true;
                }
            }

            for (Block bottom : bottoms) {
                if (block.getLocation().distanceSquared(bottom.getLocation()) > 4) {
                    return true;
                }
            }
        }


        int failCount = 0;
        for (int cont = -4; cont < 5; cont++) {
            if (block.getRelative(0, cont, 0).getType() == Material.LOG) {
                failCount++;
            }
        }
        if (failCount > 3) {
//			debug.i("fail count "+failCount+"! out!");
            return true;
        }
        return false;
    }

    @Override
    protected boolean isBottom(Block block) {
        if (bottoms != null) {
            for (Block b : bottoms) {
                if (b != null && b.equals(block)) {
                    return true;
                }
            }
        }
        return block.equals(bottom);
    }

    @Override
    protected void debug() {
        System.out.print("Tree: VanillaTree");
        System.out.print("TreeSpecies: " + (species == null ? "null" : species.name()));
        System.out.print("bottoms: ");
        if (bottoms == null) {
            System.out.print("null");
        } else {
            for (Block b : bottoms) {
                if (b == null) {
                    System.out.print("null");
                } else {
                    System.out.print(b.toString());
                }
            }
        }


        System.out.print("bottom: " + (bottom == null ? "null" : bottom.toString()));
        System.out.print("top: " + (top == null ? "null" : top.toString()));
        System.out.print("valid: " + valid);

        System.out.print("removeBlocks: " + removeBlocks.size());
        System.out.print("totalBlocks: " + totalBlocks.size());
    }
}
