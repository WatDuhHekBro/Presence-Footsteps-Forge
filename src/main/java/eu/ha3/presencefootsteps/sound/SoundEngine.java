package eu.ha3.presencefootsteps.sound;

import eu.ha3.presencefootsteps.PFConfig;
import eu.ha3.presencefootsteps.PresenceFootsteps;
import eu.ha3.presencefootsteps.mixins.IEntity;
import eu.ha3.presencefootsteps.sound.acoustics.AcousticsJsonParser;
import eu.ha3.presencefootsteps.sound.generator.Locomotion;
import eu.ha3.presencefootsteps.sound.generator.StepSoundGenerator;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

import net.minecraft.server.packs.resources.PreparableReloadListener.PreparationBarrier;

public class SoundEngine implements PreparableReloadListener {

    private static final ResourceLocation blockmap = new ResourceLocation("presencefootsteps", "config/blockmap.json");
    private static final ResourceLocation golemmap = new ResourceLocation("presencefootsteps", "config/golemmap.json");
    private static final ResourceLocation locomotionmap = new ResourceLocation("presencefootsteps", "config/locomotionmap.json");
    private static final ResourceLocation primitivemap = new ResourceLocation("presencefootsteps", "config/primitivemap.json");
    private static final ResourceLocation acoustics = new ResourceLocation("presencefootsteps", "config/acoustics.json");
    private static final ResourceLocation variator = new ResourceLocation("presencefootsteps", "config/variator.json");

    private static final ResourceLocation ID = new ResourceLocation("presencefootsteps", "sounds");

    private PFIsolator isolator = new PFIsolator(this);

    private final PFConfig config;

    public SoundEngine(PFConfig config) {
        this.config = config;
    }

    public float getGlobalVolume() {
        return config.getVolume() / 100F;
    }

    public Isolator getIsolator() {
        return isolator;
    }

    public void reload() {
        if (config.getEnabled()) {
            reloadEverything(Minecraft.getInstance().getResourceManager());
        } else {
            shutdown();
        }
    }

    public boolean isRunning(Minecraft client) {
        return config.getEnabled() && (client.isLocalServer() || config.getEnabledMP());
    }

    private List<? extends Entity> getTargets(Player ply) {
        if (config.getEnabledGlobal()) {
            AABB box = new AABB(ply.blockPosition()).inflate(16);

            return ply.level.getEntities((Entity)null, box, e ->
                        e instanceof LivingEntity
                    && !(e instanceof WaterAnimal)
                    && !(e instanceof FlyingMob)
                    && !e.isPassenger());
        } else {
            return ply.level.players();
        }
    }

    public void onFrame(Minecraft client, Player player) {
        if (!client.isPaused() && isRunning(client)) {
            getTargets(player).forEach(e -> {
                StepSoundGenerator generator = ((StepSoundSource) e).getStepGenerator(this);
                generator.setIsolator(isolator);
                if (generator.generateFootsteps((LivingEntity)e)) {
                    ((IEntity) e).setNextStepDistance(Integer.MAX_VALUE);
                }
            });

            isolator.think(); // Delayed sounds
        }
    }

    public boolean onSoundRecieved(SoundEvent event, SoundSource category) {

        if (category != SoundSource.PLAYERS || !isRunning(Minecraft.getInstance())) {
            return false;
        }

        if (event == SoundEvents.PLAYER_SWIM
         || event == SoundEvents.PLAYER_SPLASH
         || event == SoundEvents.PLAYER_BIG_FALL
         || event == SoundEvents.PLAYER_SMALL_FALL) {
            return true;
        }

        String[] name = event.getLocation().getPath().split("\\.");

        return name.length > 0
                && "block".contentEquals(name[0])
                && "step".contentEquals(name[name.length - 1]);
    }

    public Locomotion getLocomotion(LivingEntity entity) {
        if (entity instanceof Player) {
            return Locomotion.forPlayer((Player)entity, config.getLocomotion());
        }
        return isolator.getLocomotionMap().lookup(entity);
    }

    @Override
    public CompletableFuture<Void> reload(PreparationBarrier sync, ResourceManager sender,
            ProfilerFiller serverProfiler, ProfilerFiller clientProfiler,
            Executor serverExecutor, Executor clientExecutor) {

        sync.getClass();
        return sync.wait(null).thenRunAsync(() -> {
            clientProfiler.startTick();
            clientProfiler.push("Reloading PF Sounds");
            reloadEverything(sender);
            clientProfiler.pop();
            clientProfiler.endTick();
        }, clientExecutor);
    }

    public void reloadEverything(ResourceManager manager) {
        isolator = new PFIsolator(this);

        collectResources(blockmap, manager, isolator.getBlockMap()::load);
        collectResources(golemmap, manager, isolator.getGolemMap()::load);
        collectResources(primitivemap, manager, isolator.getPrimitiveMap()::load);
        collectResources(locomotionmap, manager, isolator.getLocomotionMap()::load);
        collectResources(acoustics, manager, new AcousticsJsonParser(isolator.getAcoustics())::parse);
        collectResources(variator, manager, isolator.getVariator()::load);
    }

    private void collectResources(ResourceLocation id, ResourceManager manager, Consumer<Reader> consumer) {
        try {
            manager.getResources(id).forEach(res -> {
                try (Reader stream = new InputStreamReader(res.getInputStream())) {
                    consumer.accept(stream);
                } catch (Exception e) {
                    PresenceFootsteps.logger.error("Error encountered loading resource " + res.getLocation() + " from pack" + res.getSourceName(), e);
                }
            });
        } catch (IOException e) {
            PresenceFootsteps.logger.error("Error encountered opening resources for " + id, e);
        }
    }

    public void shutdown() {
        isolator = new PFIsolator(this);

        Player player = Minecraft.getInstance().player;

        if (player != null) {
            ((IEntity) player).setNextStepDistance(0);
        }
    }
}
