package io.github.flemmli97.villagertrades.fabric;

import io.github.flemmli97.villagertrades.TraderCommand;
import io.github.flemmli97.villagertrades.VillagerTrades;
import io.github.flemmli97.villagertrades.config.ConfigHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;

public class VillagerTradesFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        VillagerTrades.updateLoaderImpl(new LoaderImpl());
        CommandRegistrationCallback.EVENT.register(((dispatcher, dedicated, selection) -> TraderCommand.register(dispatcher)));
        ConfigHandler.init();
        VillagerTrades.PERMISSION_API = FabricLoader.getInstance().isModLoaded("fabric-permissions-api-v0");
        VillagerTrades.FTB_RANKS = FabricLoader.getInstance().isModLoaded("ftbranks");
    }
}
