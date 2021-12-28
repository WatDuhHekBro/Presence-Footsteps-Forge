package eu.ha3.presencefootsteps.sound.player;

import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.util.PlayerUtil;
import eu.ha3.presencefootsteps.world.Association;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;

import java.util.Random;

/**
 * A Library that can also play sounds and default footsteps.
 *
 * @author Hurry
 */
public class ImmediateSoundPlayer implements SoundPlayer, StepSoundPlayer {

    private final Random random = new Random();

    private final DelayedSoundPlayer delayedPlayer = new DelayedSoundPlayer(this);

    @Override
    public Random getRNG() {
        return random;
    }

    @Override
    public void playStep(Association assos) {
        SoundType soundType = assos.getSoundGroup();

        if (!assos.getMaterial().isLiquid() && soundType != null) {
            BlockState beside = assos.getSource().level.getBlockState(assos.getPos().above());

            if (beside.getBlock() == Blocks.SNOW) {
                soundType = Blocks.SNOW.getSoundType(beside);
            }

            playAttenuatedSound(assos.getSource(), soundType.getStepSound().getLocation().toString(), soundType.getVolume() * 0.15F, soundType.getPitch());
        }
    }

    @Override
    public void playSound(Entity location, String soundName, float volume, float pitch, Options options) {

        if (options.containsKey("delay_min") && options.containsKey("delay_max")) {
            delayedPlayer.playSound(location, soundName, volume, pitch, options);

            return;
        }

        playAttenuatedSound(location, soundName, volume, pitch);
    }

    private void playAttenuatedSound(Entity location, String soundName, float volume, float pitch) {
        Minecraft mc = Minecraft.getInstance();
        double distance = mc.gameRenderer.getMainCamera().getPosition().distanceToSqr(location.position());

        volume *= (100 - distance) / 100F;

        SimpleSoundInstance sound = createSound(getSoundId(soundName, location), volume, pitch, location);

        if (distance > 100) {
            mc.getSoundManager().playDelayed(sound, (int) Math.floor(Math.sqrt(distance) / 2));
        } else {
            mc.getSoundManager().play(sound);
        }
    }

    @Override
    public void think() {
        delayedPlayer.think();
    }

    private SimpleSoundInstance createSound(ResourceLocation id, float volume, float pitch, Entity entity) {
        return new SimpleSoundInstance(id,
                SoundSource.MASTER,
                volume, pitch, false, 0,
                SoundInstance.Attenuation.LINEAR,
                (float) entity.getX(),
                (float) entity.getY(),
                (float) entity.getZ(),
                false);
    }

    private ResourceLocation getSoundId(String name, Entity location) {
        if (name.indexOf(':') >= 0) {
            return new ResourceLocation(name);
        }

        String domain = "presencefootsteps";

        if (!PlayerUtil.isClientPlayer(location)) {
            domain += "mono"; // Switch to mono if playing another player
        }

        return new ResourceLocation(domain, name);
    }
}
