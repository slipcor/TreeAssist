package me.itsatacoshop247.TreeAssist.trees.wood;

import me.itsatacoshop247.TreeAssist.core.Debugger;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Tree;

import java.util.List;

public class OakTree extends AbstractWoodenTree {
    public static Debugger debugger;

    public OakTree() {
        super(TreeSpecies.GENERIC, "Oak", "oak");
    }

    @Override
    public void checkBlock(List<Block> list, Block block,
                           Block top, boolean deep) {
        //debug.i("cB " + Debugger.parse(block.getLocation()));
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
        if (tree.getSpecies() != TreeSpecies.GENERIC) {
            return;
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

        if (deep) {

            for (BlockFace face : Utils.NEIGHBORFACES) {
                checkBlock(list, block.getRelative(face), top, false);
            }
        } else {

            for (BlockFace face : Utils.NEIGHBORFACES) {
                checkBlock(list, block.getRelative(face), top, false);
                checkBlock(list, block.getRelative(face).getRelative(BlockFace.DOWN), top, false);
                checkBlock(list, block.getRelative(face).getRelative(BlockFace.UP), top, false);
            }
//			debug.i("not deep, out!");
            return;

        }

        if (block.getY() > top.getY()) {
//			debug.i("over the top! (hah) out!");
            return;
        }

        checkBlock(list, block.getRelative(0, 1, 0), top, true);
    }

    @Override
    protected void getTrunks() {
    }
}
