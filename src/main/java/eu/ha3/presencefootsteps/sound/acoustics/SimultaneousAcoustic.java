package eu.ha3.presencefootsteps.sound.acoustics;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import eu.ha3.presencefootsteps.sound.Options;
import eu.ha3.presencefootsteps.sound.State;
import eu.ha3.presencefootsteps.sound.player.SoundPlayer;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

/**
 * An acoustic that plays multiple other acoustics all a the same time.
 *
 * @author Hurry
 */
class SimultaneousAcoustic implements Acoustic {

    private final List<Acoustic> acoustics = new ArrayList<>();

    public SimultaneousAcoustic(JsonObject json, AcousticsJsonParser context) {
        this(json.getAsJsonArray("array"), context);
    }

    public SimultaneousAcoustic(JsonArray sim, AcousticsJsonParser context) {
        for (JsonElement i : sim) {
            acoustics.add(context.solveAcoustic(i));
        }
    }

    @Override
    public void playSound(SoundPlayer player, Entity location, State event, Options inputOptions) {
        acoustics.forEach(acoustic -> acoustic.playSound(player, location, event, inputOptions));
    }
}
