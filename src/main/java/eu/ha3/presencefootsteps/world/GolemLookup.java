package eu.ha3.presencefootsteps.world;

import net.minecraft.world.entity.EntityType;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class GolemLookup implements Lookup<EntityType<?>> {

    private final Map<String, Map<ResourceLocation, String>> substrates = new LinkedHashMap<>();

    @Override
    public String getAssociation(EntityType<?> key, String substrate) {

        Map<ResourceLocation, String> primitives = substrates.get(substrate);

        if (primitives == null) {
            // Check for default
            primitives = substrates.get(EMPTY_SUBSTRATE);
        }

        if (primitives == null) {
            return Emitter.UNASSIGNED;
        }

        return primitives.getOrDefault(EntityType.getKey(key), Emitter.UNASSIGNED);
    }

    @Override
    public Set<String> getSubstrates() {
        return substrates.keySet();
    }

    @Override
    public void add(String key, String value) {
        String[] split = key.trim().split("@");

        String primitive = split[0];
        String substrate = split.length > 1 ? split[1] : EMPTY_SUBSTRATE;

        substrates
            .computeIfAbsent(substrate, s -> new LinkedHashMap<>())
            .put(new ResourceLocation(primitive), value);
    }

    @Override
    public boolean contains(EntityType<?> key) {
        ResourceLocation primitive = EntityType.getKey(key);

        for (Map<ResourceLocation, String> primitives : substrates.values()) {
            if (primitives.containsKey(primitive)) {
                return true;
            }
        }
        return false;
    }
}
