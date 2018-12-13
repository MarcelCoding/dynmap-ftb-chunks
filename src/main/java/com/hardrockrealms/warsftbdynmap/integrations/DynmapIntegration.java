package com.hardrockrealms.warsftbdynmap.integrations;

import com.hardrockrealms.warsftbdynmap.data.TeamDimInfo;
import com.hardrockrealms.warsftbdynmap.WarsFtbDynmapConfig;
import com.hardrockrealms.warsftbdynmap.data.PositionPoint;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.Logger;
import org.dynmap.DynmapCommonAPI;
import org.dynmap.DynmapCommonAPIListener;
import org.dynmap.markers.AreaMarker;
import org.dynmap.markers.MarkerAPI;
import org.dynmap.markers.MarkerSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * This is a interface class to Dynmap, all calls that effect Dynmap should come through here.
 */

public class DynmapIntegration {
    private DynmapCommonAPI m_DynmapApi = null;
    private MarkerAPI m_DynmapMarkerApi = null;
    private MarkerSet m_DynmapMarkerSet = null;
    private Logger logger = null;
    private Map<Integer, String> m_DimensionNames = new HashMap<>();

    private static final Pattern FORMATTING_COLOR_CODES_PATTERN = Pattern.compile("(?i)\\u00a7[0-9A-FK-OR]");


    /**
     * This is a call back class which Dynmap will call when it is ready to accept API requests. This is
     * also where we get the API object reference from.
     */
    private class DynmapAPIListener extends DynmapCommonAPIListener {
        @Override
        public void apiEnabled(DynmapCommonAPI api) {
            if (api != null)
            {
                m_DynmapApi = api;
                m_DynmapMarkerApi = m_DynmapApi.getMarkerAPI();

                createDynmapClaimMarkerLayer();
            }
        }
    }

    /**
     * Initialize the integration class.
     * @param log Logger to be used by this class.
     */

    public DynmapIntegration(Logger log) {
        logger = log;
        DynmapCommonAPIListener.register(new DynmapAPIListener());
    }

    /**
     * This creates a marker layer in Dynmap for the claims to be displayed on.
     */

    private void createDynmapClaimMarkerLayer() {
        // Create / update a Dynmap Layer for claims
        m_DynmapMarkerSet = m_DynmapMarkerApi.getMarkerSet("warsftbdynmap.claims.markerset");

        if(m_DynmapMarkerSet == null)
            m_DynmapMarkerSet = m_DynmapMarkerApi.createMarkerSet("warsftbdynmap.claims.markerset", WarsFtbDynmapConfig.claims_layer_name, null, false);
        else
            m_DynmapMarkerSet.setMarkerSetLabel(WarsFtbDynmapConfig.claims_layer_name);
    }

    /**
     * This creates a single claim marker in Dynmap.
     * @param teamDim Defines the team and dimension this claim marker is for
     * @param groupIndex Defines the index number for how many claims this team has
     * @param perimeterPoints A list of X Z points representing the perimeter of the claim to draw.
     */

