package dk.magnusjensen.cinematicbuildings;

import dk.magnusjensen.cinematicbuildings.model.BuildingLayer;
import dk.magnusjensen.cinematicbuildings.model.CinematicBuildingModel;
import net.minecraft.server.level.ServerLevel;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BuildLayersRunnable implements Runnable {

    private final ServerLevel level;
    private final CinematicBuildingModel model;
    private final int layerIntervalInSeconds;
    private final ScheduledExecutorService scheduler;
    private int currentLayerIndex = 0;

    public BuildLayersRunnable(ServerLevel level, CinematicBuildingModel model, int layerIntervalInSeconds, int initialDelayInSeconds) {
        this.level = level;
        this.model = model;
        this.layerIntervalInSeconds = layerIntervalInSeconds;
        this.scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(this, initialDelayInSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        if (currentLayerIndex < model.getLayerCount()) {
            BuildingLayer layer = model.layers().get(currentLayerIndex);

            // Place blocks for the current layer
            for (var entry : layer.blockStates().entrySet()) {
                level.setBlockAndUpdate(entry.getKey(), entry.getValue());
            }

            currentLayerIndex++;

            // Schedule the next layer
            if (currentLayerIndex < model.getLayerCount()) {
                scheduler.schedule(this, layerIntervalInSeconds, TimeUnit.SECONDS);
            } else {
                // Shutdown the scheduler when all layers are done
                scheduler.shutdownNow();
                CommonClass.ACTIVE_RUNNABLES.remove(model.name()); // remove ourselves so we can clean this up.
            }
        }
    }
}
