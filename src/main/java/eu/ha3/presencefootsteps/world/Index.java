package eu.ha3.presencefootsteps.world;

import net.minecraft.resources.ResourceLocation;

public interface Index<K, V> extends Loadable {
    V lookup(K key);

    boolean contains(ResourceLocation key);
}
