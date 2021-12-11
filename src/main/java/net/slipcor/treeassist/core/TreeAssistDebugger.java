package net.slipcor.treeassist.core;

import net.slipcor.core.CoreDebugger;
import net.slipcor.core.CorePlugin;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.utils.BlockUtils;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class TreeAssistDebugger extends CoreDebugger {
    private static Player    player;
    private static ErrorType error;

    public void explain(ErrorType type, Block block, String message) {
        if (isCheckingForError(type, block.getLocation())) {
            TreeAssist.instance.sendPrefixed(player, message + " - " + BlockUtils.printBlock(block));
        }
    }

    public enum ErrorType {
        ALL,      // why is anything and everything?
        DECAY,    // why are leaves not decaying?
        GROW,     // why are trees not growing?
        SAPLING,  // why are there no saplings replanted?
        DROPS,    // why do we not receive log drops / item drops?
        AUTOCHOP, // why does the tree not get chopped?
        CLEANUP,  // why does the tree not get cleaned up?
    }

    /**
     * Create a Debugger instance
     *
     * @param plugin  the CorePlugin to debug
     * @param debugID the Debugger instance ID
     */
    public TreeAssistDebugger(CorePlugin plugin, int debugID) {
        super(plugin, debugID);
    }

    public static void setCommandSender(Player sender) {
        TreeAssistDebugger.player = sender;
    }

    public static void setError(String error) {
        if (error == null) {
            TreeAssistDebugger.error = null;
            return;
        }
        for (ErrorType errorType : ErrorType.values()) {
            if (errorType.name().equalsIgnoreCase(error)) {
                TreeAssistDebugger.error = errorType;
                return;
            }
        }
        TreeAssistDebugger.error = null;
    }

    public static boolean isCheckingForError(ErrorType check, Location location) {
        if (error == null || player == null) {
            // we are not looking for anything
            return false;
        }

        if (player.getLocation().distanceSquared(location) > 10000) {
            // too far away
            return false;
        }

        if (error == ErrorType.ALL) {
            return true;
        }

        if (check == ErrorType.DROPS) {
            return error == ErrorType.AUTOCHOP || error == ErrorType.DROPS;
        }

        return error == check;
    }
}
