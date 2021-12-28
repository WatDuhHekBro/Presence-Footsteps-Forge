package eu.ha3.presencefootsteps.sound.player;

import eu.ha3.presencefootsteps.sound.Options;
import net.minecraft.world.entity.Entity;

import java.util.Random;

public interface SoundPlayer {
    /**
     * Plays a sound.
     */
    void playSound(Entity location, String soundName, float volume, float pitch, Options options);

    /**
     * Returns a random number generator.
     */
    Random getRNG();

    void think();
}
