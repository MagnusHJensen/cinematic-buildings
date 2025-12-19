package dk.magnusjensen.cinematicbuildings;

import dk.magnusjensen.cinematicbuildings.commands.CommandHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class FabricCinematicBuildings implements ModInitializer {
    
    @Override
    public void onInitialize() {

        CommonClass.init();

        CommandRegistrationCallback.EVENT.register((commandDispatcher, commandBuildContext, commandSelection) -> {
            CommandHandler.register(commandDispatcher);
        });
    }
}
