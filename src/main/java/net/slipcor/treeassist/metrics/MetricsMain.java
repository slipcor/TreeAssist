package net.slipcor.treeassist.metrics;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.slipcor.treeassist.TreeAssist;
import net.slipcor.treeassist.configs.MainConfig;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.Callable;

/**
 * bStats collects some data for plugin authors.
 * <p>
 * Check out https://bStats.org/ to learn more about bStats!
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class MetricsMain extends MetricsBase {

    /**
     * Class constructor.
     *
     * @param plugin The plugin which stats should be submitted.
     */
    public MetricsMain(Plugin plugin) {
        super(plugin);

        this.addChart(MainConfig.CFG.GENERAL_TOGGLE_DEFAULT, "toggle_default");
        this.addChart(MainConfig.CFG.WORLDS_RESTRICT, "restrict_worlds");

        this.addChart(MainConfig.CFG.DESTRUCTION_FALLING_BLOCKS, "falling_blocks");
        this.addChart(MainConfig.CFG.DESTRUCTION_FALLING_BLOCKS_FANCY, "fancy_blocks");
        this.addChart(MainConfig.CFG.DESTRUCTION_FAST_LEAF_DECAY, "fast_leaf_decay");
        this.addChart(MainConfig.CFG.DESTRUCTION_ONLY_ABOVE, "only_above");

        this.addChart(MainConfig.CFG.MODDING_DISABLE_DURABILITY_FIX, "durability_fix");
        this.addChart(MainConfig.CFG.PLACED_BLOCKS_ACTIVE, "placed_blocks");

        this.addChart(MainConfig.CFG.PLUGINS_USE_MCMMO, "use_mcmmo");
        this.addChart(MainConfig.CFG.PLUGINS_USE_JOBS, "use_jobs");
        this.addChart(MainConfig.CFG.GENERAL_USE_PERMISSIONS, "use_permissions");
        this.addChart(MainConfig.CFG.PLUGINS_USE_WORLDGUARD, "use_worldguard");

        TreeAssist.instance.getLogger().info("sending full Metrics! You can deactivate this in the config.yml");
    }

    private void addChart(MainConfig.CFG cfg, String id) {

        this.addCustomChart(new MetricsBase.SimplePie(id, new Callable<String>() {
            @Override
            public String call() throws Exception {
                return String.valueOf(TreeAssist.instance.getMainConfig().getBoolean(cfg));
            }
        }));
    }

    @Override
    protected JsonArray calculateCharts() {
        JsonArray customCharts = new JsonArray();
        for (MetricsBase.CustomChart customChart : charts) {
            // Add the data of the custom charts
            JsonObject chart = customChart.getRequestJsonObject();
            if (chart == null) { // If the chart is null, we skip it
                continue;
            }
            customCharts.add(chart);
        }
        return customCharts;
    }
}
