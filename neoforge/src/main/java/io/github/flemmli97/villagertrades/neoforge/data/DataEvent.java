package io.github.flemmli97.villagertrades.neoforge.data;

import io.github.flemmli97.villagertrades.VillagerTrades;
import net.minecraft.data.DataGenerator;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;

@EventBusSubscriber(modid = VillagerTrades.MODID)
public class DataEvent {

    @SubscribeEvent
    public static void data(GatherDataEvent.Server event) {
        DataGenerator data = event.getGenerator();
        ENLangGen enLang = new ENLangGen(data.getPackOutput());
        data.addProvider(true, enLang);
    }

}
