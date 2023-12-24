package io.github.flemmli97.villagertrades.mixin;

import io.github.flemmli97.villagertrades.helper.VillagerMixinInterface;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(AbstractVillager.class)
public class AbstractVillagerMixin implements VillagerMixinInterface {

    @Shadow
    protected MerchantOffers offers;

    @Override
    public void updateOffers(MerchantOffers offers) {
        this.offers = offers;
    }
}
