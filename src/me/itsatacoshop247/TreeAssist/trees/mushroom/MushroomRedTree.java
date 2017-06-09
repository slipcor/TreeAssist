package me.itsatacoshop247.TreeAssist.trees.mushroom;

import me.itsatacoshop247.TreeAssist.core.Debugger;
import org.bukkit.Material;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class MushroomRedTree extends AbstractMushroomTree {
    public MushroomRedTree() {
        super(Material.HUGE_MUSHROOM_2, Material.RED_MUSHROOM, "Red Shroom", "redshroom");
    }

    @Override
    protected List<Block> calculate(final Block bottom, final Block top) {
        List<Block> list = new ArrayList<Block>();

        int x = bottom.getX();
        int z = bottom.getZ();

        for (int y = bottom.getY(); y < top.getY(); y++) {
            list.add(bottom.getWorld().getBlockAt(x, y, z));
        }

        for (x = -1; x < 2; x++) {
            for (z = -1; z < 2; z++) {
                list.add(top.getRelative(x, 0, z));

                list.add(top.getRelative(-2, x-2, z));
                list.add(top.getRelative(2, x-2, z));
                list.add(top.getRelative(z, x-2, -2));
                list.add(top.getRelative(z, x-2, 2));
            }
        }

        return list;
    }
}
