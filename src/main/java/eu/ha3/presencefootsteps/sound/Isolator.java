package eu.ha3.presencefootsteps.sound;

import eu.ha3.presencefootsteps.config.Variator;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticLibrary;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import eu.ha3.presencefootsteps.sound.player.StepSoundPlayer;
import eu.ha3.presencefootsteps.world.Index;
import eu.ha3.presencefootsteps.world.Lookup;
import eu.ha3.presencefootsteps.world.Solver;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

public interface Isolator {
    AcousticLibrary getAcoustics();

    Solver getSolver();

    Index<Entity, Locomotion> getLocomotionMap();

    Lookup<EntityType<?>> getGolemMap();

    Lookup<BlockState> getBlockMap();

    Lookup<SoundType> getPrimitiveMap();

    StepSoundPlayer getStepPlayer();

    Variator getVariator();
}