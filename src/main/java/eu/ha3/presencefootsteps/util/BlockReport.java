package eu.ha3.presencefootsteps.util;

import com.google.gson.stream.JsonWriter;
import com.minelittlepony.common.util.GamePaths;
import eu.ha3.presencefootsteps.PresenceFootsteps;
import eu.ha3.presencefootsteps.world.Lookup;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.Registry;

import javax.annotation.Nullable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Predicate;

import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import net.minecraft.world.level.block.BaseRailBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TorchBlock;
import net.minecraft.world.level.block.WoolCarpetBlock;
import net.minecraft.world.level.block.state.BlockState;

public class BlockReport {
    private final Path loc;

    public BlockReport(String baseName) {
        loc = getUniqueFileName(GamePaths.getGameDirectory().resolve("presencefootsteps"), baseName, ".json");
    }

    public void execute(@Nullable Predicate<BlockState> filter) {
        try {
            writeReport(filter);
            printResults();
        } catch (Exception e) {
            addMessage(new TranslatableComponent("pf.report.error", e.getMessage()).withStyle(s -> s.withColor(ChatFormatting.RED)));
        }
    }

    private void writeReport(@Nullable Predicate<BlockState> filter) throws IOException {
        Files.createDirectories(loc.getParent());

        try (JsonWriter writer = new JsonWriter(Files.newBufferedWriter(loc))) {
            writer.setIndent("    ");
            writer.beginObject();
            writer.name("blocks");
            writer.beginObject();
            Registry.BLOCK.forEach(block -> {
                BlockState state = block.defaultBlockState();

                try {
                    if (filter == null || filter.test(state)) {
                        writer.name(Registry.BLOCK.getKey(block).toString());
                        writer.beginObject();
                        writer.name("class");
                        writer.value(getClassData(state));
                        writer.name("sound");
                        writer.value(getSoundData(state));
                        writer.name("association");
                        writer.value(PresenceFootsteps.getInstance().getEngine().getIsolator().getBlockMap().getAssociation(state, Lookup.EMPTY_SUBSTRATE));
                        writer.endObject();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writer.endObject();
            writer.name("unmapped_entities");
            writer.beginArray();
            Registry.ENTITY_TYPE.forEach(type -> {
                if (type.create(Minecraft.getInstance().level) instanceof LivingEntity) {
                    ResourceLocation id = Registry.ENTITY_TYPE.getKey(type);
                    if (!PresenceFootsteps.getInstance().getEngine().getIsolator().getLocomotionMap().contains(id)) {
                        try {
                            writer.value(id.toString());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            writer.endArray();
            writer.endObject();
        }
    }

    private String getSoundData(BlockState state) {
        if (state.getSoundType() == null) {
            return "NULL";
        }
        if (state.getSoundType().getStepSound() == null) {
            return "NO_SOUND";
        }
        return state.getSoundType().getStepSound().getLocation().getPath();
    }

    private String getClassData(BlockState state) {
        Block block = state.getBlock();

        String soundName = "";

        if (block instanceof BasePressurePlateBlock) soundName += ",EXTENDS_PRESSURE_PLATE";
        if (block instanceof BaseRailBlock) soundName += ",EXTENDS_RAIL";
        if (block instanceof BaseEntityBlock) soundName += ",EXTENDS_CONTAINER";
        if (block instanceof LiquidBlock) soundName += ",EXTENDS_LIQUID";
        if (block instanceof BushBlock) soundName += ",EXTENDS_PLANT";
        if (block instanceof DoublePlantBlock) soundName += ",EXTENDS_DOUBLE_PLANT";
        if (block instanceof PipeBlock) soundName += ",EXTENDS_CONNECTED_PLANT";
        if (block instanceof LeavesBlock) soundName += ",EXTENDS_LEAVES";
        if (block instanceof SlabBlock) soundName += ",EXTENDS_SLAB";
        if (block instanceof StairBlock) soundName += ",EXTENDS_STAIRS";
        if (block instanceof SnowyDirtBlock) soundName += ",EXTENDS_SNOWY";
        if (block instanceof SpreadingSnowyDirtBlock) soundName += ",EXTENDS_SPREADABLE";
        if (block instanceof FallingBlock) soundName += ",EXTENDS_PHYSICALLY_FALLING";
        if (block instanceof IronBarsBlock) soundName += ",EXTENDS_PANE";
        if (block instanceof HorizontalDirectionalBlock) soundName += ",EXTENDS_PILLAR";
        if (block instanceof TorchBlock) soundName += ",EXTENDS_TORCH";
        if (block instanceof WoolCarpetBlock) soundName += ",EXTENDS_CARPET";
        if (block instanceof InfestedBlock) soundName += ",EXTENDS_INFESTED";
        if (block instanceof HalfTransparentBlock) soundName += ",EXTENDS_TRANSPARENT";

        return soundName;
    }

    private void printResults() {
        addMessage(new TranslatableComponent("pf.report.save")
                .append(new TextComponent(loc.getFileName().toString()).withStyle(s -> s
                    .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, loc.toString()))
                    .applyFormat(ChatFormatting.UNDERLINE)))
                .withStyle(s -> s
                    .withColor(ChatFormatting.GREEN)));
    }

    public static void addMessage(Component text) {
        Minecraft.getInstance().gui.getChat().addMessage(text);
    }

    static Path getUniqueFileName(Path directory, String baseName, String ext) {
        Path loc = null;

        int counter = 0;
        while (loc == null || Files.exists(loc)) {
            loc = directory.resolve(baseName + (counter == 0 ? "" : "_" + counter) + ext);
            counter++;
        }

        return loc;
    }
}
