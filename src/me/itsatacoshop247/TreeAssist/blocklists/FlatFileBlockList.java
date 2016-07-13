package me.itsatacoshop247.TreeAssist.blocklists;

import me.itsatacoshop247.TreeAssist.core.Language;
import me.itsatacoshop247.TreeAssist.core.Language.MSG;
import me.itsatacoshop247.TreeAssist.core.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FlatFileBlockList implements BlockList {
    FileConfiguration data = new YamlConfiguration();
    File dataFile;
    private List<String> list = new ArrayList<String>();

    @Override
    public void addBlock(Block block) {
        String check = toString(block);

        list.add(check);
        data.set("Blocks", list);
    }

    @Override
    public void initiate() {
        this.dataFile = new File(Utils.plugin.getDataFolder(), "data.yml");
        data.options().copyDefaults(true);
        try {
            if (!this.dataFile.exists()) {
                this.dataFile.createNewFile();
            }
            this.data.load(this.dataFile);
            list = data.getStringList("Blocks");

            if (list == null || list.size() < 1) {
                return;
            }

            final String first = list.get(0);
            final String[] split = first.split(";");
            if (split.length < 5) {
                List<String> newList = new ArrayList<String>();
                StringBuffer buff = new StringBuffer();
                for (String def : list) {
                    String[] defSplit = def.split(";");
                    buff.setLength(0);
                    buff.append(defSplit[0]);
                    buff.append(';');
                    buff.append(defSplit[1]);
                    buff.append(';');
                    buff.append(defSplit[2]);
                    buff.append(';');
                    buff.append(System.currentTimeMillis());
                    buff.append(';');
                    buff.append(defSplit[3]);

                    newList.add(buff.toString());
                }
                list = newList;
                save(true);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        if (!this.dataFile.exists()) {
            this.dataFile.getParentFile().mkdirs();
            copy(Utils.plugin.getResource("data.yml"), this.dataFile);
            list = new ArrayList<String>();
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
        if (list == null || block == null) {
            return false;
        }
        final String[] check = toString(block).split(";");

        for (String s : list) {
            String[] b = s.split(";");
            if (check[0].equals(b[0]) &&
                    check[1].equals(b[1]) &&
                    check[2].equals(b[2]) &&
                    check[4].equals(b[4])) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void logBreak(Block block, Player player) {
        removeBlock(block);
    }

    @Override
    public void removeBlock(final Block block) {
        final String[] check = toString(block).split(";");
        final List<String> removals = new ArrayList<String>();

        for (String s : list) {
            String[] b = s.split(";");
            if (check[0].equals(b[0]) &&
                    check[1].equals(b[1]) &&
                    check[2].equals(b[2]) &&
                    check[4].equals(b[4])) {
                removals.add(s);
            }
        }
        for (String s : removals) {
            list.remove(s);
        }
        data.set("Blocks", list);
    }

    public int purge(final CommandSender sender) {
        final List<String> removals = new ArrayList<String>();
        int line = 1;
        for (String def : list) {
            line++;
            String[] split = def.split(";");
            // x y z TIME world
            if (split.length < 4) {
                removals.add(def);
                continue;
            }
            World world = Bukkit.getWorld(split[4]);
            if (world == null) {
                removals.add(def);
                continue;
            }
            try {
                int x = Integer.parseInt(split[0]);
                int y = Integer.parseInt(split[1]);
                int z = Integer.parseInt(split[2]);
                Block block = world.getBlockAt(x, y, z);
                if (block.getType() != Material.LOG && !block.getType().name().equals(Material.LOG_2)) {
                    removals.add(def);
                }
            } catch (NumberFormatException e) {
                removals.add(def);
            } catch (Exception e) {
                sender.sendMessage(Language.parse(MSG.ERROR_DATA_YML) + " (#" + line + ")");
                return 0;
            }
        }
        list.removeAll(removals);
        save(true);
        return removals.size();
    }

    public int purge(final String worldname) {
        final List<String> removals = new ArrayList<String>();
        for (String def : list) {
            if (def.endsWith(worldname)) {
                removals.add(def);
            }
        }
        list.removeAll(removals);
        save(true);
        return removals.size();
    }

    public int purge(final int days) {
        final List<String> removals = new ArrayList<String>();
        for (String def : list) {
            int i = Integer.valueOf(def.split(";")[3]);
            if (i < (System.currentTimeMillis() - days * 24 * 60 * 60 * 1000)) {
                removals.add(def);
            }
        }
        list.removeAll(removals);
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

    private String toString(Block block) {
        return block.getX() + ";" + block.getY() + ";"
                + block.getZ() + ";" + System.currentTimeMillis() + ";" + block.getWorld().getName();
    }

    private void copy(InputStream in, File file) {
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private synchronized void saveData() {
        try {
            data.set("Blocks", list);
            data.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
