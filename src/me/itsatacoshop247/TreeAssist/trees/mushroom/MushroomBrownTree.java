package me.itsatacoshop247.TreeAssist.trees.mushroom;

import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class MushroomBrownTree extends AbstractMushroomTree {
    public MushroomBrownTree() {
        super(Material.HUGE_MUSHROOM_1, Material.BROWN_MUSHROOM, "Brown Shroom", "brownshroom");
    }

    @Override
    protected List<Block> calculate(final Block bottom, final Block top) {
        List<Block> list = new ArrayList<Block>();

        int x = bottom.getX();
        int z = bottom.getZ();

        for (int y = bottom.getY(); y < top.getY(); y++) {
            list.add(bottom.getWorld().getBlockAt(x, y, z));
        }

        for (x = -3; x < 4; x++) {
            for (z = -3; z < 4; z++) {
                list.add(top.getRelative(x, 0, z));
            }
        }

        return list;
    }
}
