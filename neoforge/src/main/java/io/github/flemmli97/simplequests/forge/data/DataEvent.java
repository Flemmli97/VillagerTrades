package io.github.flemmli97.simplequests.forge.data;

import io.github.flemmli97.villagertrades.VillagerTrades;
import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = VillagerTrades.MODID, bus = EventBusSubscriber.Bus.MOD)
public class DataEvent {

    @SubscribeEvent
    public static void data(GatherDataEvent event) {
        DataGenerator data = event.getGenerator();
        ENLangGen enLang = new ENLangGen(data.getPackOutput());
        data.addProvider(event.includeServer(), enLang);
    }

}
