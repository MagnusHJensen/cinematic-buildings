package dk.magnusjensen.cinematicbuildings.model;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

public record BuildingLayer(Map<BlockPos, BlockState> blockStates) {
    public static final Codec<BuildingLayer> CODEC = Codec.unboundedMap(  Codec.STRING.xmap(
            // Decode: "x,y,z" -> BlockPos
            s -> {
                try {
                    String[] parts = s.split(",");
                    return new BlockPos(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                } catch (Exception e) {
                    return BlockPos.ZERO; // fallback
                }
            },
            // Encode: BlockPos -> "x,y,z"
            pos -> pos.getX() + "," + pos.getY() + "," + pos.getZ()
        ), BlockState.CODEC)
        .xmap(BuildingLayer::new, BuildingLayer::blockStates);


    public BuildingLayer() {
        this(new HashMap<>());
    }

    public void addBlock(BlockPos pos, BlockState state) {
        blockStates.put(pos, state);
    }
}
