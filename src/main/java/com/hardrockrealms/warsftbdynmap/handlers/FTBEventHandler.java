package com.hardrockrealms.warsftbdynmap.handlers;

import com.feed_the_beast.ftblib.lib.data.ForgeTeam;
import com.feed_the_beast.ftbutilities.events.FTBUtilitiesEvent;
import com.feed_the_beast.ftbutilities.events.chunks.ChunkModifiedEvent;

import com.hardrockrealms.warsftbdynmap.WarsFtbDynmapMod;
import com.hardrockrealms.warsftbdynmap.data.TeamDimInfo;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Called when FTB claim events occur, this will capture the event and pass the details in to the main class
 */

@Mod.EventBusSubscriber(modid = WarsFtbDynmapMod.MODID)
public class FTBEventHandler {

    @Optional.Method(modid = "ftbutilities")
    @SubscribeEvent
    public static void onClaimEvent(FTBUtilitiesEvent event) {
        if (event instanceof ChunkModifiedEvent) {
            ChunkModifiedEvent chunkEvent = (ChunkModifiedEvent) event;

            if (WarsFtbDynmapMod.isModEnabled()) {
                ForgeTeam team = chunkEvent.getChunk().getTeam();
                TeamDimInfo teamDim = new TeamDimInfo(team.toString(),
                        chunkEvent.getChunk().getPos().dim,
                        team.getTitle().getUnformattedText(),
                        team.getDesc(),
                        team.getColor().getColor().rgba() & 0x00FFFFFF);

                WarsFtbDynmapMod.instance.queueClaimEventReceived(teamDim);
            }
        }
    }
}
