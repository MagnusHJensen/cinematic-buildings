package dk.magnusjensen.cinematicbuildings;


import dk.magnusjensen.cinematicbuildings.commands.CommandHandler;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

@Mod(Constants.MOD_ID)
@EventBusSubscriber(modid = Constants.MOD_ID)
public class NeoforgeCinematicBuildings {

    public NeoforgeCinematicBuildings(IEventBus eventBus) {

        CommonClass.init();
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandHandler.register(event.getDispatcher());
    }
}