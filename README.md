# War's FTB Dynmap Integration Mod

This mod provides integration between [FTB Utilities](https://minecraft.curseforge.com/projects/ftb-utilities) and [Dynmap](https://minecraft.curseforge.com/projects/dynmapforge?gameCategorySlug=mc-mods&projectID=59433) to
show land claims on the map.

This mod depends on both FTB Utilities and Dynmap being installed to operate. You only need to install this mod on the
**Server Side**.

## Testing

Below is a list of versions tested with this mod. It should work with other versions of similar minor version variants
however there is no guarantee, you will have to test your setup. If you experience issues please provide details in
a bug report.

### Known Working Versions

- FTBLibs 5.3.0.56
- FTB Utilities 5.3.0.52
- Dynmap 3.0-beta-1-forge

## Configuration

On first load of the mod a config file **config/warsftbdynmap.cfg" will be created with customizable options for the mod.
Comments in the config file explain what each option does. You are not required to change any config options this mod
will work by default on startup.

### Example config
```
# Configuration file

general {
    # The name of the layer on the Dynmap web page
    S:claims_layer_name=Land Claims

    # If true the mod will output more debug information to the console
    B:debug_mode=false

    # The opacity level of the claim border
    # Min: 0.0
    # Max: 1.0
    D:dynmap_border_opacity=0.8

    # The weight of the claim border line
    # Min: 0
    # Max: 10
    I:dynmap_border_weight=3

    # The fill color of the claims if "enable_team_colors" is disabled
    S:dynmap_fill_color=FF0000

    # The opacity level of the claim fill
    # Min: 0.0
    # Max: 1.0
    D:dynmap_fill_opacity=0.35

    # If true, Dynmap will show claims with the same colors as the FTB teams color
    B:enable_team_colors=true
}

```