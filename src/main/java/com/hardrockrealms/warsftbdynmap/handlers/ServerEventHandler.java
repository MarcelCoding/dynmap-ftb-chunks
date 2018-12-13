package com.hardrockrealms.warsftbdynmap.handlers;

import com.hardrockrealms.warsftbdynmap.WarsFtbDynmapMod;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(value = Side.SERVER, modid = WarsFtbDynmapMod.MODID)
public class ServerEventHandler {
    public static long tickCounter = 0;

    @SubscribeEvent
    public static void onServerTickEvent(TickEvent.ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.END) {
            ServerEventHandler.tickCounter++;

            // Only process these server tickCounter events once a second, there is no need to do this on every tick.
            if ((ServerEventHandler.tickCounter % WarsFtbDynmapMod.TICKS_PER_SEC) == 0) {
                WarsFtbDynmapMod.instance.onServerTickEvent(tickCounter);
            }
        }
    }
}
