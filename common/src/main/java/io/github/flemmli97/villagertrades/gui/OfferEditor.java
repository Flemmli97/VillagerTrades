package io.github.flemmli97.villagertrades.gui;

import io.github.flemmli97.villagertrades.helper.MerchantOfferMixinInterface;
import io.github.flemmli97.villagertrades.mixin.MerchantOfferAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.MerchantOffer;

import java.util.List;

public class OfferEditor extends ServerOnlyScreenHandler implements TradeEditor.MerchantDataBacktrack {

    public final AbstractVillager villager;
    public final MerchantOffer offer;
    public final int page;
    public final List<MerchantOffer> currentOffers;

    private boolean changed;

    protected OfferEditor(int syncId, Inventory playerInventory, AbstractVillager villager, MerchantOffer offer, int page, List<MerchantOffer> currentOffers, boolean changed) {
        super(syncId, playerInventory, 4);
        this.villager = villager;
        this.offer = offer;
        this.page = page;
        this.currentOffers = currentOffers;
        this.changed = changed;
        this.update();
    }

    public static void openGui(ServerPlayer player, AbstractVillager villager, MerchantOffer offer, int page, List<MerchantOffer> currentOffers, boolean changed) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new OfferEditor(syncId, inv, villager, offer, page, currentOffers, changed);
            }

            @Override
            public Component getDisplayName() {
                return Component.translatable("villagertrades.gui.offer.edit");
            }
        };
        player.openMenu(fac);
    }

    private void update() {
        for (int i = 0; i < 36; i++) {
            if (i == 0) {
                ItemStack stack = new ItemStack(Items.RED_TERRACOTTA);
                stack.set(DataComponents.CUSTOM_NAME, Component.translatable("villagertrades.gui.back").setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
                this.slots.get(i).set(stack);
            } else if (i < 9 || i > 27 || i % 9 == 0 || i % 9 == 8) {
                this.slots.get(i).set(TradeEditor.emptyFiller());
            }
        }
        ItemStack stack = new ItemStack(Items.WHEAT);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("villagertrades.gui.offer.edit.uses").setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.translatable("villagertrades.gui.offer.tooltip.uses", this.offer.getUses())
                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY)))));
        this.slots.get(10).set(stack);
        stack = new ItemStack(Items.EMERALD_ORE);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("villagertrades.gui.offer.edit.maxUses").setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.translatable("villagertrades.gui.offer.tooltip.maxUses", this.offer.getMaxUses())
                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY)))));
        this.slots.get(19).set(stack);
        stack = new ItemStack(((MerchantOfferMixinInterface) this.offer).isInfinite() ? Items.EMERALD_BLOCK : Items.REDSTONE_BLOCK);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("villagertrades.gui.offer.edit.infinite").setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.translatable("villagertrades.gui.offer.tooltip.infinite", Component.translatable(stack.is(Items.EMERALD_BLOCK) ? "villagertrades.gui.true" : "villagertrades.gui.false"))
                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY)))));
        this.slots.get(12).set(stack);
        stack = new ItemStack(Items.LAPIS_LAZULI);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("villagertrades.gui.offer.edit.rewardExp").setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
        if (this.offer.shouldRewardExp()) {
            stack.enchant(this.villager.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.UNBREAKING), 1);
            stack.set(DataComponents.ENCHANTMENTS, stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
                    .withTooltip(false));
        }
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.translatable("villagertrades.gui.offer.tooltip.rewardExp", Component.translatable(this.offer.shouldRewardExp() ? "villagertrades.gui.true" : "villagertrades.gui.false"))
                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY)))));
        this.slots.get(21).set(stack);
        stack = new ItemStack(Items.BOOK);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("villagertrades.gui.offer.edit.xp").setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.translatable("villagertrades.gui.offer.tooltip.xp", this.offer.getXp())
                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY)))));
        this.slots.get(14).set(stack);
        stack = new ItemStack(Items.IRON_INGOT);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("villagertrades.gui.offer.edit.specialPriceDiff").setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.translatable("villagertrades.gui.offer.tooltip.specialPriceDiff", this.offer.getSpecialPriceDiff())
                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY)))));
        this.slots.get(23).set(stack);
        stack = new ItemStack(Items.LECTERN);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("villagertrades.gui.offer.edit.demand").setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.translatable("villagertrades.gui.offer.tooltip.demand", this.offer.getDemand())
                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY)))));
        this.slots.get(16).set(stack);
        stack = new ItemStack(Items.DIAMOND);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("villagertrades.gui.offer.edit.priceMultiplier").setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
        stack.set(DataComponents.LORE, new ItemLore(List.of(Component.translatable("villagertrades.gui.offer.tooltip.priceMultiplier", this.offer.getPriceMultiplier())
                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY)))));
        this.slots.get(25).set(stack);
    }

    @Override
    protected boolean handleSlotClicked(ServerPlayer player, int index, Slot slot, int mouse) {
        if (index == 0) {
            TradeEditor.openFrom(player, this);
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
                        this.changed = true;
                        TradeEditor.playSongToPlayer(player, SoundEvents.ANVIL_USE, 1, 1f);
                    } catch (NumberFormatException e) {
                        TradeEditor.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                    }
                    player.closeContainer();
                    player.getServer().execute(() -> OfferEditor.openGui(player, this.villager, this.offer, this.page, this.currentOffers, this.changed));
                }, () -> {
                    player.closeContainer();
                    player.getServer().execute(() -> OfferEditor.openGui(player, this.villager, this.offer, this.page, this.currentOffers, this.changed));
                    TradeEditor.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                });
                return true;
            }
            case 25 -> {
                StringResultScreenHandler.createNewStringResult(player, s -> {
                    try {
                        float amount = Float.parseFloat(s);
                        ((MerchantOfferAccessor) this.offer).setPriceMultiplier(amount);
                        this.changed = true;
                        TradeEditor.playSongToPlayer(player, SoundEvents.ANVIL_USE, 1, 1f);
                    } catch (NumberFormatException e) {
                        TradeEditor.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                    }
                    player.closeContainer();
                    player.getServer().execute(() -> OfferEditor.openGui(player, this.villager, this.offer, this.page, this.currentOffers, this.changed));
                }, () -> {
                    player.closeContainer();
                    player.getServer().execute(() -> OfferEditor.openGui(player, this.villager, this.offer, this.page, this.currentOffers, this.changed));
                    TradeEditor.playSongToPlayer(player, SoundEvents.VILLAGER_NO, 1, 1f);
                });
                return true;
            }
            case 12 -> {
                ((MerchantOfferMixinInterface) this.offer).setInfinite(!((MerchantOfferMixinInterface) this.offer).isInfinite());
                this.changed = true;
                TradeEditor.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            }
            case 21 -> {
                ((MerchantOfferAccessor) this.offer).setRewardExp(!this.offer.shouldRewardExp());
                this.changed = true;
                TradeEditor.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            }
        }
        this.update();
        return true;
    }

    @Override
    protected boolean isRightSlot(int slot) {
        return slot == 0 || slot > 9 && slot < 27;
    }

    @Override
    public TradeEditor.MerchantData getMerchantData() {
        return new TradeEditor.MerchantData(this.villager, this.currentOffers, this.page, this.changed);
    }
}
