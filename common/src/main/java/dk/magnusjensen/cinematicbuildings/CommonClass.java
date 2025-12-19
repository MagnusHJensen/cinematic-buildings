package dk.magnusjensen.cinematicbuildings;

import dk.magnusjensen.cinematicbuildings.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;


public class CommonClass {

    public static Map<String, BuildLayersRunnable> ACTIVE_RUNNABLES = new HashMap<>();


    public static void init() {

        Constants.LOG.info("Hello from Common init on {}! we are currently in a {} environment!", Services.PLATFORM.getPlatformName(), Services.PLATFORM.getEnvironmentName());
        Constants.LOG.info("The ID for diamonds is {}", BuiltInRegistries.ITEM.getKey(Items.DIAMOND));

        // It is common for all supported loaders to provide a similar feature that can not be used directly in the
        // common code. A popular way to get around this is using Java's built-in service loader feature to create
        // your own abstraction layer. You can learn more about this in our provided services class. In this example
        // we have an interface in the common code and use a loader specific implementation to delegate our call to
        // the platform specific approach.
        if (Services.PLATFORM.isModLoaded("examplemod")) {

            Constants.LOG.info("Hello to examplemod");
        }
    }
}