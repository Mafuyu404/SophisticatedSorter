package com.sighs.sophisticatedsorter.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sighs.sophisticatedsorter.SophisticatedSorter;
import com.sighs.sophisticatedsorter.client.SearchBox;
import com.sighs.sophisticatedsorter.utils.ClientUtils;
import com.sighs.sophisticatedsorter.utils.CoreUtils;
import com.sighs.sophisticatedsorter.visual.VisualStorageScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.Config;
import net.p3pp3rf1y.sophisticatedcore.client.gui.SortButtonsPosition;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.Button;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinitions;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.TextBox;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ToggleButton;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Consumer;
import java.util.function.Predicate;

@Mixin(value = AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {
    @Shadow protected int leftPos;

    @Shadow protected int imageWidth;

    @Shadow protected int topPos;

    @Shadow protected int inventoryLabelX;

    @Shadow protected int inventoryLabelY;

    protected AbstractContainerScreenMixin(Component p_96550_) {
        super(p_96550_);
    }

    @Unique private boolean isInventoryScreen = false;
    @Unique private boolean canContainerSort = false;
    @Unique private TextBox searchBox;

    @Inject(method = "init", at = @At("RETURN"))
    private void q(CallbackInfo ci) {
        if ((Object) this instanceof CreativeModeInventoryScreen) return;
        if ((Object) this instanceof StorageScreenBase) return;

        var menu = Minecraft.getInstance().player.containerMenu;
        isInventoryScreen = Minecraft.getInstance().screen instanceof InventoryScreen;
        canContainerSort = CoreUtils.canContainerSort(menu);
        if (!canContainerSort && !isInventoryScreen) return;

        SortButtonsPosition sortButtonsPosition = Config.CLIENT.sortButtonsPosition.get();
        if (sortButtonsPosition != SortButtonsPosition.HIDDEN) {
            int x = leftPos + imageWidth - 31;
            int y = topPos + 4;
            var sortButton = new Button(new Position(x, y), ButtonDefinitions.SORT, (button) -> {
                if (button == 0) {
                    CoreUtils.serverSort(menu);
                }
            });
            if (isInventoryScreen) sortButton.setPosition(new Position(leftPos + inventoryLabelX + 137, topPos + inventoryLabelY - 2));
            this.addRenderableWidget(sortButton);

            var toggleButton = new ToggleButton(new Position(x + 12, y), ButtonDefinitions.SORT_BY, (button) -> {
                if (button == 0) {
                    CoreUtils.toggleSortBy();
                }
            }, CoreUtils::getSortBy);
            if (isInventoryScreen) toggleButton.setPosition(new Position(leftPos + inventoryLabelX + 149, topPos + inventoryLabelY - 2));
            this.addRenderableWidget(toggleButton);

            if (!isInventoryScreen) {
                int xEnd = sortButtonsPosition == SortButtonsPosition.TITLE_LINE_RIGHT ? new Position(leftPos + imageWidth - 31, topPos + 4).x() - 1 - leftPos : imageWidth - 7;
                int _width = xEnd - 7;
                searchBox = ClientUtils.createSearchBox(new Position(leftPos + 7, topPos + 5), new Dimension(_width, 10), null);
                this.addRenderableWidget(searchBox);

                var visualScreen = new VisualStorageScreen();

                Consumer<Boolean> transferToStorage = filterByContents -> CoreUtils.serverTransfer(true, filterByContents);
                var transferToStorageButton = ClientUtils.createTransferButton(
                        visualScreen,
                        transferToStorage,
                        ButtonDefinitions.TRANSFER_TO_STORAGE,
                        ButtonDefinitions.TRANSFER_TO_STORAGE_FILTERED
                );
                transferToStorageButton.setPosition(new Position(leftPos + inventoryLabelX + 137, topPos + inventoryLabelY - 2));
                addRenderableWidget(transferToStorageButton);

                Consumer<Boolean> transferToInventory = filterByContents -> CoreUtils.serverTransfer(false, filterByContents);
                var transferToInventoryButton = ClientUtils.createTransferButton(
                        visualScreen,
                        transferToInventory,
                        ButtonDefinitions.TRANSFER_TO_INVENTORY,
                        ButtonDefinitions.TRANSFER_TO_INVENTORY_FILTERED
                );
                transferToInventoryButton.setPosition(new Position(leftPos + inventoryLabelX + 149, topPos + inventoryLabelY - 2));
                addRenderableWidget(transferToInventoryButton);
            }
        }
    }

    @Inject(method = "renderTooltip", at = @At("HEAD"))
    private void tooltip(GuiGraphics guiGraphics, int x, int y, CallbackInfo ci) {
        for (Renderable renderable : this.renderables) {
            if (renderable instanceof Button w) w.renderTooltip(this, guiGraphics, x, y);
            if (renderable instanceof TextBox w) w.renderTooltip(this, guiGraphics, x, y);
        }
    }

    @Inject(method = "mouseClicked", at = @At("RETURN"))
    private void click(double x, double y, int p_97750_, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof StorageScreenBase<?>) return;
        if (getFocused() instanceof TextBox && !getFocused().isMouseOver(x, y)) {
            if (getFocused() instanceof TextBox) getFocused().setFocused(false);
        }
    }

    @Unique
    Predicate<ItemStack> stackPredicate;

    @Inject(method = "tick", at = @At("HEAD"))
    private void searchListener(CallbackInfo ci) {
        if (searchBox != null) {
            if (searchBox.getValue().isEmpty()) stackPredicate = null;
            else stackPredicate = ClientUtils.getStackFilter(searchBox.getValue());
        }
    }

    @Inject(method = "renderSlot", at = @At("HEAD"))
    private void onRenderSlotHead(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        if (stackPredicate != null && !stackPredicate.test(slot.getItem())) {
            RenderSystem.setShaderColor(0.3f, 0.3f, 0.3f, 1.0f);
        }
    }
    @Inject(method = "renderSlot", at = @At("RETURN"))
    private void onRenderSlotReturn(GuiGraphics guiGraphics, Slot slot, CallbackInfo ci) {
        if (stackPredicate != null && !stackPredicate.test(slot.getItem())) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            int x = slot.x;
            int y = slot.y;
            guiGraphics.fill(x, y, x + 16, y + 16, 0x90202020);
        }
    }
}
