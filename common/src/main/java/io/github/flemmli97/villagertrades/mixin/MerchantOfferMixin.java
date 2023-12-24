package io.github.flemmli97.villagertrades.mixin;

import io.github.flemmli97.villagertrades.helper.MerchantOfferMixinInterface;
import net.minecraft.world.item.trading.MerchantOffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantOffer.class)
public class MerchantOfferMixin implements MerchantOfferMixinInterface {

    @Unique
    private boolean villagerTrades_infinite;
    @Unique
    private int villagerTrades_maxUses;

    @Shadow
    private int uses;
    @Shadow
    @Mutable
    private int maxUses;

    @Override
    public void setInfinite(boolean flag) {
        if (flag) {
            this.villagerTrades_maxUses = this.maxUses;
            this.villagerTrades_infinite = true;
            this.uses = 0;
        } else {
            this.maxUses = this.villagerTrades_maxUses;
            this.villagerTrades_infinite = false;
        }
    }

    @Override
    public boolean isInfinite() {
        return this.villagerTrades_infinite;
    }

    @Inject(method = "increaseUses", at = @At("HEAD"), cancellable = true)
    private void onIncrease(CallbackInfo info) {
        if (this.villagerTrades_infinite)
            info.cancel();
    }
}
