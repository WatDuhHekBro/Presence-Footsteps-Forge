package eu.ha3.presencefootsteps.world;

import eu.ha3.presencefootsteps.PresenceFootsteps;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;

import java.util.LinkedHashMap;
import java.util.Map;

public class LocomotionLookup implements Index<Entity, Locomotion> {

    private final Map<ResourceLocation, Locomotion> values = new LinkedHashMap<>();

    @Override
    public Locomotion lookup(Entity key) {
        if (key instanceof Player) {
            return Locomotion.forPlayer((Player)key, Locomotion.NONE);
        }
        return Locomotion.forLiving(key, values.getOrDefault(EntityType.getKey(key.getType()), Locomotion.BIPED));
    }

    @Override
    public void add(String key, String value) {
        ResourceLocation id = new ResourceLocation(key);

        if (!Registry.ENTITY_TYPE.containsKey(id)) {
            PresenceFootsteps.logger.warn("Locomotion registered for unknown entity type " + id);
        }

        values.put(id, Locomotion.byName(value.toUpperCase()));
    }

    @Override
    public boolean contains(ResourceLocation key) {
        return values.containsKey(key);
    }
}
