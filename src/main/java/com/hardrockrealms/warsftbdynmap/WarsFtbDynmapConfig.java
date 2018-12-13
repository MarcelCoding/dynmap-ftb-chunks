package com.hardrockrealms.warsftbdynmap;

import net.minecraftforge.common.config.Config;

@Config(
        modid = WarsFtbDynmapMod.MODID,
        name = WarsFtbDynmapMod.MODID
)
public class WarsFtbDynmapConfig {

    @Config.Comment("If true the mod will output more debug information to the console")
    public static boolean debug_mode = false;

    @Config.Comment("The name of the layer on the Dynmap web page")
    public static String claims_layer_name = "Land Claims";

    @Config.Comment("If true, Dynmap will show claims with the same colors as the FTB teams color")
    public static boolean enable_team_colors = true;

    @Config.Comment("The fill color of the claims if \"enable_team_colors\" is disabled")
    public static String dynmap_fill_color = "FF0000";

    @Config.Comment("The opacity level of the claim fill")
    @Config.RangeDouble(min = 0.0, max = 1.0)
    public static Double dynmap_fill_opacity = 0.35;

    @Config.Comment("The opacity level of the claim border")
    @Config.RangeDouble(min = 0.0, max = 1.0)
    public static Double dynmap_border_opacity = 0.80;

    @Config.Comment("The weight of the claim border line")
    @Config.RangeInt(min = 0, max = 10)
    public static int dynmap_border_weight = 3;
}
