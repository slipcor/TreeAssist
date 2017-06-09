package me.itsatacoshop247.TreeAssist.trees;

import me.itsatacoshop247.TreeAssist.core.Debugger;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public class InvalidTree extends AbstractGenericTree {
    public static Debugger debugger;

    public InvalidTree() {
    }

    @Override
    public boolean isValid() {
        return false;
    }

    @Override
    protected boolean willBeDestroyed() {
        return false;
    }

    @Override
    protected Block getBottom(Block block) {
        return null;
    }

    @Override
    protected Block getTop(Block block) {
        return null;
    }

    @Override
    protected List<Block> calculate(Block bottom, Block top) {
        return null;
    }

    @Override
    protected int isLeaf(Block block) {
        return 0;
    }

    @Override
    protected boolean hasPerms(Player player) {
        return false;
    }

    @Override
    protected boolean willReplant() {
        return false;
    }

    @Override
    protected void getTrunks() {
    }

    @Override
    protected void handleSaplingReplace(int delay) {
    }

    @Override
    protected boolean checkFail(Block block) {
        return false;
    }

    @Override
    protected boolean isBottom(Block block) {
        return false;
    }

    @Override
    protected void debug() {
        System.out.print("Tree: InvalidTree");
    }
}
