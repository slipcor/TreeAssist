package me.itsatacoshop247.TreeAssist.trees.wood;

import me.itsatacoshop247.TreeAssist.core.Debugger;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Tree;

import java.util.ArrayList;
import java.util.List;

public class SpruceTree extends AbstractWoodenTree {
    Block[] bottoms = null;

    public SpruceTree() {
        super(TreeSpecies.REDWOOD, "Spruce", "spruce");
    }

    @Override
    protected List<Block> calculate(Block bottom, Block top) {
        List<Block> list = new ArrayList<Block>();
        if (bottoms == null) {
            checkBlock(list, bottom, top, true);
        } else {
            int x = Math.min(bottoms[0].getX(), Math.min(bottoms[1].getX(), bottoms[2].getX()));
            int z = Math.min(bottoms[0].getZ(), Math.min(bottoms[1].getZ(), bottoms[2].getZ()));

            checkBlock(list,
                    bottom.getWorld().getBlockAt(x, bottom.getY(), z),
                    bottom.getWorld().getBlockAt(x, top.getY(), z),
                    BlockFace.WEST, BlockFace.NORTH, true);
            checkBlock(list,
                    bottom.getWorld().getBlockAt(x+1, bottom.getY(), z),
                    bottom.getWorld().getBlockAt(x+1, top.getY(), z),
                    BlockFace.EAST, BlockFace.NORTH, true);
            checkBlock(list,
                    bottom.getWorld().getBlockAt(x, bottom.getY(), z+1),
                    bottom.getWorld().getBlockAt(x, top.getY(), z+1),
                    BlockFace.WEST, BlockFace.SOUTH, true);
            checkBlock(list,
                    bottom.getWorld().getBlockAt(x+1, bottom.getY(), z+1),
                    bottom.getWorld().getBlockAt(x+1, top.getY(), z+1),
                    BlockFace.EAST, BlockFace.SOUTH, true);
        }
        list.addAll(leaves);
        return list;
    }

    // north = towards negative z
    // west = towards negative x

    private void checkBlock(List<Block> list, Block block, Block top, BlockFace x, BlockFace z, boolean deep) {

        if (!Utils.plugin.getConfig()
                .getBoolean("Automatic Tree Destruction.Tree Types.BigSpruce")) {
            return;
        }
        //debug.i("cB " + Debugger.parse(block.getLocation()));

        if (bottoms == null) {
            return;
        }

        if (block.getType() != Material.LOG) {
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
        if (tree.getSpecies() != TreeSpecies.REDWOOD) {
//			debug.i("cB not custom log; data wrong! " + block.getData() + "!=" + top.getData());
            return;
        }

        if (block.getRelative(0, 1, 0).getType() == Material.LOG) { // might
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

        checkBlock(list, block.getRelative(x).getRelative(BlockFace.UP), top, x, z, false);
        checkBlock(list, block.getRelative(x).getRelative(z).getRelative(BlockFace.UP), top, x, z, false);
        checkBlock(list, block.getRelative(z).getRelative(BlockFace.UP), top, x, z, false);

        if (block.getY() > top.getY()) {
//			debug.i("over the top! (hah) out!");
            return;
        }
        checkBlock(list, block.getRelative(0, 1, 0), top, x, z,true);
    }

    @Override
    protected void debug() {
        super.debug();
        if (bottoms != null) {
            for (Block b : bottoms) {
                if (b == null) {
                    System.out.print("null");
                } else {
                    System.out.print(b.toString());
                }
            }
        }
    }

    @Override
    protected void getTrunks() {
        bottoms = new Block[4];
        bottoms[0] = bottom;
        int j = 1;

        if (bottom == null) {
            return;
        }

        int foundsum = 0;

        for (BlockFace face : Utils.NEIGHBORFACES) {
            if (bottom.getRelative(face).getType() == Material.LOG && j < 4) {
                bottoms[j] = bottom.getRelative(face);
                j++;
                foundsum++;
            }
            if (j == 4) {
                break;
            }
        }
        if (foundsum < 4) {
            bottoms = null;
        }
    }

    @Override
    protected void handleSaplingReplace(int delay) {
        if (bottoms != null) {
            if (!Utils.plugin.getConfig().getBoolean(
                    "Sapling Replant.Tree Types to Replant.BigSpruce")) {
                //debugger.i("no big spruce sapling !!!");
                return;
            }
            for (Block bottom : bottoms) {
                handleSaplingReplace(delay, bottom);
                //debugger.i("go !!!");
            }
        }
        handleSaplingReplace(delay, bottom);
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
}
