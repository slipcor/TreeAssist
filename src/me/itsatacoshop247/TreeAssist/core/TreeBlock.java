package me.itsatacoshop247.TreeAssist.core;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

public class TreeBlock implements ConfigurationSerializable {
    private final int x, y, z;
    public final String world;
    public final long time;

    public TreeBlock(final Block b, final long timestamp) {
        x = b.getX();
        y = b.getY();
        z = b.getZ();
        world = b.getWorld().getName();
        time = timestamp;
    }

    public TreeBlock(final Map<String, Object> map) {
        x = (Integer) map.get("x");
        y = (Integer) map.get("y");
        z = (Integer) map.get("z");
        world = map.get("w").toString();
        time = (Long) map.get("t");
    }

    public Block getBukkitBlock() {
        return Bukkit.getWorld(world).getBlockAt(x, y, z);
    }

    @Override
    public Map<String, Object> serialize() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("w", world);
        map.put("x", x);
        map.put("y", y);
        map.put("z", z);
        map.put("t", time);
        return map;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        final TreeBlock theOther = (TreeBlock) other;
        if (this.x != theOther.x) {
            return false;
        }
        if (this.y != theOther.y) {
            return false;
        }
        if (this.z != theOther.z) {
            return false;
        }
        return this.world.equals(theOther.world);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (world == null ? 0 : world.hashCode());
        result = prime * result + (x ^ x >>> 32);
        result = prime * result + (y ^ y >>> 32);
        result = prime * result + (z ^ z >>> 32);
        return result;
    }
}
