package me.itsatacoshop247.TreeAssist.blocklists;

import me.itsatacoshop247.TreeAssist.core.TreeBlock;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlatFileBlockList implements BlockList {
    final FileConfiguration config = new YamlConfiguration();
    File configFile;
    private final Map<TreeBlock, Long> blockMap = new HashMap<TreeBlock, Long>();

    @Override
    public void addBlock(Block block) {
        final long time = System.currentTimeMillis();
        blockMap.put(new TreeBlock(block, time), time);
    }

    @Override
    public void initiate() {
        configFile = new File(Utils.plugin.getDataFolder(), "data.yml");

        try {
            if (!configFile.exists()) {
                configFile.createNewFile();
            }
            config.load(configFile);
            if (config.contains("Blocks")) {
                File backupFile = new File(Utils.plugin.getDataFolder(), "data_backup.yml");
                if (!backupFile.exists()) {
                    config.save(backupFile);
                }
                final List<String> list = config.getStringList("Blocks");
                final Map<String, Object> map = new HashMap<String, Object>();
                for (String entry : list) {
                    String[] split = entry.split(";");
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
                            blockMap.put(new TreeBlock(map), time);
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
                            blockMap.put(new TreeBlock(map), time);
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
                    TreeBlock block = (TreeBlock) o;
                    blockMap.put(block, block.time);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        Bukkit.getScheduler().runTaskTimerAsynchronously(Utils.plugin, new Runnable() {
            @Override
            public void run() {
                FlatFileBlockList.this.save(true);
            }
        }, 1200, 1200);
    }


    @Override
    public boolean isPlayerPlaced(final Block block) {
        if (blockMap == null || block == null) {
            return false;
        }
        TreeBlock check = new TreeBlock(block, 0);
        return blockMap.containsKey(check);
    }

    @Override
    public void logBreak(Block block, Player player) {
        removeBlock(block);
    }

    @Override
    public void removeBlock(final Block block) {
        if (blockMap == null || block == null) {
            return;
        }
        TreeBlock check = new TreeBlock(block, 0);
        blockMap.remove(check);
    }

    public int purge(final CommandSender sender) {
        final List<TreeBlock> removals = new ArrayList<TreeBlock>();
        for (TreeBlock block : blockMap.keySet()) {
            if (Bukkit.getWorld(block.world) == null) {
                removals.add(block);
                continue;
            }
            Block bukkitBlock = block.getBukkitBlock();
            if (bukkitBlock.getType() != Material.LOG &&
                    !bukkitBlock.getType().name().equals(Material.LOG_2)) {
                removals.add(block);
            }
        }
        for (TreeBlock block : removals) {
            blockMap.remove(block);
        }
        save(true);
        return removals.size();
    }

    public int purge(final String worldname) {
        final List<TreeBlock> removals = new ArrayList<TreeBlock>();
        for (TreeBlock block : blockMap.keySet()) {
            if (block.world.toLowerCase().endsWith(worldname.toLowerCase())) {
                removals.add(block);
            }
        }
        for (TreeBlock block : removals) {
            blockMap.remove(block);
        }
        save(true);
        return removals.size();
    }

    public int purge(final int days) {
        final List<TreeBlock> removals = new ArrayList<TreeBlock>();
        for (TreeBlock block : blockMap.keySet()) {
            long time = blockMap.get(block);
            if (time < (System.currentTimeMillis() - days * 24 * 60 * 60 * 1000)) {
                removals.add(block);
            }
        }
        for (TreeBlock block : removals) {
            blockMap.remove(block);
        }
        save(true);
        return removals.size();
    }

    @Override
    public void save() {
    }

    @Override
    public void save(boolean force) {
        this.saveData();
    }

    private void saveData() {
        try {
            List<TreeBlock> list = new ArrayList<>();
            for (TreeBlock block : blockMap.keySet()) {
                list.add(block);
            }
            config.set("TreeBlocks", list);
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
