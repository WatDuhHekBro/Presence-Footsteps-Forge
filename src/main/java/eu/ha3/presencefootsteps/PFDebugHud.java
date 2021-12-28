package eu.ha3.presencefootsteps;

import eu.ha3.presencefootsteps.sound.SoundEngine;
import eu.ha3.presencefootsteps.world.Emitter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;
import java.util.Map;

public class PFDebugHud {

    private final SoundEngine engine;

    public PFDebugHud(SoundEngine engine) {
        this.engine = engine;
    }

    public void render(HitResult blockHit, HitResult fluidHit, List<String> list) {
        Minecraft client = Minecraft.getInstance();

        if (blockHit.getType() == HitResult.Type.BLOCK) {
            BlockState state = client.level.getBlockState(((BlockHitResult)blockHit).getBlockPos());

            renderSoundList("PF Sounds",
                    engine.getIsolator().getBlockMap().getAssociations(state),
                    list);
        }

        if (client.crosshairPickEntity != null) {
            renderSoundList("PF Golem Sounds",
                    engine.getIsolator().getGolemMap().getAssociations(client.crosshairPickEntity.getType()),
                    list);
            list.add(engine.getIsolator().getLocomotionMap().lookup(client.crosshairPickEntity).getDisplayName());
        }
    }

    private void renderSoundList(String title, Map<String, String> sounds, List<String> list) {
        list.add("");
        list.add(title);
        if (sounds.isEmpty()) {
            list.add(Emitter.UNASSIGNED);
        } else {
            sounds.forEach((key, value) -> {
                list.add((key.isEmpty() ? "default" : key) + ": " + value);
            });
        }
    }
}
