package me.itsatacoshop247.TreeAssist.trees.wood;

import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.material.Tree;

import java.util.ArrayList;
import java.util.List;

public class JungleBigTree extends AbstractWoodenTree {
    Block[] bottoms = null;

    public JungleBigTree() {
        super(TreeSpecies.JUNGLE, "Jungle", "jungle");
    }

    @Override
    protected List<Block> calculate(final Block bottom, final Block top) {
        List<Block> list = new ArrayList<Block>();
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
        list.addAll(leaves);

        return list;
    }

    private void checkBlock(List<Block> list, Block block, Block top, BlockFace x, BlockFace z, boolean deep) {

        if (!Utils.plugin.getConfig()
                .getBoolean("Automatic Tree Destruction.Tree Types.BigJungle")) {
            return;
        }
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
        if (tree.getSpecies() != TreeSpecies.JUNGLE) {
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

        checkBlock(list, block.getRelative(x), top, x, z, false);
        checkBlock(list, block.getRelative(x).getRelative(z), top, x, z, false);
        checkBlock(list, block.getRelative(z), top, x, z, false);

        checkBlock(list, block.getRelative(x).getRelative(BlockFace.UP), top, x, z, false);
        checkBlock(list, block.getRelative(x).getRelative(z).getRelative(BlockFace.UP), top, x, z, false);
        checkBlock(list, block.getRelative(z).getRelative(BlockFace.UP), top, x, z, false);


        if (!deep) {
            // last resort: look for switching branches away from the trunk

            // north: -z
            // west: -x

            if (x == BlockFace.WEST && z == BlockFace.NORTH) {
                if (block.getZ() == bottom.getZ() && block.getRelative(0, 0, 1).getType() != Material.LOG) {
                    // on the NORTH X axis -> to the WEST of the trunk log, check the SOUTH relative and one above
                    checkBlock(list, block.getRelative(0, 0, 1), top, x, z, false);
                    checkBlock(list, block.getRelative(0, 1, 1), top, x, z, false);

                    checkBlock(list, block.getRelative(-1, 0, 1), top, x, z, false);
                    checkBlock(list, block.getRelative(-1, 1, 1), top, x, z, false);
                } else if (block.getX() == bottom.getX() && block.getRelative(1, 0, 0).getType() != Material.LOG) {
                    // on the WEST Z axis, -> to the NORTH of the trunk log, check the EAST relative and one above
                    checkBlock(list, block.getRelative(1, 0, 0), top, x, z, false);
                    checkBlock(list, block.getRelative(1, 1, 0), top, x, z, false);

                    checkBlock(list, block.getRelative(1, 0, -1), top, x, z, false);
                    checkBlock(list, block.getRelative(1, 1, -1), top, x, z, false);
                }
            } else if (x == BlockFace.EAST && z == BlockFace.NORTH) {
                if (block.getX() == bottom.getX()+1 && block.getRelative(-1, 0, 0).getType() != Material.LOG) {
                    // on the EAST Z axis, -> to the NORTH of the trunk log, check the WEST relative and one above
                    checkBlock(list, block.getRelative(-1, 0, 0), top, x, z, false);
                    checkBlock(list, block.getRelative(-1, 1, 0), top, x, z, false);

                    checkBlock(list, block.getRelative(-1, 0, -1), top, x, z, false);
                    checkBlock(list, block.getRelative(-1, 1, -1), top, x, z, false);
                } else if (block.getZ() == bottom.getZ() && block.getRelative(0, 0, 1).getType() != Material.LOG) {
                    // on the NORTH X axis -> to the EAST of the trunk log, check the SOUTH relative and one above
                    checkBlock(list, block.getRelative(0, 0, 1), top, x, z, false);
                    checkBlock(list, block.getRelative(0, 1, 1), top, x, z, false);

                    checkBlock(list, block.getRelative(1, 0, 1), top, x, z, false);
                    checkBlock(list, block.getRelative(1, 1, 1), top, x, z, false);
                }
            } else if (x == BlockFace.EAST && z == BlockFace.SOUTH) {
                if (block.getZ() == bottom.getZ()+1 && block.getRelative(0, 0, -1).getType() != Material.LOG) {
                    // on the SOUTH X axis -> to the EAST of the trunk log, check the NORTH relative and one above
                    checkBlock(list, block.getRelative(0, 0, -1), top, x, z, false);
                    checkBlock(list, block.getRelative(0, 1, -1), top, x, z, false);

                    checkBlock(list, block.getRelative(1, 0, -1), top, x, z, false);
                    checkBlock(list, block.getRelative(1, 1, -1), top, x, z, false);
                } else if (block.getX() == bottom.getX()+1 && block.getRelative(-1, 0, 0).getType() != Material.LOG) {
                    // on the EAST Z axis, -> to the SOUTH of the trunk log, check the WEST relative and one above
                    checkBlock(list, block.getRelative(-1, 0, 0), top, x, z, false);
                    checkBlock(list, block.getRelative(-1, 1, 0), top, x, z, false);

                    checkBlock(list, block.getRelative(-1, 0, 1), top, x, z, false);
                    checkBlock(list, block.getRelative(-1, 1, 1), top, x, z, false);
                }
            } else if (x == BlockFace.WEST && z == BlockFace.SOUTH) {
                if (block.getX() == bottom.getX() && block.getRelative(1, 0, 0).getType() != Material.LOG) {
                    // on the WEST Z axis, -> to the SOUTH of the trunk log, check the EAST relative and one above
                    checkBlock(list, block.getRelative(1, 0, 0), top, x, z, false);
                    checkBlock(list, block.getRelative(1, 1, 0), top, x, z, false);

                    checkBlock(list, block.getRelative(1, 0, 1), top, x, z, false);
                    checkBlock(list, block.getRelative(1, 1, 1), top, x, z, false);
                } else if (block.getZ() == bottom.getZ()+1 && block.getRelative(0, 0, -1).getType() != Material.LOG) {
                    // on the SOUTH X axis -> to the WEST of the trunk log, check the NORTH relative and one above
                    checkBlock(list, block.getRelative(0, 0, -1), top, x, z, false);
                    checkBlock(list, block.getRelative(0, 1, -1), top, x, z, false);

                    checkBlock(list, block.getRelative(-1, 0, -1), top, x, z, false);
                    checkBlock(list, block.getRelative(-1, 1, -1), top, x, z, false);
                }
            }

//			debug.i("not deep, out!");
            return;
        }
/*
        checkBlock(list, block.getRelative(x).getRelative(x).getRelative(BlockFace.UP), top, x, z, false);
        checkBlock(list, block.getRelative(z).getRelative(z).getRelative(BlockFace.UP), top, x, z, false);
*/
        if (block.getY() > top.getY()) {
//			debug.i("over the top! (hah) out!");
            return;
        }
        checkBlock(list, block.getRelative(0, 1, 0), top, x, z,true);
    }

    @Override
    public void checkBlock(List<Block> list, Block block,
                           Block top, boolean deep) {
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
    protected Block getBottom(Block block) {
        int counter = 1;
        do {
            if (block.getRelative(0, 0 - counter, 0).getType() == Material.LOG) {
                counter++;
            } else {
                if (block.getRelative(0, 0, -1).getType() == Material.LOG) {
                    block = block.getRelative(0, 0, -1);
                }
                if (block.getRelative(-1, 0, 0).getType() == Material.LOG) {
                    block = block.getRelative(-1, 0, 0);
                }

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
        if (foundsum < 1) {
            bottoms = null;
        }
    }

    @Override
    protected void handleSaplingReplace(int delay) {
        if (bottoms != null) {
            if (!Utils.plugin.getConfig().getBoolean(
                    "Sapling Replant.Tree Types to Replant.BigJungle")) {
                debugger.i("no big jungle sapling !!!");
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
