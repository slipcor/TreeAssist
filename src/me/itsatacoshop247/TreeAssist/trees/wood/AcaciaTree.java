package me.itsatacoshop247.TreeAssist.trees.wood;

import me.itsatacoshop247.TreeAssist.core.Debugger;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.Material;
import org.bukkit.TreeSpecies;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AcaciaTree extends AbstractWoodenTree {
    private final List<Block> blocks = new ArrayList<>();
    private final List<Block> leafTops = new ArrayList<>();

    public AcaciaTree() {
        super(TreeSpecies.ACACIA, "Acacia", "acacia");
    }

    @Override
    protected List<Block> calculate(final Block bottom, final Block top) {
        //debugger.i("size: " + blocks.size());
        for (Block block : leafTops) {
            for (BlockFace face : Utils.NEIGHBORFACES) {
                blocks.add(block.getRelative(face));
                blocks.add(block.getRelative(face).getRelative(BlockFace.UP));
            }
            blocks.add(block.getRelative(BlockFace.UP));
        }
        return blocks;
    }

    @Override
    protected Block getBottom(Block block) {
        int counter = 1;
        do {
            if (block.getRelative(0, 0 - counter, 0).getType() == logMaterial) {
                counter++;
            } else {
                bottom = block.getRelative(0, 1 - counter, 0);

                boolean foundDiagonal = false;

                for (BlockFace face : Utils.NEIGHBORFACES) {
                    if (bottom.getRelative(BlockFace.DOWN).getRelative(face).getType() == logMaterial) {
                        bottom = bottom.getRelative(BlockFace.DOWN).getRelative(face);
                        block = block.getRelative(face);
                        foundDiagonal = true;
                    }
                }

                if (foundDiagonal) {
                    counter++;
                    continue;
                }

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
        return bottom;
    }

    private Block getDiagonalTop(Block block, BlockFace face) {
        if (!blocks.contains(block)) {
            //debugger.i("adding "+face+": " + Debugger.parse(block.getLocation()));
            blocks.add(block);
        }
        if (block.getRelative(BlockFace.UP).getType() == logMaterial) {
            return getDiagonalTop(block.getRelative(BlockFace.UP), face);
        }
        if (block.getRelative(BlockFace.UP).getRelative(face).getType() == logMaterial) {
            return getDiagonalTop(block.getRelative(BlockFace.UP).getRelative(face), face);
        }
        // we are at the top
        leafTops.add(block);
        return block;
    }

    @Override
    protected Block getTop(Block block) {
        int maxY = block.getWorld().getMaxHeight() + 10;
        int counter = 1;

        //debug.i("trying to calculate the TOP block of a 1.7 tree!");


        Map<BlockFace, Block> checkMap = new HashMap<BlockFace, Block>();

        while (block.getY() + counter < maxY) {
            if (block.getRelative(0, counter, 0).getType() != logMaterial) {
                // reached non log,
                top = block.getRelative(0, counter-1, 0);
                if (!blocks.contains(top)) {
                    //debugger.i("adding top: " + Debugger.parse(top.getLocation()));
                    blocks.add(top);
                }
                break;
            } else {
                Block temp = block.getRelative(0, counter, 0);
                if (!blocks.contains(temp)) {
                    //debugger.i("adding trunk: " + Debugger.parse(temp.getLocation()));
                    blocks.add(temp);
                }
                if (counter == 1) {
                    // first trunk, let's double check that there is no INSTANT branch
                    for (BlockFace face : Utils.NEIGHBORFACES) {
                        Block check = temp.getRelative(face);
                        if (check.getType() == logMaterial) {
                            check = getDiagonalTop(check, face);
                            checkMap.put(face, check);
                        }
                    }
                }

                for (BlockFace face : Utils.NEIGHBORFACES) {
                    Block check = temp.getRelative(face);
                    if (check.getRelative(BlockFace.UP).getType() == logMaterial) {
                        check = getDiagonalTop(check.getRelative(BlockFace.UP), face);
                        checkMap.put(face, check);
                    }
                }
                counter++;
            }
        }

		debug.i("> straight trunk for " + counter + " blocks; y="+top.getY());

        leafTops.add(top);

        // top is the currently last trunk log
        // from here on we either go horizontally or diagonally

        for (BlockFace face : Utils.NEIGHBORFACES) {
            Block check = top.getRelative(face);
            if (check.getRelative(BlockFace.UP).getType() == logMaterial) {
                check = getDiagonalTop(check.getRelative(BlockFace.UP), face);
                checkMap.put(face, check);
            }
        }

        for (BlockFace face : checkMap.keySet()) {
            Block check = checkMap.get(face);
            if  (check.getY() > top.getY()) {
                top = check;
            }
        }

        top = top.getRelative(BlockFace.UP);

		debug.i("final top should be at y="+top.getY());

        return (top != null && leafCheck(top)) ? top : null;
    }

    @Override
    protected void getTrunks() {
    }
}
