package com.hardrockrealms.warsftbdynmap;

import com.hardrockrealms.warsftbdynmap.data.ChunkPosition;
import com.hardrockrealms.warsftbdynmap.data.GroupedChunks;
import com.hardrockrealms.warsftbdynmap.data.PositionPoint;
import com.hardrockrealms.warsftbdynmap.data.TeamDimInfo;
import com.hardrockrealms.warsftbdynmap.integrations.DynmapIntegration;
import com.hardrockrealms.warsftbdynmap.integrations.FTBUtilitiesIntegration;

import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mod(
    modid = WarsFtbDynmapMod.MODID,
    name = WarsFtbDynmapMod.NAME,
    version = WarsFtbDynmapMod.VERSION,
    dependencies = "after:ftbutilities;after:dynmap",
    acceptableRemoteVersions = "*"
)
public class WarsFtbDynmapMod
{
    public static final String MODID = "warsftbdynmap";
    public static final String NAME = "WarsFtbDynmap";
    public static final String VERSION = "1.0";

    private static final String DYNMAP_MODID = "dynmap";
    private static final String FTBUTILITIES_MODID = "ftbutilities";

    public  static Logger logger;
    public static final int TICKS_PER_SEC = 20;
    private static final long TICK_DELAY_TIME = TICKS_PER_SEC * 2;

    private static boolean m_bModEnabled = true;

    private long m_NextTriggerTickCount = 0;
    private int m_InitializeAttemptCount = 0;
    private boolean m_bMapInitialized = false;
    private DynmapIntegration m_DynmapIntegration = null;
    private Set<TeamDimInfo> m_ClaimUpdates = new HashSet<>();

    @Mod.Instance(MODID)
    public static WarsFtbDynmapMod instance;

    @EventHandler
    public void onPreInit(FMLPreInitializationEvent event) {
        logger = LogManager.getLogger(NAME);
        logger.info("Initializing War's FTB Dynmap Integration");

        if (!Loader.isModLoaded(DYNMAP_MODID) && !Loader.isModLoaded(FTBUTILITIES_MODID)) {
            m_bModEnabled = false;
            logger.error("FTB Utilities and or Dynmap does not appear to be installed. Disabling integration.");
        }
    }

    @EventHandler
    public void onInit(FMLInitializationEvent event)
    {
        // Kick off the dynmap API integration process
        if (m_bModEnabled) {
            m_DynmapIntegration = new DynmapIntegration(logger);
        }
    }

    @EventHandler
    public void onServerStarted(FMLServerStartedEvent event)
    {
        // We need to load in and determine the world names at this point
        if (m_bModEnabled) {
            if (m_DynmapIntegration != null) {
                m_DynmapIntegration.buildDynmapWorldNames();
            }
        }
    }

    /**
     * Method to queue up team claim event updates to be processed at a later time. Multiple updates for the same
     * team are combined in to a single update.
     *
     * @param teamDim The team and dimension the claim update is for.
     */

    public void queueClaimEventReceived(TeamDimInfo teamDim) {
        if (m_bModEnabled) {

            if (WarsFtbDynmapConfig.debug_mode) {
                logger.info("Claim update notification received for team [{}] in Dimension [{}]", teamDim.getTeamID(), teamDim.getDim());
            }

            m_ClaimUpdates.add(teamDim);
        }
    }

    /**
     * Updates all the claims in Dynamp for the specified team in the specified dimension.
     * @param teamDim The team and dimension to update claims for.
     */

    private void updateTeamClaims(TeamDimInfo teamDim) {
        long startTimeNS = 0;
        long totalChunks = 0;
        long totalGroups = 0;

        if (WarsFtbDynmapConfig.debug_mode){
            startTimeNS = System.nanoTime();
            logger.info("Claim update started for team [{}] in Dimension [{}]", teamDim.getTeamID(), teamDim.getDim());
        }

        Set<ChunkPosition> teamClaimsList = FTBUtilitiesIntegration.getTeamClaimedChunks(teamDim);
        totalChunks = teamClaimsList.size();

        // Build a list of groups of claim chunks where the claims are touching each other.
        List<GroupedChunks> groupList = new ArrayList<>();
        if (!teamClaimsList.isEmpty()) {

            while (!teamClaimsList.isEmpty()) {
                ChunkPosition chunkPos = teamClaimsList.iterator().next();
                GroupedChunks group = new GroupedChunks();
                groupList.add(group);

                group.processChunk(chunkPos, teamClaimsList);
            }
        }
        totalGroups = groupList.size();

        // Draw all the team claim markers for the specified dimension.
        if (m_DynmapIntegration != null) {
            m_DynmapIntegration.clearAllTeamMarkers(teamDim);
            int nIndex = 0;
            for (GroupedChunks group : groupList) {
                List<PositionPoint> perimeterPoints = group.traceShapePerimeter();

                m_DynmapIntegration.createAreaMarker(teamDim, nIndex++, perimeterPoints);
            }
        }

        // Make sure we clean up all the object cross references so they can be garbage collected.
        for (GroupedChunks group : groupList) {
            group.cleanup();
        }

        if (WarsFtbDynmapConfig.debug_mode){
            long deltaNs = System.nanoTime() - startTimeNS;
            logger.info(" --> {} Claim chunks processed.", totalChunks);
            logger.info(" --> {} Claim groups detected.", totalGroups);
            logger.info(" --> Complete claim update in [{}ns]", deltaNs);
        }

    }

    /**
     * Initializes the dynmap claims for all teams and all dimensions.
     *
     * NOTE: There is no way for us to know when FTB is ready with all the claim data, so we assume if we get a
     * response we are good to go. However when there are no claims we will also not get a response so we can't tell
     * the difference.
     *
     * @return Returns true if the initialization was successful.
     */

    private boolean initializeMap() {
        Set<TeamDimInfo> teamDimList = FTBUtilitiesIntegration.getListTeamDimWithClaims();

        for (TeamDimInfo teamDim : teamDimList) {
            queueClaimEventReceived(teamDim);
        }

        return teamDimList.size() > 0;
    }

    /**
     * Called on a server tickCounter timer to update the claims on a slow periodic basis
     * @param tickCounter An incrementing tick counter.
     */

    public void onServerTickEvent(long tickCounter) {
        if (m_bModEnabled && tickCounter >= m_NextTriggerTickCount) {

            if (m_bMapInitialized) {
                // Update the claim display in dynmap for the list of teams.
                if (!m_ClaimUpdates.isEmpty()) {
                    for (TeamDimInfo teamDim : m_ClaimUpdates) {
                        updateTeamClaims(teamDim);
                    }

                    m_ClaimUpdates.clear();
                }
            }
            else {
                // We can't determine when FTB Claims information will be available so we have to check every so often, for
                // the most part after the first tickCounter update FTB should be ready to go but we retry a few times before we
                // consider ourselves initialized.

                m_bMapInitialized = initializeMap();

                m_InitializeAttemptCount++;

                // After a few attempts to initialize, just consider the system initialized. When no claims exist
                // we will hit this case.
                if (m_InitializeAttemptCount > 10) {
                    m_bMapInitialized = true;
                }
            }

            m_NextTriggerTickCount = tickCounter + TICK_DELAY_TIME;
        }
    }

    /**
     * @return Returns true if the mod is enabled and operating.
     */

    public static boolean isModEnabled() {
        return WarsFtbDynmapMod.m_bModEnabled;
    }

}
