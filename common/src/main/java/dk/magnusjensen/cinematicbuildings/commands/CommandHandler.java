package dk.magnusjensen.cinematicbuildings.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import dk.magnusjensen.cinematicbuildings.BuildLayersRunnable;
import dk.magnusjensen.cinematicbuildings.CommonClass;
import dk.magnusjensen.cinematicbuildings.data.CinematicBuildingsData;
import dk.magnusjensen.cinematicbuildings.model.BuildingLayer;
import dk.magnusjensen.cinematicbuildings.model.CinematicBuildingModel;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.ArrayList;

public class CommandHandler {
    public static final String COMMAND_ROOT = "cb";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        // For now, we register directly here, to keep it collected, and we later know of a better separation
        dispatcher.register(
            Commands.literal(COMMAND_ROOT)
                .requires(ctx -> {
                    try {
                        return ctx.getPlayerOrException().permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.MODERATORS));
                    } catch (CommandSyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }) // Require at least level 2 (command blocks / cheats)
                .then(Commands.literal("delete")
                    .then(Commands.argument("building_name", StringArgumentType.string())
                        .executes(context -> {
                            String buildingName = StringArgumentType.getString(context, "building_name");

                            var data = context.getSource().getLevel().getDataStorage().computeIfAbsent(CinematicBuildingsData.ID);
                            if (!data.hasBuilding(buildingName)) {
                                context.getSource().sendFailure(
                                    Component.literal("No building with the name '" + buildingName + "' exists.")
                                );
                                return 0;
                            }

                            data.deleteBuilding(buildingName);
                            context.getSource().sendSuccess(
                                () -> Component.literal("Deleted building '" + buildingName + "'."),
                                false
                            );
                            return 1;
                        })

                ))
                .then(Commands.literal("create")
                    .then(Commands.argument("building_name", StringArgumentType.string())
                    .then(Commands.argument("pos1", BlockPosArgument.blockPos())
                    .then(Commands.argument("pos2", BlockPosArgument.blockPos())
                        .executes(context -> {
                            String buildingName = StringArgumentType.getString(context, "building_name");
                            BlockPos pos1 = BlockPosArgument.getLoadedBlockPos(context, "pos1");
                            BlockPos pos2 = BlockPosArgument.getLoadedBlockPos(context, "pos2");
                            BoundingBox boundingBox = BoundingBox.fromCorners(pos1, pos2);

                            var data = context.getSource().getLevel().getDataStorage().computeIfAbsent(CinematicBuildingsData.ID);

                            if (data.hasBuilding(buildingName)) {
                                context.getSource().sendFailure(
                                    Component.literal("A building with the name '" + buildingName + "' already exists.")
                                );
                                return 0;
                            }

                            var initialLayer = new BuildingLayer();
                            for (int x = boundingBox.minX(); x <= boundingBox.maxX(); x++) {
                                for (int y = boundingBox.minY(); y <= boundingBox.maxY(); y++) {
                                    for (int z = boundingBox.minZ(); z <= boundingBox.maxZ(); z++) {
                                        BlockPos currentPos = new BlockPos(x, y, z);
                                        var blockState = context.getSource().getLevel().getBlockState(currentPos);
                                        initialLayer.addBlock(currentPos, blockState);
                                    }
                                }
                            }

                            data.addBuilding(new CinematicBuildingModel(buildingName, boundingBox, initialLayer, new ArrayList<>()));
                            context.getSource().sendSuccess(
                                () -> Component.literal("Created building '" + buildingName + "' with bounding box " + boundingBox + "."),
                                false
                            );
                            return 1;
                        })
                ))))
                .then(Commands.literal("add")
                    .then(Commands.argument("building_name", StringArgumentType.string())
                        .executes(context -> {
                            String buildingName = StringArgumentType.getString(context, "building_name");
                            var data = context.getSource().getLevel().getDataStorage().computeIfAbsent(CinematicBuildingsData.ID);

                            if (!data.hasBuilding(buildingName)) {
                                context.getSource().sendFailure(
                                    Component.literal("No building with the name '" + buildingName + "' exists.")
                                );
                                return 0;
                            }
                            var building = data.getBuilding(buildingName);

                            var level = context.getSource().getLevel();
                            if (!level.isLoaded(building.boundingBox().getCenter())) {
                                context.getSource().sendFailure(
                                    Component.literal("The building area is not loaded. Please make sure the area is loaded before adding a layer.")
                                );
                                return 0;
                            }

                            // Iterate through all blocks and start constructing a building layer
                            var layer = new BuildingLayer();

                            for (int x = building.boundingBox().minX(); x <= building.boundingBox().maxX(); x++) {
                                for (int y = building.boundingBox().minY(); y <= building.boundingBox().maxY(); y++) {
                                    for (int z = building.boundingBox().minZ(); z <= building.boundingBox().maxZ(); z++) {
                                        BlockPos currentPos = new BlockPos(x, y, z);
                                        var blockState = level.getBlockState(currentPos);
                                        layer.addBlock(currentPos, blockState);
                                    }
                                }
                            }

                            building.addLayer(layer);
                            data.updateBuilding(building);
                            context.getSource().sendSuccess(
                                () -> Component.literal("Added layer " + building.getLayerCount() + " to building '" + buildingName + "'."),
                                false
                            );

                            return 1;
                        })
                ))
                .then(Commands.literal("run")
                    .then(Commands.argument("building_name", StringArgumentType.string())
                    .then(Commands.argument("initial_delay", IntegerArgumentType.integer(1, 60))
                    .then(Commands.argument("delay", IntegerArgumentType.integer(1))
                        .executes(context -> {
                            String buildingName = StringArgumentType.getString(context, "building_name");
                            int initialDelay = IntegerArgumentType.getInteger(context, "initial_delay");
                            int layerDelay = IntegerArgumentType.getInteger(context, "delay");

                            var data = context.getSource().getLevel().getDataStorage().computeIfAbsent(CinematicBuildingsData.ID);
                            if (!data.hasBuilding(buildingName)) {
                                context.getSource().sendFailure(
                                    Component.literal("No building with the name '" + buildingName + "' exists.")
                                );
                                return 0;
                            }


                            if (CommonClass.ACTIVE_RUNNABLES.containsKey(buildingName)) {
                                context.getSource().sendFailure(
                                    Component.literal("A build process for the building '" + buildingName + "' is already running.")
                                );
                                return 0;
                            }

                            var building = data.getBuilding(buildingName);
                            var level = context.getSource().getLevel();

                            var initialLayer = building.initialLayer();

                            for (var entry : initialLayer.blockStates().entrySet()) {
                                BlockPos currentPos = new BlockPos(entry.getKey().getX(), entry.getKey().getY(), entry.getKey().getZ());
                                level.setBlockAndUpdate(currentPos, entry.getValue());
                            }

                            var runnable = new BuildLayersRunnable(level, building, layerDelay, initialDelay);
                            CommonClass.ACTIVE_RUNNABLES.put(buildingName, runnable);

                            return 1;
                        })
                ))))
                .then(Commands.literal("list")
                    .executes(context -> {
                        var data = context.getSource().getLevel().getDataStorage().computeIfAbsent(CinematicBuildingsData.ID);

                        var map = data.listBuildings();

                        if (map.isEmpty()) {
                            context.getSource().sendSuccess(() -> Component.literal("No buildings defined"), false);
                            return 0;
                        }

                        for (var entry : map.entrySet()) {
                            String buildingName = entry.getKey();
                            BlockPos pos = entry.getValue();

                            Component message = Component.literal(buildingName + " at ")
                                .append(Component.literal(pos.toShortString())
                                    .withStyle(style -> style
                                        .withColor(ChatFormatting.GREEN)
                                        .withClickEvent(new ClickEvent.RunCommand(
                                            "/tp " + pos.getX() + " " + pos.getY() + " " + pos.getZ()
                                        ))
                                        .withHoverEvent(new HoverEvent.ShowText(
                                            Component.literal("Click to teleport to " + pos.toShortString())
                                        ))
                                    )
                                );

                            context.getSource().sendSuccess(() -> message, false);
                        }


                        return 1;
                    })
                )
        );
    }
}