    public void createAreaMarker(TeamDimInfo teamDim, int groupIndex, List<PositionPoint> perimeterPoints)
    {
        String worldName = getWorldName(teamDim.getDim());
        String markerID = worldName + "_" + teamDim.getTeamID() + "_" + groupIndex;

        double[] xList = new double[perimeterPoints.size()];
        double[] zList = new double[perimeterPoints.size()];

        for (int index = 0; index < perimeterPoints.size(); index++) {
            xList[index] = perimeterPoints.get(index).getX();
            zList[index] = perimeterPoints.get(index).getY();
        }

        // Build the data going in to the Dynmap tooltip
        String stToolTip = "<div class=\"infowindow\">";

        stToolTip += "<div style=\"text-align: center;\"><span style=\"font-weight:bold;\">" + teamDim.getTeamTitle() + "</span></div>";

        if (!teamDim.getTeamDescription().isEmpty()) {
            stToolTip += "<div style=\"text-align: center;\"><span>" + teamDim.getTeamDescription() + "</span></div>";
        }

        List<String> teamMembers = FTBUtilitiesIntegration.getTeamMembers(teamDim.getTeamID());

        if (teamMembers.size() > 0) {
            stToolTip += "<br><div style=\"text-align: center;\"><span style=\"font-weight:bold;\"><i>Team Members</i></span></div>";

            for (String member : teamMembers) {
                stToolTip += "<div style=\"text-align: center;\"><span>" + stripColorCodes(member) + "</span></div>";
            }
        }

        stToolTip += "</div>";

        // Create the area marker for the claim
        AreaMarker marker = m_DynmapMarkerSet.createAreaMarker(markerID, stToolTip, true, worldName, xList, zList, false);

        // Configure the marker style
        if (marker != null) {
            int nStrokeWeight = WarsFtbDynmapConfig.dynmap_border_weight;
            double dStrokeOpacity = WarsFtbDynmapConfig.dynmap_border_opacity;
            double dFillOpacity = WarsFtbDynmapConfig.dynmap_fill_opacity;
            int nFillColor = Integer.parseInt(WarsFtbDynmapConfig.dynmap_fill_color, 16);

            if (WarsFtbDynmapConfig.enable_team_colors) {
                nFillColor = teamDim.getTeamColor();
            }

            marker.setLineStyle(nStrokeWeight, dStrokeOpacity, nFillColor);
            marker.setFillStyle(dFillOpacity, nFillColor);
        }
        else {
            logger.error("Failed to create Dynmap area marker for claim.");
        }
    }

    /**
     * Find all the markers for the specified team and clear them.
     * @param teamDim Name of team and dimension you want to clear the markers for.
     */

    public void clearAllTeamMarkers(TeamDimInfo teamDim) {
        if (m_DynmapMarkerSet != null) {
            String worldName = getWorldName(teamDim.getDim());

            int nMarkerID = 0;
            AreaMarker areaMarker = null;
            do {
                String markerID = worldName + "_" + teamDim.getTeamID() + "_" + nMarkerID;
                areaMarker = m_DynmapMarkerSet.findAreaMarker(markerID);

                if (areaMarker != null && areaMarker.getWorld() == worldName) {
                    areaMarker.deleteMarker();
                }

                nMarkerID++;
            } while (areaMarker != null);
        }
    }

    /**
     * Build a list of dimension names which are compatible with how Dynmap makes its names.
     *
     * Note: This method needs to be called prior to any worlds being unloaded.
     */

    public void buildDynmapWorldNames() {
        WorldServer[] worldsList = FMLCommonHandler.instance().getMinecraftServerInstance().worlds;

        // This code below follows Dynmap's naming which is required to get mapping between dimensions and worlds
        // to work. As dynmap API takes world strings not dimension numbers.
        for (WorldServer world : worldsList) {
            DimensionType dimType = world.provider.getDimensionType();

            if (dimType == DimensionType.OVERWORLD) {
                m_DimensionNames.put(dimType.getId(), world.getWorldInfo().getWorldName());
            }
            else {
                m_DimensionNames.put(dimType.getId(), "DIM" + dimType.getId());
            }
        }

        if (WarsFtbDynmapConfig.debug_mode){
            logger.info("Building Dynmap compatible world name list");

            for (Map.Entry<Integer, String> entry : m_DimensionNames.entrySet()) {
                logger.info("  --> Dimension [{}] = {}", entry.getKey(), entry.getValue());
            }
        }

    }

    /**
     * Helper method to return the name of the world based on the dimension ID.
     *
     * @param dim The dimension ID you want the name for
     * @return Returns the string name of the dimension
     */

    private String getWorldName(int dim) {
        String worldName = "";

        if (m_DimensionNames.containsKey(dim)) {
            worldName = m_DimensionNames.get(dim);
        }

        return  worldName;
    }

    /**
     * @param text Text with color codes
     * @return Removes color codes from text strings and returns the raw text
     */

    public static String stripColorCodes(String text) {
        return text.isEmpty() ? text :FORMATTING_COLOR_CODES_PATTERN.matcher(text).replaceAll("");
    }

}
