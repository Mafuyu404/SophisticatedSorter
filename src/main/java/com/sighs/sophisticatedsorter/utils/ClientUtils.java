package com.sighs.sophisticatedsorter.utils;

import com.sighs.sophisticatedsorter.api.IStorageScreenBase;
import com.sighs.sophisticatedsorter.visual.VisualStorageScreen;
import net.minecraft.world.item.ItemStack;
import net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.Button;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.ButtonDefinition;
import net.p3pp3rf1y.sophisticatedcore.client.gui.controls.TextBox;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Dimension;
import net.p3pp3rf1y.sophisticatedcore.client.gui.utils.Position;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class ClientUtils {
    private static final Logger log = LogManager.getLogger(ClientUtils.class);
    private static Class<?> searchBoxClass;
    private static Class<?> transferButton;

    static {
        try {
            searchBoxClass = Class.forName("net.p3pp3rf1y.sophisticatedcore.client.gui.SearchBox");
            transferButton = Class.forName("net.p3pp3rf1y.sophisticatedcore.client.gui.StorageScreenBase$TransferButton");
        } catch (ClassNotFoundException ignored) {}
    }

    public static TextBox createSearchBox(Object... params) {
        if (searchBoxClass != null) {
            try {
                Constructor<?> constructor = searchBoxClass.getDeclaredConstructor(Position.class, Dimension.class, StorageScreenBase.class);
                constructor.setAccessible(true);
                return (TextBox) constructor.newInstance(params);
            } catch (Exception ignored) {}
        }
        return null;
    }
    public static Button createTransferButton(Object... params) {
        if (transferButton != null) {
            try {
                for (@NotNull Constructor<?> declaredField : transferButton.getDeclaredConstructors()) {
                    System.out.print(declaredField+"\n");
                }
                Constructor<?> constructor = transferButton.getDeclaredConstructor(StorageScreenBase.class, Consumer.class, ButtonDefinition.class, ButtonDefinition.class);
                constructor.setAccessible(true);
                return (Button) constructor.newInstance(params);
            } catch (Exception e) {
                log.error("e: ", e);
            }
        }
        return null;
    }

    public static Predicate<ItemStack> getStackFilter(String string) {
        var faker = new VisualStorageScreen();
        return ((IStorageScreenBase) faker).getVisualStackFilter(string);
    }
}
