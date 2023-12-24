package io.github.flemmli97.villagertrades.mixin;

import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MerchantOffer.class)
public interface MerchantOfferAccessor {

    @Accessor("uses")
    void setUses(int uses);

    @Accessor("maxUses")
    void setMaxUses(int maxUses);

    @Accessor("rewardExp")
    void setRewardExp(boolean rewardXp);

    @Accessor("specialPriceDiff")
    void setSpecialPriceDiff(int specialPriceDiff);

    @Accessor("demand")
    void setDemand(int demand);

    @Accessor("priceMultiplier")
    void setPriceMultiplier(float priceMultiplier);

    @Accessor("xp")
    void setXp(int xp);
}
