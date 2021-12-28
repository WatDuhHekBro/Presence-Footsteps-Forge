package eu.ha3.presencefootsteps.world;

import com.google.common.collect.Lists;
import eu.ha3.presencefootsteps.PresenceFootsteps;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;

import java.util.*;
import java.util.stream.Collectors;

/**
 * A state lookup that finds an association for a given block state within a specific substrate (or no substrate).
 *
 * @author Sollace
 */
public class StateLookup implements Lookup<BlockState> {

    private final Map<String, Bucket> substrates = new LinkedHashMap<>();

    @Override
    public String getAssociation(BlockState state, String substrate) {
        return substrates.getOrDefault(substrate, Bucket.EMPTY).get(state).value;
    }

    @Override
    public void add(String key, String value) {
        if (!Emitter.isResult(value)) {
            PresenceFootsteps.logger.info("Skipping non-result value " + key + "=" + value);
            return;
        }

        Key k = new Key(key, value);

        substrates.computeIfAbsent(k.substrate, Bucket.Substrate::new).add(k);
    }

    @Override
    public Set<String> getSubstrates() {
        return substrates.keySet();
    }

    @Override
    public boolean contains(BlockState state) {
        for (Bucket substrate : substrates.values()) {
            if (substrate.contains(state)) {
                return true;
            }
        }

        return false;
    }

    private interface Bucket {

        Bucket EMPTY = state -> Key.NULL;

        default void add(Key key) {}

        Key get(BlockState state);

        default boolean contains(BlockState state) {
            return false;
        }

        final class Substrate implements Bucket {
            private final KeyList wildcards = new KeyList();
            private final Map<ResourceLocation, Bucket> blocks = new LinkedHashMap<>();
            private final Map<ResourceLocation, Bucket> tags = new LinkedHashMap<>();

            Substrate(String substrate) { }

            @Override
            public void add(Key key) {
                if (key.isWildcard) {
                    wildcards.add(key);
                } else {
                    (key.isTag ? tags : blocks).computeIfAbsent(key.identifier, Tile::new).add(key);
                }
            }

            @Override
            public Key get(BlockState state) {
                Key association = getTile(state).get(state);

                if (association == Key.NULL) {
                    return wildcards.findMatch(state);
                }
                return association;
            }

            @Override
            public boolean contains(BlockState state) {
                return getTile(state).contains(state);
            }

            private Bucket getTile(BlockState state) {
                return blocks.computeIfAbsent(Registry.BLOCK.getKey(state.getBlock()), id -> {
                    Block block = Registry.BLOCK.get(id);

                    for (ResourceLocation tag : tags.keySet()) {
                        if (BlockTags.getAllTags().getTagOrEmpty(tag).contains(block)) {
                            return tags.get(tag);
                        }
                    }

                    return Bucket.EMPTY;
                });
            }
        }

        final class Tile implements Bucket {
            private final Map<BlockState, Key> cache = new LinkedHashMap<>();
            private final KeyList keys = new KeyList();

            Tile(ResourceLocation id) { }

            @Override
            public void add(Key key) {
                keys.add(key);
            }

            @Override
            public Key get(BlockState state) {
                return cache.computeIfAbsent(state, keys::findMatch);
            }

            @Override
            public boolean contains(BlockState state) {
                return get(state) != Key.NULL;
            }
        }
    }

    private static final class KeyList {
        private final Set<Key> keys = new LinkedHashSet<>();

        void add(Key key) {
            keys.remove(key);
            keys.add(key);
        }

        public Key findMatch(BlockState state) {
            for (Key i : keys) {
                if (i.matches(state)) {
                    return i;
                }
            }
            return Key.NULL;
        }
    }

    private static final class Key {

        public static final Key NULL = new Key();

        public final ResourceLocation identifier;

        public final String substrate;

        private final Set<Attribute> properties;

        public final String value;

        private final boolean empty;

        public final boolean isTag;

        public final boolean isWildcard;

        private Key() {
            identifier = new ResourceLocation("air");
            substrate = "";
            properties = Collections.emptySet();
            value = Emitter.UNASSIGNED;
            empty = true;
            isTag = false;
            isWildcard = false;
        }

        /*
         * minecraft:block[one=1,two=2].substrate
         * #minecraft:blanks[one=1,two=2].substrate
         */
        Key(String key, String value) {

            this.value = value;
            this.isTag = key.indexOf('#') == 0;

            if (isTag) {
                key = key.replaceFirst("#", "");
            }

            String id = key.split("[\\.\\[]")[0];

            isWildcard = id.indexOf('*') == 0;

            if (!isWildcard) {
                if (id.indexOf('^') > -1) {
                    identifier = new ResourceLocation(id.split("\\^")[0]);
                    PresenceFootsteps.logger.warn("Metadata entry for " + key + "=" + value + " was ignored");
                } else {
                    identifier = new ResourceLocation(id);
                }

                if (!isTag && !Registry.BLOCK.containsKey(identifier)) {
                    PresenceFootsteps.logger.warn("Sound registered for unknown block id " + identifier);
                }
            } else {
                identifier = new ResourceLocation("air");
            }

            key = key.replace(id, "");

            String substrate = key.replaceFirst("\\[[^\\]]+\\]", "");

            if (substrate.indexOf('.') > -1) {
                this.substrate = substrate.split("\\.")[1];

                key = key.replace(substrate, "");
            } else {
                this.substrate = "";
            }

            properties = Lists.newArrayList(key.replace("[", "").replace("]", "").split(","))
                .stream()
                .filter(line -> line.indexOf('=') > -1)
                .map(Attribute::new)
                .collect(Collectors.toSet());
            empty = properties.isEmpty();
        }

        boolean matches(BlockState state) {

            if (empty) {
                return true;
            }

            Map<Property<?>, Comparable<?>> entries = state.getValues();
            Set<Property<?>> keys = entries.keySet();

            for (Attribute property : properties) {
                for (Property<?> key : keys) {
                    if (key.getName().equals(property.name)) {
                        Comparable<?> value = entries.get(key);

                        if (!Objects.toString(value).equals(property.value)) {
                            return false;
                        }
                    }
                }
            }

            return true;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (empty ? 1231 : 1237);
            result = prime * result + ((identifier == null) ? 0 : identifier.hashCode());
            result = prime * result + (isTag ? 1231 : 1237);
            result = prime * result + (isWildcard ? 1231 : 1237);
            result = prime * result + ((properties == null) ? 0 : properties.hashCode());
            result = prime * result + ((substrate == null) ? 0 : substrate.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj || (obj != null && getClass() == obj.getClass()) && equals((Key) obj);
        }
        private boolean equals(Key other) {
            return isTag == other.isTag && isWildcard == other.isWildcard && empty == other.empty
                    && Objects.equals(identifier, other.identifier)
                    && Objects.equals(substrate, other.substrate)
                    && Objects.equals(properties, other.properties);
        }

        private static class Attribute {
            private final String name;
            private final String value;

            Attribute(String prop) {
                String[] split = prop.split("=");

                this.name = split[0];
                this.value = split[1];
            }

            @Override
            public int hashCode() {
                final int prime = 31;
                int result = 1;
                result = prime * result + ((name == null) ? 0 : name.hashCode());
                result = prime * result + ((value == null) ? 0 : value.hashCode());
                return result;
            }

            @Override
            public boolean equals(Object obj) {
                return this == obj || (obj != null && getClass() == obj.getClass()) && equals((Attribute) obj);
            }
            private boolean equals(Attribute other) {
                return Objects.equals(name, other.name) && Objects.equals(value, other.value);
            }
        }
    }
}
