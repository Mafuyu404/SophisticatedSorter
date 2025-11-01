package com.sighs.sophisticatedsorter;

import net.minecraft.client.gui.screens.Screen;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = SophisticatedSorter.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static ForgeConfigSpec.ConfigValue<String> SORT_BY;
    public static ForgeConfigSpec.ConfigValue<Boolean> Filter;
    public static ForgeConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;

    static {
        BUILDER.push("Sorter Setting");

        SORT_BY = BUILDER
                .comment("name, mod, count, tags")
                .define("SortBy", "name");
        Filter = BUILDER
                .comment("Only valid for containers with more than 10 slots if true.")
                .define("Filter", true);
        BLACKLIST = BUILDER
                .comment("Blacklist of screens.")
                .defineList("Blacklist",
                        List.of(),
                        entry -> entry instanceof String
                );

        SPEC = BUILDER.build();
    }
}
