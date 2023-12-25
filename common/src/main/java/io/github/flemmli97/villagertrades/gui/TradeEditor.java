package io.github.flemmli97.villagertrades.gui;

import io.github.flemmli97.villagertrades.VillagerTrades;
import io.github.flemmli97.villagertrades.config.ConfigHandler;
import io.github.flemmli97.villagertrades.gui.inv.SeparateInv;
import io.github.flemmli97.villagertrades.helper.MerchantOfferMixinInterface;
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
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.List;
import java.util.function.IntPredicate;

public class TradeEditor extends EditableServerOnlyScreenHandler<TradeEditor.Data> {

    public static int OFFERS_PER_PAGE = 8;
    private static final IntPredicate IS_TRADE_SLOT = index -> {
        int mod = index % 9;
        return index < 54 && index > 17 && mod != 2 && mod != 4 && mod != 7;
    };
    private static final IntPredicate IS_EDIT_SLOT = index -> {
        int mod = index % 9;
        return index > 17 && index < 54 && (mod == 2 || mod == 7);
    };

    private int page, maxPages;
    private AbstractVillager villager;
    private final ServerPlayer player;

    protected TradeEditor(int syncId, Inventory playerInventory, Data data) {
        super(syncId, playerInventory, 6, true, IS_TRADE_SLOT, data);
        this.villager = data.villager;
        if (playerInventory.player instanceof ServerPlayer)
            this.player = (ServerPlayer) playerInventory.player;
        else
            throw new IllegalStateException("This is a server side container");
    }

