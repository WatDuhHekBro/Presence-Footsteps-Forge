package eu.ha3.presencefootsteps.events;

import com.minelittlepony.common.util.GamePaths;
import eu.ha3.presencefootsteps.PFConfig;
import eu.ha3.presencefootsteps.PFDebugHud;
import eu.ha3.presencefootsteps.PresenceFootsteps;
import eu.ha3.presencefootsteps.sound.SoundEngine;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Path;

@Mod.EventBusSubscriber(modid = PresenceFootsteps.modId, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModEventSubscriber {

    @SubscribeEvent
    public static void onInitializeClient(final FMLClientSetupEvent event) {
        PresenceFootsteps.logger.info("Presence Footsteps starting");
        Path pfFolder = GamePaths.getConfigDirectory().resolve("presencefootsteps");

        // todo: use Forge's update notifier system
        /*updateNotifier = new UpdateNotifier(
                pfFolder.resolve("updater.json"),
                "https://raw.githubusercontent.com/Sollace/Presence-Footsteps/master/version/versions.json?ver=%d",
                new UpdateNotifier.Version("1.16.4", "r", 29), this::onUpdate);
        updateNotifier.load();*/

        PresenceFootsteps.getInstance().setConfig(new PFConfig(pfFolder.resolve("userconfig.json"), PresenceFootsteps.getInstance()));
        PresenceFootsteps.getInstance().getConfig().load();

        PresenceFootsteps.keyBinding = new KeyMapping("presencefootsteps.settings.key", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F10, "key.categories.misc");

        PresenceFootsteps.engine = new SoundEngine(PresenceFootsteps.getInstance().getConfig());

        PresenceFootsteps.debugHud = new PFDebugHud(PresenceFootsteps.engine);

        PresenceFootsteps.getInstance().getEngine().reload();
    }
}
