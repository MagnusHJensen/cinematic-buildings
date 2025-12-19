package dk.magnusjensen.cinematicbuildings.model;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public final class CinematicBuildingModel {
    public static final Codec<CinematicBuildingModel> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.fieldOf("name").forGetter(CinematicBuildingModel::name),
        BoundingBox.CODEC.fieldOf("bounding_box").forGetter(CinematicBuildingModel::boundingBox),
        BuildingLayer.CODEC.fieldOf("initial_layer").forGetter(CinematicBuildingModel::initialLayer),
        BuildingLayer.CODEC.listOf().fieldOf("layers").forGetter(CinematicBuildingModel::layers)
    ).apply(instance, CinematicBuildingModel::new));

    private final String name;
    private final BoundingBox boundingBox;
    private BuildingLayer initialLayer; // What the scene will be set to before building starts (so as soon /run is used)
    private final List<BuildingLayer> layers;

    // Since building layer is just a map of block pos -> block states we can use it for the current state tracking
    private BuildingLayer currentState;

    public CinematicBuildingModel(String name, BoundingBox boundingBox, BuildingLayer initialLayer, List<BuildingLayer> layers) {
        this.name = name;
        this.boundingBox = boundingBox;
        this.initialLayer = initialLayer;
        this.layers = new ArrayList<>(layers);

        this.currentState = calculateDiffLayerBasedOnCurrent(initialLayer);
        // Loop over all layers coming in to get it to the latest state
        for (var layer : layers) {
            calculateDiffLayerBasedOnCurrent(layer);
        }
    }

    public void addLayer(BuildingLayer layer) {
        layers.add(this.calculateDiffLayerBasedOnCurrent(layer));
    }

    public int getLayerCount() {
        return layers.size();
    }

    public String name() {
        return name;
    }

    public BoundingBox boundingBox() {
        return boundingBox;
    }

    public List<BuildingLayer> layers() {
        return layers;
    }

    public BuildingLayer initialLayer() {
        return initialLayer;
    }

    private BuildingLayer calculateDiffLayerBasedOnCurrent(BuildingLayer layer) {
        if (this.currentState == null) {
            return new BuildingLayer(new HashMap<>(layer.blockStates())); // We copy the contents to avoid references
        }

        // Calculate the new current state based on the current blocks and all the new incoming blocks in the layer
        // Lets assume it's the full layer for now (so we need to calculate the diff and apply it to the current state, as well as return a new layer with the diff changes)
        BuildingLayer diffLayer = new BuildingLayer();
        for (var entry : layer.blockStates().entrySet()) {
            var pos = entry.getKey();
            var newState = entry.getValue();
            var currentStateAtPos = this.currentState.blockStates().get(pos);
            if (!Objects.equals(currentStateAtPos, newState)) {
                // State has changed, add to diff layer
                diffLayer.addBlock(pos, newState);
                // Update current state
                this.currentState.addBlock(pos, newState);
            }
        }

        return diffLayer;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CinematicBuildingModel) obj;
        return Objects.equals(this.name, that.name) &&
            Objects.equals(this.boundingBox, that.boundingBox) &&
            Objects.equals(this.layers, that.layers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, boundingBox, layers);
    }

    @Override
    public String toString() {
        return "CinematicBuildingModel[" +
            "name=" + name + ", " +
            "boundingBox=" + boundingBox + ", " +
            "layers=" + layers + ']';
    }

}
