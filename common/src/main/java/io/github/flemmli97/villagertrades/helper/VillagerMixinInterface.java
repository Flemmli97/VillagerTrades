package io.github.flemmli97.villagertrades.helper;

import net.minecraft.world.item.trading.MerchantOffers;

public interface VillagerMixinInterface {

    void villagerTrades$updateOffers(MerchantOffers offers);
}
