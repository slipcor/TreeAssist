package me.itsatacoshop247.TreeAssist.trees.wood;

import me.itsatacoshop247.TreeAssist.core.Debugger;
import me.itsatacoshop247.TreeAssist.core.Utils;
import me.itsatacoshop247.TreeAssist.trees.AbstractGenericTree;
import me.itsatacoshop247.TreeAssist.trees.InvalidTree;
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

    private void addTrunk(List<Block> blocks) {
        findSaplingBlock(bottom);
        Block block = saplingBlock;

        while (!blocks.contains(block) && isLog(block.getType())) {
            blocks.add(block);
            block = block.getRelative(BlockFace.UP);
            if (!isLog(block.getType())) {
                for (BlockFace face : Utils.NEIGHBORFACES) {
                    Block neighbor = block.getRelative(face);
                    if (isLog(neighbor.getType()) && !blocks.contains(neighbor)) {
                        block = neighbor;
                        break;
                    }
                }
            }
        }
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
        if (!Utils.plugin.getConfig().getBoolean("Main.Destroy Only Blocks Above")) {
            addTrunk(blocks);
        }
        return blocks;
    }

    protected AbstractGenericTree findSaplingBlock(Block block) {
        Block newBlock = block;
        int count = 5;
        saplingBlock = block; // assume we are already there
        while (this.isLog(newBlock.getType()) || newBlock.getType() == Material.AIR) {
            if (--count <= 0) {

                debug.i("this is probably not a tree!!");
                return new InvalidTree();
            }
            if (this.isLog(newBlock.getType())) {
                saplingBlock = newBlock; // override sapling block
                debug.i("Overriding saplingBlock to " + Debugger.parse(newBlock.getLocation()));
                if (!Utils.plugin.getConfig().getBoolean("Main.Destroy Only Blocks Above")) {
                    blocks.add(saplingBlock);
                }
            }
            newBlock = newBlock.getRelative(BlockFace.DOWN);
            if (!this.isLog(newBlock.getType())) {
                // let's have a look around!

                for (BlockFace face : Utils.NEIGHBORFACES) {
                    if (newBlock.getRelative(face).getType() == logMaterial) {
                        newBlock = newBlock.getRelative(face);
                    }
                }
            }
            debug.i("sliding down: " + Debugger.parse(newBlock.getLocation()));
        }
        return this;
    }

    @Override
    protected Block getBottom(Block block) {
        int min = Utils.plugin.getConfig().getBoolean("Main.Destroy Only Blocks Above") ? block.getY() : 0;

        debug.i("lowest block will be: " + min);

        int counter = 1;
        while (block.getY() - counter >= min) {
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
                        bottom.getRelative(BlockFace.DOWN).getType() != Material.GRASS_BLOCK &&
                        bottom.getRelative(BlockFace.DOWN).getType() != Material.CLAY &&
                        bottom.getRelative(BlockFace.DOWN).getType() != Material.SAND &&
                        bottom.getRelative(BlockFace.DOWN).getType() != Material.PODZOL) {
                    return null; // the tree is already broken.
                }
                return bottom;
            }
        }

        if (Utils.plugin.getConfig().getBoolean("Main.Destroy Only Blocks Above")) {
            return bottom; // if we destroy above we can assume we have nothing to lose down there
        } // otherwise we assume that we tried to go too far down and return a non-tree!

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
        if (!Utils.plugin.getConfig().getBoolean("Main.Destroy Only Blocks Above")) {
            // we need to first go down to find the actual bottom, otherwise this never gets calculated properly
            boolean bottom = false; // assume we are not at the base yet

            while (!bottom) {
                bottom = true;
                if (block.getRelative(BlockFace.DOWN).getType() == logMaterial) {
                    bottom = false;
                    block = block.getRelative(BlockFace.DOWN);
                    continue;
                }
                for (BlockFace face : Utils.NEIGHBORFACES) {
                    if (block.getRelative(BlockFace.DOWN).getRelative(face).getType() == logMaterial) {
                        bottom = false;
                        block = block.getRelative(BlockFace.DOWN).getRelative(face);
                    }
                }
            }
        }


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

		//debug.i("> straight trunk for " + counter + " blocks; y="+top.getY());

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

		  //debug.i("final top should be at y="+top.getY());

        return (top != null && leafCheck(top)) ? top : null;
    }

    @Override
    protected void getTrunks() {
    }
}
