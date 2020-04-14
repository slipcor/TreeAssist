package me.itsatacoshop247.TreeAssist.blocklists;

import me.itsatacoshop247.TreeAssist.core.TreeBlock;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlatFileBlockList extends EmptyBlockList {
    private Map<RegionKey, Map<TreeBlock, Long>> mymap = new HashMap<RegionKey, Map<TreeBlock, Long>>();

    /**
     * A small class to ease access to the file name and to mapping of the files' content
     */
    class RegionKey {
        String world;
        int x;
        int z;

        RegionKey(final String world, final int x, final int z) {
            this.world = world;
            this.x = x;
            this.z = z;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (world == null ? 0 : world.hashCode());
            result = prime * result + (x ^ x >>> 32);
            result = prime * result + (z ^ z >>> 32);
            return result;
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
            final RegionKey theOther = (RegionKey) other;
            if (this.x != theOther.x) {
                return false;
            }
            if (this.z != theOther.z) {
                return false;
            }
            return this.world.equals(theOther.world);
        }
    }

    public FlatFileBlockList() {
        super();
    }

    /**
     * Get the TreeBlock Map by Bukkit Block
     *
     * @param block the block to check
     * @return the TreeBlock Map
     */
    private synchronized Map<TreeBlock, Long> getChunkMap(final Block block) {
        final int cx = block.getX() >> 4;
        final int cz = block.getZ() >> 4;

        final int rx = cx >> 5;
        final int rz = cz >> 5;

        final String world = block.getWorld().getName();
        final RegionKey rKey = new RegionKey(world, rx, rz);

        return getChunkMap(rKey);
    }

    /**
     * Get the TreeBlock Map by RegionKey
     *
     * @param rKey the RegionKey to check
     * @return the TreeBlock Map
     */
    private synchronized Map<TreeBlock, Long> getChunkMap(final RegionKey rKey) {
        if (!mymap.containsKey(rKey)) {
            final Map<TreeBlock, Long> map = new HashMap<TreeBlock, Long>();
            try {
                final File path = new File(Utils.plugin.getDataFolder(), rKey.world);
                if (path.isDirectory() && path.exists()) {
                    final File file = new File(path, rKey.x + "." + rKey.z + ".txt");
                    if (file.exists()) {
                        final BufferedReader reader = new BufferedReader(new FileReader(file));
                        String line = null;
                        while ((line = reader.readLine()) != null) {
                            TreeBlock tBlock = new TreeBlock(line);
                            map.put(tBlock, tBlock.time);
                        }
                        reader.close();
                    }
                }
            } catch (Exception e) {
            }
            mymap.put(rKey, map);
        }
        return mymap.get(rKey);
    }

    private synchronized Map<TreeBlock, Long> getChunkMap(final TreeBlock treeBlock) {
        final int cx = treeBlock.getX() >> 4;
        final int cz = treeBlock.getZ() >> 4;

        final int rx = cx >> 5;
        final int rz = cz >> 5;

        final String world = treeBlock.getWorld();
        final RegionKey rKey = new RegionKey(world, rx, rz);

        return getChunkMap(rKey);
    }

    @Override
    public void addBlock(final Block block) {
        final long time = System.currentTimeMillis();
        final Map<TreeBlock, Long> cc = getChunkMap(block);
        cc.put(new TreeBlock(block, time), time);
    }

    private void addBlock(final TreeBlock treeBlock) {
        final Map<TreeBlock, Long> cc = getChunkMap(treeBlock);
        cc.put(treeBlock, treeBlock.time);
    }

    @Override
    public void initiate() {
        final File configFile = new File(Utils.plugin.getDataFolder(), "data.yml");

        try {
            if (configFile.exists()) {
                final FileConfiguration config = new YamlConfiguration();
                config.load(configFile);

                final File backupFile = new File(Utils.plugin.getDataFolder(), "data_old.yml");
                if (!backupFile.exists()) {
                    config.save(backupFile);
                }

                if (config.contains("Blocks")) {
                    final List<String> list = config.getStringList("Blocks");
                    final Map<String, Object> map = new HashMap<String, Object>();
                    for (final String entry : list) {
                        final String[] split = entry.split(";");
                        if (split.length == 4) {
                            // legacy²
                            // X;Y;Z;W
                            try {
                                long time = System.currentTimeMillis();
                                map.put("x", Integer.parseInt(split[0]));
                                map.put("y", Integer.parseInt(split[1]));
                                map.put("z", Integer.parseInt(split[2]));
                                map.put("t", time);
                                map.put("w", split[3]);
                                addBlock(new TreeBlock(map));
                            } catch (Exception e) {
                            }
                        } else if (split.length == 5) {
                            // legacy
                            // X;Y;Z;T;W
                            try {
                                long time = Long.parseLong(split[3]);
                                map.put("x", Integer.parseInt(split[0]));
                                map.put("y", Integer.parseInt(split[1]));
                                map.put("z", Integer.parseInt(split[2]));
                                map.put("t", time);
                                map.put("w", split[4]);
                                addBlock(new TreeBlock(map));
                            } catch (Exception e) {
                            }
                        } else {
                            continue;
                        }
                        map.clear();
                    }
                    config.set("Blocks", null);
                } else if (config.contains("TreeBlocks")) {
                    for (Object o : config.getList("TreeBlocks")) {
                        addBlock((TreeBlock) o);
                    }
                }
                configFile.delete();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        Bukkit.getScheduler().runTaskTimer(Utils.plugin, new Runnable() {
            @Override
            public void run() {
                FlatFileBlockList.this.save(false);
            }
        }, 1200, 1200);
    }

    @Override
    public boolean isPlayerPlaced(final Block block) {
        if (block == null || lookupTime <= 0) {
            return false;
        }
        final Map<TreeBlock, Long> cc = getChunkMap(block);

        if (!cc.containsKey(new TreeBlock(block, 0))) {
            return false;
        }

        long value = cc.get(new TreeBlock(block, 0));

        return value + (lookupTime * 1000L) > System.currentTimeMillis();
    }

    @Override
    public void logBreak(final Block block, final Player player) {
        removeBlock(block);
    }

    @Override
    public void removeBlock(final Block block) {
        if (block == null) {
            return;
        }
        final Map<TreeBlock, Long> cc = getChunkMap(block);
        cc.remove(new TreeBlock(block, 0));
    }

    /**
     * Globally purge blocks - remove non LOG blocks
     *
     * @return the amount of blocks purged
     */
    public int purge() {
        int total = 0;

        final List<RegionKey> keyRemovals = new ArrayList<RegionKey>();

        for (final World world : Bukkit.getWorlds()) {
            final File path = new File(Utils.plugin.getDataFolder(), world.getName());
            if (!path.isDirectory() || !path.exists()) {
                continue;
            }
            for (final File file : path.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    String[] vals = file.getName().split("\\.");
                    try {
                        final int x = Integer.parseInt(vals[0]);
                        final int z = Integer.parseInt(vals[1]);

                        final RegionKey rKey = new RegionKey(world.getName(), x, z);

                        final Map<TreeBlock, Long> blockMap = getChunkMap(rKey);

                        final List<TreeBlock> removals = new ArrayList<TreeBlock>();
                        for (final TreeBlock block : blockMap.keySet()) {
                            final Block bukkitBlock = block.getBukkitBlock();
                            if (!Utils.isLog(bukkitBlock.getType())) {
                                removals.add(block);
                            }
                        }
                        if (removals.size() > 0) {
                            for (final TreeBlock block : removals) {
                                blockMap.remove(block);
                            }
                            saveData(rKey, true);
                            total += removals.size();
                            if (blockMap.size() < 1) {
                                keyRemovals.add(rKey);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
        for (final RegionKey key : keyRemovals) {
            mymap.remove(key);
        }
        return total;
    }

    /**
     * Purge a world, remove all blocks
     * @param worldname
     * @return the amount of blocks purged
     */
    public int purge(final String worldname) {
        int total = 0;

        final File path = new File(Utils.plugin.getDataFolder(), worldname);
        if (path.isDirectory() && path.exists()) {
            for (final File file : path.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    final String[] vals = file.getName().split("\\.");
                    try {
                        final int x = Integer.parseInt(vals[0]);
                        final int z = Integer.parseInt(vals[1]);

                        final RegionKey rKey = new RegionKey(worldname, x, z);

                        final Map<TreeBlock, Long> blockMap = getChunkMap(rKey);

                        if (blockMap.size() > 0) {
                            total += blockMap.size();
                            blockMap.clear();
                            saveData(rKey, true);
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }

        final List<RegionKey> removals = new ArrayList<RegionKey>();

        for (final RegionKey key : mymap.keySet()) {
            if (key.world.equalsIgnoreCase(worldname)) {
                final Map<TreeBlock, Long> blockMap = getChunkMap(key);
                if (blockMap.size() > 0) {
                    total += blockMap.size();
                    blockMap.clear();
                    saveData(key, true);
                }
                removals.add(key);
            }
        }
        for (final RegionKey key : removals) {
            mymap.remove(key);
        }
        return total;
    }

    /**
     * Globally purge blocks - remove blocks that are older than the input
     * @param days the age to allow
     * @return the amount of blocks purged
     */
    public int purge(final int days) {

        int total = 0;

        final List<RegionKey> keyRemovals = new ArrayList<RegionKey>();

        for (final World world : Bukkit.getWorlds()) {
            final File path = new File(Utils.plugin.getDataFolder(), world.getName());
            if (!path.isDirectory() || !path.exists()) {
                continue;
            }
            for (final File file : path.listFiles()) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    final String[] vals = file.getName().split("\\.");
                    try {
                        final int x = Integer.parseInt(vals[0]);
                        final int z = Integer.parseInt(vals[1]);

                        final RegionKey rKey = new RegionKey(world.getName(), x, z);

                        final Map<TreeBlock, Long> blockMap = getChunkMap(rKey);

                        final List<TreeBlock> removals = new ArrayList<TreeBlock>();
                        for (final TreeBlock block : blockMap.keySet()) {
                            if (block.time < (System.currentTimeMillis() - days * 24 * 60 * 60 * 1000)) {
                                removals.add(block);
                            }
                        }
                        if (removals.size() > 0) {
                            for (final TreeBlock block : removals) {
                                blockMap.remove(block);
                            }
                            saveData(rKey, true);
                            total += removals.size();
                            if (blockMap.size() < 1) {
                                keyRemovals.add(rKey);
                            }
                        }
                    } catch (Exception e) {
                    }
                }
            }
        }
        for (final RegionKey key : keyRemovals) {
            mymap.remove(key);
        }
        return total;
    }

    @Override
    public void save(final boolean force) {
        for (final RegionKey key : mymap.keySet()) {
            saveData(key, force);
        }
        mymap.clear();
    }

    /**
     * Save a certain RegionKey file
     *
     * @param rKey  the RegionKey to save
     * @param force are we shutting down, so must we not use async?
     */
    private void saveData(final RegionKey rKey, final boolean force) {
        final File path = new File(Utils.plugin.getDataFolder(), rKey.world);
        final Map<TreeBlock, Long> map = new HashMap<TreeBlock, Long>(mymap.get(rKey));

        class RunLater implements Runnable {
            @Override
            public void run() {
                try {
                    if (!path.exists() || !path.isDirectory()) {
                        path.mkdir();
                    }
                    final File file = new File(path, rKey.x + "." + rKey.z + ".txt");
                    if (!file.exists() && map != null && map.size() > 0) {
                        file.createNewFile();
                    } else if (map == null || map.size() < 1) {
                        file.delete();
                        return;
                    }
                    final PrintWriter pw = new PrintWriter(file);
                    for (TreeBlock block : map.keySet()) {
                        pw.println(block.toString());
                    }
                    pw.close();
                } catch (Exception e) {
                }
            }
        }
        if (force) {
            new RunLater().run();
        } else {
            Bukkit.getScheduler().runTaskAsynchronously(Utils.plugin, new RunLater());
        }
    }
}
