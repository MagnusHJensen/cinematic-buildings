package dk.magnusjensen.cinematicbuildings.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dk.magnusjensen.cinematicbuildings.Constants;
import dk.magnusjensen.cinematicbuildings.model.CinematicBuildingModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.HashMap;
import java.util.Map;

public class CinematicBuildingsData extends SavedData {

    public static final SavedDataType<CinematicBuildingsData> ID = new SavedDataType<>(
        Constants.MOD_ID,
        CinematicBuildingsData::new,
        RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Codec.STRING, CinematicBuildingModel.CODEC).fieldOf("buildings").forGetter(data -> data.buildings)
        ).apply(instance, CinematicBuildingsData::new)),
        null
    );

    private Map<String, CinematicBuildingModel> buildings = new HashMap<>();

    public CinematicBuildingsData() {
    }

    public CinematicBuildingsData(Map<String, CinematicBuildingModel> buildings) {
        this.buildings = new HashMap<>(buildings);
    }

    public boolean addBuilding(CinematicBuildingModel building) {
        if (hasBuilding(building.name())) {
            return false; // Do not override another building with same name
        }

        buildings.put(building.name(), building);
        this.setDirty();
        return true;
    }

    public void deleteBuilding(String buildingName) {
        buildings.remove(buildingName);
        this.setDirty();
    }

    // We have an explicit method to avoid accidental overrides.
    public void updateBuilding(CinematicBuildingModel building) {
        buildings.put(building.name(), building);
        this.setDirty();
    }

    public boolean hasBuilding(String name) {
        return buildings.containsKey(name);
    }

    public CinematicBuildingModel getBuilding(String buildingName) {
        return buildings.get(buildingName);
    }

    public Map<String, BlockPos> listBuildings() {
        Map<String, BlockPos> result = new HashMap<>();
        for (var entry : buildings.entrySet()) {
            var box = entry.getValue().boundingBox();
            result.put(entry.getKey(), box.getCenter());
        }
        return result;
    }
}
