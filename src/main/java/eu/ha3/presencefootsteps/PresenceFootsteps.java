package eu.ha3.presencefootsteps;

import eu.ha3.presencefootsteps.sound.SoundEngine;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(PresenceFootsteps.modId)
public class PresenceFootsteps {
    public static final String modId = "presencefootsteps";

    public static final Logger logger = LogManager.getLogger("PFSolver");

    private static PresenceFootsteps instance;

    public static PresenceFootsteps getInstance() {
        return instance;
    }

    public static SoundEngine engine;

    public PFConfig config;

    public static PFDebugHud debugHud;

    public static KeyMapping keyBinding;

    public PFDebugHud getDebugHud() {
        return debugHud;
    }

    public SoundEngine getEngine() {
        return engine;
    }

    public PFConfig getConfig() {
        return config;
    }

    public void setConfig(PFConfig newConfig) { config = newConfig; }

    public PresenceFootsteps() {
        instance = this;

        MinecraftForge.EVENT_BUS.register(this);
    }
}
