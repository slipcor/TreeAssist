package net.slipcor.treeassist.utils;

import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.discovery.TreeStructure;
import net.slipcor.treeassist.yml.TreeConfig;
import org.bukkit.Material;

public class MaterialUtils {
    private MaterialUtils() {}

    /**
     * Check whether a given material is air
     *
     * @param mat the material to check
     * @return whether the material is air
     */
    public static boolean isAir(final Material mat) {
        return mat == null || mat == Material.AIR || mat == Material.CAVE_AIR || mat == Material.VOID_AIR;
    }

    /**
     * Check whether a given material is a valid extra tree block
     *
     * @param material the material to check
     * @return whether the material is an extra block
     */
    public static boolean isLeaf(Material material) {
        return TreeStructure.allExtras.contains(material);
    }

    /**
     * Check whether a given material is a valid trunk block
     *
     * @param material the material to check
     * @return whether the material is a log block
     */
    public static boolean isLog(Material material) {
        return TreeStructure.allTrunks.contains(material);
    }

    /**
     * Check whether a given material is a valid mushroom block
     *
     * @param material the material to check
     * @return whether the material is a mushroom block
     */
    public static boolean isMushroom(Material material) {
        switch (material) {
            case BROWN_MUSHROOM:
            case BROWN_MUSHROOM_BLOCK:
            case MUSHROOM_STEM:
            case RED_MUSHROOM:
            case RED_MUSHROOM_BLOCK:
                return true;
        }
        return false;
    }

    /**
     * Check whether a given material is a valid sapling
     *
     * @param material the material to check
     * @return whether the material is a sapling
     */
    public static boolean isSapling(Material material) {
        if (material == Material.AIR) {
            return false;
        }
        for (TreeConfig config : TreeAssist.treeConfigs.values()) {
            if (material == config.getMaterial(TreeConfig.CFG.REPLANTING_MATERIAL)) {
                return true;
            }
        }
        return false;
    }
}
