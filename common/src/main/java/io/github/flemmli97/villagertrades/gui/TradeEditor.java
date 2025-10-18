package io.github.flemmli97.villagertrades.gui;

import io.github.flemmli97.villagertrades.helper.MerchantOfferMixinInterface;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.IntPredicate;

public class TradeEditor extends EditableServerOnlyScreenHandler {

    public static int OFFERS_PER_PAGE = 8;
    private static final IntPredicate IS_TRADE_SLOT = index -> {
        int mod = index % 9;
        return index <= 54 && index > 17 && mod != 2 && mod != 4 && mod != 7;
    };
    private static final IntPredicate IS_EDIT_SLOT = index -> {
        int mod = index % 9;
        return index > 17 && index < 54 && (mod == 2 || mod == 7);
    };

    private int page, maxPages;
    private final AbstractVillager villager;

    private List<MerchantOffer> currentOffers;
    private boolean changed;

    protected TradeEditor(int syncId, Inventory playerInventory, AbstractVillager villager) {
        super(syncId, playerInventory, 6, true, IS_TRADE_SLOT);
        this.villager = villager;
        this.updatePage(true);
    }

    public static void openGui(ServerPlayer player, AbstractVillager villager) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                return new TradeEditor(syncId, inv, villager);
            }

            @Override
            public Component getDisplayName() {
                return villager.getDisplayName();
            }
        };
        player.openMenu(fac);
    }

    public static void openFrom(ServerPlayer player, MerchantDataBacktrack offerEditor) {
        MenuProvider fac = new MenuProvider() {
            @Override
            public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
                MerchantData data = offerEditor.getMerchantData();
                TradeEditor editor = new TradeEditor(syncId, inv, data.villager());
                editor.page = data.page();
                editor.currentOffers = data.currentOffers();
                editor.changed = data.changed;
                editor.updateOfferSlots();
                return editor;
            }

            @Override
            public Component getDisplayName() {
                return offerEditor.getMerchantData().villager().getDisplayName();
            }
        };
        player.openMenu(fac);
    }

    public static ItemStack emptyFiller() {
        ItemStack stack = new ItemStack(Items.GRAY_STAINED_GLASS_PANE);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(""));
        return stack;
    }

    public static ItemStack tradingFiller() {
        ItemStack stack = new ItemStack(Items.YELLOW_STAINED_GLASS_PANE);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(""));
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

    public static OfferState validateTrade(MerchantOffer offer) {
        if (offer == null)
            return OfferState.NONE;
        return !offer.getBaseCostA().isEmpty() && !offer.getResult().isEmpty() ? OfferState.VALID : OfferState.INVALID;
    }

    public static boolean offerEq(MerchantOffer first, MerchantOffer sec) {
        if (first != null && sec != null)
            return first.getItemCostA() == sec.getItemCostA() // Copy uses same instance so should be fine
                    && first.getItemCostB() == sec.getItemCostB()
                    && ItemStack.isSameItemSameComponents(first.getResult(), sec.getResult());
        return false;
    }

    private static ItemStack offerEditStack(MerchantOffer offer, RegistryAccess registryAccess, OfferState valid) {
        ItemStack stack = new ItemStack(valid == OfferState.VALID ? Items.LIME_STAINED_GLASS_PANE : Items.ORANGE_STAINED_GLASS_PANE);
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable("villagertrades.gui.trade.edit")
                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.AQUA)));
        List<Component> lore = new ArrayList<>();
        if (valid == OfferState.INVALID)
            lore.add(Component.translatable("villagertrades.gui.offer.tooltip.invalid")
                    .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.DARK_RED)));
        lore.addAll(List.of(
                ((MerchantOfferMixinInterface) offer).isInfinite() ?
                        Component.translatable("villagertrades.gui.trade.edit.infinite")
                                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY))
                        :
                        Component.translatable("villagertrades.gui.trade.edit.uses", offer.getUses(), offer.getMaxUses())
                                .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY)),
                Component.translatable("villagertrades.gui.trade.edit.xp", offer.shouldRewardExp(), offer.getXp())
                        .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY)),
                Component.translatable("villagertrades.gui.trade.edit.demand", offer.getDemand())
                        .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY)),
                Component.translatable("villagertrades.gui.trade.edit.price", offer.getPriceMultiplier(), offer.getSpecialPriceDiff())
                        .setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.GRAY))
        ));
        stack.set(DataComponents.LORE, new ItemLore(lore));
        stack.enchant(registryAccess.registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(Enchantments.UNBREAKING), 1);
        stack.set(DataComponents.ENCHANTMENTS, stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY)
                .withTooltip(false));
        return stack;
    }

    private static ItemCost costOf(ItemStack stack) {
        return new ItemCost(stack.getItemHolder(), stack.getCount(),
                DataComponentPredicate.allOf(PatchedDataComponentMap.fromPatch(DataComponentMap.EMPTY, stack.getComponentsPatch())));
    }

    private void updatePage(boolean init) {
        for (int i = 0; i < 54; i++) {
            if (i == 0) {
                ItemStack stack = new ItemStack(Items.BARRIER);
                stack.set(DataComponents.CUSTOM_NAME, Component.translatable("villagertrades.gui.close").setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
                this.slots.get(i).set(stack);
            } else if (i == 4 && this.villager instanceof Villager) {
                ItemStack stack = new ItemStack(Items.SUNFLOWER);
                stack.set(DataComponents.CUSTOM_NAME, Component.translatable("villagertrades.gui.villager.edit.data").setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
                this.slots.get(i).set(stack);
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
        if (init) {
            MerchantOffers offers = this.villager.getOffers();
            this.currentOffers = new ArrayList<>();
            for (MerchantOffer offer : offers) {
                this.currentOffers.add(offer.copy());
            }
        }
        this.updatePageInfo();
        this.updateOfferSlots();
        this.broadcastChanges();
    }

    protected void updatePageInfo() {
        this.maxPages = (this.currentOffers.size() / OFFERS_PER_PAGE) - 1;
        // Add a new page only if all current trades on the page are full and valid
        int currentEntries = 0;
        for (int i = 0; i < OFFERS_PER_PAGE; i++) {
            int offerIdx = i + OFFERS_PER_PAGE * this.page;
            if (offerIdx >= this.currentOffers.size())
                break;
            if (validateTrade(this.currentOffers.get(offerIdx)) == OfferState.VALID)
                currentEntries++;
        }
        if (currentEntries >= OFFERS_PER_PAGE)
            this.maxPages += 1;
        ItemStack stack = ItemStack.EMPTY;
        if (this.page > 0) {
            stack = new ItemStack(Items.ARROW);
            stack.set(DataComponents.CUSTOM_NAME, Component.translatable("villagertrades.gui.previous").setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
        }
        this.slots.get(1).set(stack);
        ItemStack next = ItemStack.EMPTY;
        if (this.page < this.maxPages) {
            next = new ItemStack(Items.ARROW);
            next.set(DataComponents.CUSTOM_NAME, Component.translatable("villagertrades.gui.next").setStyle(Style.EMPTY.withItalic(false).applyFormat(ChatFormatting.WHITE)));
        }
        this.slots.get(8).set(next);
    }

    private void updateOfferSlots() {
        for (int i = 0; i < OFFERS_PER_PAGE; i++) {
            int offerIdx = i + OFFERS_PER_PAGE * this.page;
            if (offerIdx >= this.currentOffers.size())
                break;
            int firstIdx = this.getTradeStartSlotIndex(i);
            MerchantOffer offer = this.currentOffers.get(offerIdx);
            if (offer != null) {
                this.slots.get(firstIdx).set(offer.getBaseCostA());
                this.slots.get(firstIdx + 1).set(offer.getCostB());
                this.slots.get(firstIdx + 2).set(offerEditStack(offer, this.villager.level().registryAccess(), validateTrade(offer)));
                this.slots.get(firstIdx + 3).set(offer.getResult());
            } else {
                this.slots.get(firstIdx + 2).set(tradingFiller());
            }
        }
    }

    public int getTradeStartSlotIndex(int index) {
        int firstIdx = 18 + index * 9;
        if (index > 3)
            firstIdx = 18 + (index - 4) * 9 + 5;
        return firstIdx;
    }

    private int getOfferIndex(int slotIdx) {
        if (slotIdx < 18)
            return -1;
        slotIdx -= 18;
        int idx = slotIdx / 9 + OFFERS_PER_PAGE * this.page;
        if (slotIdx % 9 > 4) {
            idx += 4;
        }
        return idx;
    }

    public MerchantOffer getOfferFromSlot(int slotIdx) {
        int offerIndex = this.getOfferIndex(slotIdx);
        if (offerIndex == -1)
            return null;
        if (offerIndex < this.currentOffers.size())
            return this.currentOffers.get(offerIndex);
        return null;
    }

    public void updateOffers() {
        for (int i = 0; i < OFFERS_PER_PAGE; i++) {
            int offerIdx = i + OFFERS_PER_PAGE * this.page;
            MerchantOffer current = offerIdx >= this.currentOffers.size() ? null : this.currentOffers.get(offerIdx);
            int firstIdx = this.getTradeStartSlotIndex(i);
            ItemStack first = this.slots.get(firstIdx).getItem();
            ItemStack second = this.slots.get(firstIdx + 1).getItem();
            ItemStack result = this.slots.get(firstIdx + 3).getItem();
            MerchantOffer offer;
            if (first.isEmpty() && second.isEmpty() && result.isEmpty()) {
                offer = null;
            } else {
                ItemCost firstCost;
                ItemCost secondCost = null;
                if (!first.isEmpty()) {
                    firstCost = costOf(first);
                    secondCost = second.isEmpty() ? null : costOf(second);
                } else {
                    firstCost = costOf(second);
                }
                if (current != null) {
                    offer = new MerchantOffer(firstCost, Optional.ofNullable(secondCost), result, current.getUses(), current.getMaxUses(), current.getXp(), current.getPriceMultiplier(), current.getDemand());
                } else {
                    offer = new MerchantOffer(firstCost, Optional.ofNullable(secondCost), result, 0, 4, 0, 0, 0);
                }
            }
            OfferState state = validateTrade(offer);
            if (state == OfferState.NONE) {
                this.slots.get(firstIdx + 2).set(tradingFiller());
            } else {
                this.slots.get(firstIdx + 2).set(offerEditStack(offer, this.villager.level().registryAccess(), state));
            }
            if (offerIdx >= this.currentOffers.size()) {
                if (offer != null)
                    this.currentOffers.add(offer);
            } else
                this.currentOffers.set(offerIdx, offer);
        }
        this.updatePageInfo();
        this.broadcastChanges();
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
            this.updatePage(false);
            TradeEditor.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 8) {
            this.page++;
            this.updatePage(false);
            TradeEditor.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (index == 4) {
            VillagerDataEditor.openGui(player, this.villager, this.page, this.currentOffers, this.changed);
            TradeEditor.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
            return true;
        }
        if (IS_EDIT_SLOT.test(index)) {
            MerchantOffer offer = this.getOfferFromSlot(index);
            if (offer != null) {
                OfferEditor.openGui(player, this.villager, offer, this.page, this.currentOffers, this.changed);
                TradeEditor.playSongToPlayer(player, SoundEvents.UI_BUTTON_CLICK, 1, 1f);
                return true;
            }
            return false;
        }
        this.updateOffers();
        return true;
    }

    @Override
    public void onDrag(int mouse, ClickType clickType, Player playerEntity) {
        this.updateOffers();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.saveOffers();
    }

    private void saveOffers() {
        MerchantOffers current = this.villager.getOffers();
        MerchantOffers offers = new MerchantOffers();
        for (int i = 0; i < this.currentOffers.size(); i++) {
            MerchantOffer offer = this.currentOffers.get(i);
            if (i < current.size() && !offerEq(offer, current.get(i)))
                this.changed = true;
            if (validateTrade(offer) == OfferState.VALID) {
                offers.add(offer);
            }
        }
        this.changed = this.changed || current.size() != offers.size();
        if (this.changed) {
            this.changed = false;
            if (this.villager instanceof Villager v) {
                if (v.getVillagerXp() == 0)
                    v.setVillagerXp(1); // Prevent resetting
                if (v.getVillagerData().getProfession() == VillagerProfession.NONE || v.getVillagerData().getProfession() == VillagerProfession.NITWIT) {
                    v.setVillagerData(v.getVillagerData().setProfession(VillagerProfession.FARMER));
                }
                this.villager.getOffers().clear();
                this.villager.getOffers().addAll(offers);
            }
        }
    }

    @Override
    protected boolean isRightSlot(int slot, ClickType clickType) {
        if (clickType == ClickType.PICKUP_ALL)
            return true;
        return slot == 0 || slot == 4 || (this.page > 0 && slot == 1) || (this.page < this.maxPages && slot == 8) || IS_TRADE_SLOT.test(slot)
                || IS_EDIT_SLOT.test(slot);
    }

    public enum OfferState {
        NONE,
        INVALID,
        VALID
    }

    public interface MerchantDataBacktrack {

        MerchantData getMerchantData();
    }

    public record MerchantData(AbstractVillager villager, List<MerchantOffer> currentOffers, int page,
                               boolean changed) {

    }
}
