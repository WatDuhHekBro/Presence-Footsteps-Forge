package eu.ha3.presencefootsteps.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class PlayerUtil {
    public static boolean isClientPlayer(Entity entity) {
        Player client = Minecraft.getInstance().player;
        return entity instanceof Player
                && !(entity instanceof RemotePlayer)
                && client != null
                && (client == entity || client.getUUID().equals(entity.getUUID()));
    }
}
