package io.github.flemmli97.villagertrades.gui;

import io.github.flemmli97.villagertrades.VillagerTrades;
import io.github.flemmli97.villagertrades.config.ConfigHandler;
import io.github.flemmli97.villagertrades.gui.inv.SeparateInv;
import io.github.flemmli97.villagertrades.helper.MerchantOfferMixinInterface;
import io.github.flemmli97.villagertrades.mixin.MerchantOfferAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.List;
import java.util.function.BiConsumer;

public class OfferEditor extends ServerOnlyScreenHandler<OfferEditor.Data> {

    private AbstractVillager villager;
    private MerchantOffer offer;

    protected OfferEditor(int syncId, Inventory playerInventory, OfferEditor.Data data) {
        super(syncId, playerInventory, 4, data);
        this.villager = data.villager();
        this.offer = data.offer();
    }

    public static void openGui(ServerPlayer player, AbstractVillager villager, MerchantOffer offer) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new OfferEditor(syncId, inv, new OfferEditor.Data(villager, offer));
            }

            @Override
            public Component getDisplayName() {
                return Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.offer.edit"));
            }
        };
        player.openMenu(fac);
    }

    public static void playSongToPlayer(ServerPlayer player, Holder<SoundEvent> event, float vol, float pitch) {
        player.connection.send(
                new ClientboundSoundPacket(event, SoundSource.PLAYERS, player.position().x, player.position().y, player.position().z, vol, pitch, player.getRandom().nextLong()));
    }

    public static void playSongToPlayer(ServerPlayer player, SoundEvent event, float vol, float pitch) {
        player.connection.send(
                new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(event), SoundSource.PLAYERS, player.position().x, player.position().y, player.position().z, vol, pitch, player.getRandom().nextLong()));
    }

    @Override
    protected void fillInventoryWith(Player player, SeparateInv inv, OfferEditor.Data data) {
        if (!(player instanceof ServerPlayer))
            return;
        this.update(data.offer, inv::updateStack);
    }

    private void update(MerchantOffer offer, BiConsumer<Integer, ItemStack> consumer) {
        for (int i = 0; i < 36; i++) {
            if (i == 0) {
                ItemStack stack = new ItemStack(Items.RED_TERRACOTTA);
                stack.setHoverName(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.back")).setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
                consumer.accept(i, stack);
            } else if (i < 9 || i > 27 || i % 9 == 0 || i % 9 == 8) {
                consumer.accept(i, TradeEditor.emptyFiller());
            }
        }
        ItemStack stack = new ItemStack(Items.WHEAT);
        stack.setHoverName(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.offer.edit.uses")).setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
        VillagerTrades.addLore(stack, List.of(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.offer.tooltip.uses"), offer.getUses())
                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY))));
        consumer.accept(10, stack);
        stack = new ItemStack(Items.EMERALD_ORE);
        stack.setHoverName(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.offer.edit.maxUses")).setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
        VillagerTrades.addLore(stack, List.of(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.offer.tooltip.maxUses"), offer.getMaxUses())
                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY))));
        consumer.accept(19, stack);
        stack = new ItemStack(((MerchantOfferMixinInterface) offer).isInfinite() ? Items.EMERALD_BLOCK : Items.REDSTONE_BLOCK);
        stack.setHoverName(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.offer.edit.infinite")).setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
        VillagerTrades.addLore(stack, List.of(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.offer.tooltip.infinite"), stack.is(Items.EMERALD_BLOCK))
                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY))));
        consumer.accept(12, stack);
        stack = new ItemStack(Items.LAPIS_LAZULI);
        stack.setHoverName(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.offer.edit.rewardExp")).setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
        if(offer.shouldRewardExp()) {
            stack.enchant(Enchantments.UNBREAKING, 1);
            stack.hideTooltipPart(ItemStack.TooltipPart.ENCHANTMENTS);
        }
        VillagerTrades.addLore(stack, List.of(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.offer.tooltip.rewardExp"), offer.shouldRewardExp())
                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY))));
        consumer.accept(21, stack);
        stack = new ItemStack(Items.BOOK);
        stack.setHoverName(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.offer.edit.xp")).setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
        VillagerTrades.addLore(stack, List.of(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.offer.tooltip.xp"), offer.getXp())
                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY))));
        consumer.accept(14, stack);
        stack = new ItemStack(Items.IRON_INGOT);
        stack.setHoverName(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.offer.edit.specialPriceDiff")).setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
        VillagerTrades.addLore(stack, List.of(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.offer.tooltip.specialPriceDiff"), offer.getSpecialPriceDiff())
                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY))));
        consumer.accept(23, stack);
        stack = new ItemStack(Items.LECTERN);
        stack.setHoverName(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.offer.edit.demand")).setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
        VillagerTrades.addLore(stack, List.of(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.offer.tooltip.demand"), offer.getDemand())
                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY))));
        consumer.accept(16, stack);
        stack = new ItemStack(Items.DIAMOND);
        stack.setHoverName(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.offer.edit.priceMultiplier")).setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
        VillagerTrades.addLore(stack, List.of(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.offer.tooltip.priceMultiplier"), offer.getPriceMultiplier())
                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY))));
        consumer.accept(25, stack);
    }

    @Override
    protected boolean handleSlotClicked(ServerPlayer player, int index, Slot slot, int clickType) {
        if (index == 0) {
            TradeEditor.openGui(player, this.villager);
            TradeEditor.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        switch (index) {
            case 10, 19, 23, 16, 14 -> {
                StringResultScreenHandler.createNewStringResult(player, s -> {
                    try {
                        int amount = Integer.parseInt(s);
                        switch (index) {
                            case 10 -> ((MerchantOfferAccessor) this.offer).setUses(amount);
                            case 19 -> ((MerchantOfferAccessor) this.offer).setMaxUses(amount);
                            case 23 -> ((MerchantOfferAccessor) this.offer).setSpecialPriceDiff(amount);
                            case 16 -> ((MerchantOfferAccessor) this.offer).setDemand(amount);
                            case 14 -> ((MerchantOfferAccessor) this.offer).setXp(amount);
                        }
                        ((MerchantOfferAccessor) this.offer).setUses(amount);
                        TradeEditor.playSongToPlayer(player, SoundEvents.ANVIL_USE, 1, 1f);
                    } catch (NumberFormatException e) {
                        TradeEditor.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                    }
                    player.closeContainer();
                    player.getServer().execute(() -> OfferEditor.openGui(player, this.villager, this.offer));
                }, () -> {
                    player.closeContainer();
                    player.getServer().execute(() -> OfferEditor.openGui(player, this.villager, this.offer));
                    TradeEditor.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                });
                return true;
            }
            case 25 -> {
                StringResultScreenHandler.createNewStringResult(player, s -> {
                    try {
                        float amount = Float.parseFloat(s);
                        ((MerchantOfferAccessor) this.offer).setPriceMultiplier(amount);
                        TradeEditor.playSongToPlayer(player, SoundEvents.ANVIL_USE, 1, 1f);
                    } catch (NumberFormatException e) {
                        TradeEditor.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                    }
                    player.closeContainer();
                    player.getServer().execute(() -> OfferEditor.openGui(player, this.villager, this.offer));
                }, () -> {
                    player.closeContainer();
                    player.getServer().execute(() -> OfferEditor.openGui(player, this.villager, this.offer));
                    TradeEditor.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                });
                return true;
            }
            case 12 -> {
                ((MerchantOfferMixinInterface) this.offer).setInfinite(!((MerchantOfferMixinInterface) this.offer).isInfinite());
                TradeEditor.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            }
            case 21 -> {
                ((MerchantOfferAccessor) this.offer).setRewardExp(!this.offer.shouldRewardExp());
                TradeEditor.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            }
        }
        this.update(this.offer, (i, stack) -> this.getSlot(i).set(stack));
        return true;
    }

    @Override
    protected boolean isRightSlot(int slot) {
        return slot == 0 || slot > 9 && slot < 27;
    }

    record Data(AbstractVillager villager, MerchantOffer offer) {
    }
}
