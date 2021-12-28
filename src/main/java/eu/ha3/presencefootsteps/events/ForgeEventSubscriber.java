package eu.ha3.presencefootsteps.events;

import eu.ha3.presencefootsteps.PFOptionsScreen;
import eu.ha3.presencefootsteps.PresenceFootsteps;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PresenceFootsteps.modId, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ForgeEventSubscriber {

    @SubscribeEvent
    public static void onClientTick(final TickEvent.ClientTickEvent event) {
        Minecraft client = Minecraft.getInstance();
        Player ply = client.player;

        if (ply == null || !ply.isAlive()) {
            return;
        }

        if (PresenceFootsteps.keyBinding.consumeClick() && client.screen == null) {
            client.setScreen(new PFOptionsScreen(client.screen));
        }

        PresenceFootsteps.engine.onFrame(client, ply);

        //updateNotifier.attempt();
    }
}