    public static void openGui(ServerPlayer player, AbstractVillager villager) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new TradeEditor(syncId, inv, new Data(villager));
            }

            @Override
            public Component getDisplayName() {
                return villager.getDisplayName();
            }
        };
        player.openMenu(fac);
    }

    public static ItemStack emptyFiller() {
        ItemStack stack = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        stack.setHoverName(Component.literal(""));
        return stack;
    }

    public static ItemStack tradingFiller() {
        ItemStack stack = new ItemStack(Items.LIME_STAINED_GLASS_PANE);
        stack.setHoverName(Component.literal(""));
        return stack;
    }

    public static void playSongToPlayer(ServerPlayer player, SoundEvent event, float vol, float pitch) {
        player.connection.send(
                new ClientboundSoundPacket(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(event), SoundSource.PLAYERS, player.position().x, player.position().y, player.position().z, vol, pitch, player.level().getRandom().nextLong()));
    }

    public static void playSongToPlayer(ServerPlayer player, Holder<SoundEvent> event, float vol, float pitch) {
        player.connection.send(
                new ClientboundSoundPacket(event, SoundSource.PLAYERS, player.position().x, player.position().y, player.position().z, vol, pitch, player.level().getRandom().nextLong()));
    }

    @Override
    protected void fillInventoryWith(Player player, SeparateInv inv, Data data) {
        if (!(player instanceof ServerPlayer))
            return;
        MerchantOffers offers = data.villager.getOffers();
        this.maxPages = offers.size() / OFFERS_PER_PAGE;
        for (int i = 0; i < 54; i++) {
            if (i == 0) {
                ItemStack stack = new ItemStack(Items.BARRIER);
                stack.setHoverName(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.close")).setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
                inv.updateStack(i, stack);
            } else if (i == 1) {
                ItemStack stack = ItemStack.EMPTY;
                if (this.page > 0) {
                    stack = new ItemStack(Items.ARROW);
                    stack.setHoverName(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.previous")).setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
                }
                inv.updateStack(i, stack);
            } else if (i == 8) {
                ItemStack close = ItemStack.EMPTY;
                if (this.page < this.maxPages) {
                    close = new ItemStack(Items.ARROW);
                    close.setHoverName(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.next")).setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
                }
                inv.updateStack(i, close);
            } else if (i / 9 == 1)
                inv.updateStack(i, emptyFiller());
            else if (i > 17) {
                int modI = i % 9;
                if (modI == 4)
                    inv.updateStack(i, emptyFiller());
                else if (modI == 2 || modI == 7)
                    inv.updateStack(i, tradingFiller());
            }
        }
        for (int x = 0; x < OFFERS_PER_PAGE; x++) {
            int idx = x + OFFERS_PER_PAGE * this.page;
            if (idx < offers.size()) {
                MerchantOffer offer = offers.get(idx);
                int firstIdx = 18 + x * 9;
                if (x > 3)
                    firstIdx = 18 + (x - 4) * 9 + 5;
                inv.updateStack(firstIdx, offer.getBaseCostA());
                inv.updateStack(firstIdx + 1, offer.getCostB());
                inv.updateStack(firstIdx + 2, offerEditStack(offer));
                inv.updateStack(firstIdx + 3, offer.getResult());
            }
        }
    }

    private static ItemStack offerEditStack(MerchantOffer offer) {
        ItemStack stack = new ItemStack(Items.LIME_STAINED_GLASS_PANE);
        stack.setHoverName(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.trade.edit"))
                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.AQUA)));
        VillagerTrades.addLore(stack, List.of(
                ((MerchantOfferMixinInterface) offer).isInfinite() ?
                        Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.trade.edit.infinite"))
                                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY))
                        :
                        Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.trade.edit.uses"), offer.getUses(), offer.getMaxUses())
                                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY)),
                Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.trade.edit.xp"), offer.shouldRewardExp(), offer.getXp())
                        .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY)),
                Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.trade.edit.demand"), offer.getDemand())
                        .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY)),
                Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.trade.edit.price"), offer.getPriceMultiplier(), offer.getSpecialPriceDiff())
                        .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY))
        ));
        stack.enchant(Enchantments.UNBREAKING, 1);
        stack.hideTooltipPart(ItemStack.TooltipPart.ENCHANTMENTS);
        return stack;
    }

    private void flipPage() {
        MerchantOffers offers = this.villager.getOffers();
        this.maxPages = offers.size() / OFFERS_PER_PAGE;
        for (int i = 0; i < 54; i++) {
            if (i == 0) {
                ItemStack stack = new ItemStack(Items.BARRIER);
                stack.setHoverName(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.close")).setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
                this.slots.get(i).set(stack);
            } else if (i == 1) {
                ItemStack stack = ItemStack.EMPTY;
                if (this.page > 0) {
                    stack = new ItemStack(Items.ARROW);
                    stack.setHoverName(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.previous")).setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
                }
                this.slots.get(i).set(stack);
            } else if (i == 8) {
                ItemStack next = ItemStack.EMPTY;
                if (this.page < this.maxPages) {
                    next = new ItemStack(Items.ARROW);
                    next.setHoverName(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.next")).setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
                }
                this.slots.get(i).set(next);
            } else if (i / 9 == 1)
                this.slots.get(i).set(emptyFiller());
            else if (i > 17) {
                int modI = i % 9;
                if (modI == 4)
                    this.slots.get(i).set(emptyFiller());
                else if (modI == 2 || modI == 7)
                    this.slots.get(i).set(tradingFiller());
                else
                    this.slots.get(i).set(ItemStack.EMPTY);
            }
        }
        this.broadcastChanges();
        for (int x = 0; x < OFFERS_PER_PAGE; x++) {
            int idx = x + OFFERS_PER_PAGE * this.page;
            if (idx < offers.size()) {
                MerchantOffer offer = offers.get(idx);
                int firstIdx = 18 + x * 9;
                if (x > 3)
                    firstIdx = 18 + (x - 4) * 9 + 5;
                this.slots.get(firstIdx).set(offer.getBaseCostA());
                this.slots.get(firstIdx + 1).set(offer.getCostB());
                this.slots.get(firstIdx + 2).set(offerEditStack(offer));
                this.slots.get(firstIdx + 3).set(offer.getResult());
            }
        }
        this.broadcastChanges();
    }

    public int getOfferIndex(int index) {
        if (index < 18)
            return -1;
        index -= 18;
        int idx = index / 9 + OFFERS_PER_PAGE * this.page;
        if (index % 9 > 4) {
            idx += 4;
        }
        return idx;
    }

    public MerchantOffer getOfferFromSlot(int index) {
        int offerIndex = this.getOfferIndex(index);
        if (offerIndex == -1)
            return null;
        MerchantOffers offers = this.villager.getOffers();
        if (offerIndex < offers.size())
            return offers.get(offerIndex);
        return offers.get(offers.size() - 1);
    }

    public void updateOfferFor(int index) {
        if (index < 18)
            return;
        int idx = index - 18;
        int offerIndex = idx / 9 + OFFERS_PER_PAGE * this.page;
        int firstIdx = index / 9 * 9;
        if (idx % 9 > 4) {
            offerIndex += 4;
            firstIdx += 5;
        }
        MerchantOffers offers = this.villager.getOffers();
        ItemStack first = this.slots.get(firstIdx).getItem();
        ItemStack second = this.slots.get(firstIdx + 1).getItem();
        ItemStack result = this.slots.get(firstIdx + 3).getItem();
        if (first.isEmpty() && second.isEmpty() && result.isEmpty()) {
            if (offerIndex < offers.size()) {
                offers.remove(offerIndex);
            }
        } else {
            MerchantOffer offer;
            if (offerIndex < offers.size()) {
                MerchantOffer current = offers.get(offerIndex);
                offer = new MerchantOffer(first, second, result, current.getUses(), current.getMaxUses(), current.getXp(), current.getPriceMultiplier(), current.getDemand());
                offers.set(offerIndex, offer);
            } else {
                offer = new MerchantOffer(first, second, result, 0, 4, 0, 0, 0);
                offers.add(offer);
            }
            this.slots.get(firstIdx + 2).set(offerEditStack(offer));
        }
        this.flipPage();
        /*this.maxPages = offers.size() / OFFERS_PER_PAGE;
        if (this.page < this.maxPages) {
            ItemStack next = new ItemStack(Items.ARROW);
            next.setHoverName(Component.translatable(ConfigHandler.LANG.get("villagertrades.gui.next")).setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
            this.slots.get(8).set(next);
        } else {
            this.slots.get(8).set(ItemStack.EMPTY);
        }*/
    }

    @Override
    protected boolean handleSlotClicked(ServerPlayer player, int index, Slot slot, int clickType) {
        if (index == 0) {
            player.closeContainer();
            TradeEditor.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 1) {
            this.page--;
            this.flipPage();
            TradeEditor.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 8) {
            this.page++;
            this.flipPage();
            TradeEditor.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (IS_EDIT_SLOT.test(index)) {
            MerchantOffer offer = this.getOfferFromSlot(index);
            if (offer != null) {
                OfferEditor.openGui(player, this.villager, offer);
                TradeEditor.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                return true;
            }
            return false;
        }
        this.updateOfferFor(index);
        return true;
    }

    @Override
    protected boolean isRightSlot(int slot) {
        return slot == 0 || (this.page > 0 && slot == 1) || (this.page < this.maxPages && slot == 8) || IS_TRADE_SLOT.test(slot)
                || IS_EDIT_SLOT.test(slot);
    }

    record Data(AbstractVillager villager) {
    }
}
