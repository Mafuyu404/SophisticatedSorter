package com.sighs.sophisticatedsorter.visual;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;

public class VisualStorageScreen extends StorageScreenBase {
    public VisualStorageScreen() {
        super(new VisualStorageContainerMenu(Minecraft.getInstance().player), Minecraft.getInstance().player.getInventory(), Component.empty());
    }

    @Override
    protected String getStorageSettingsTabTooltip() {
        return "";
    }

    public void setLeftPos(int value) {
        leftPos = value;
    }
}
