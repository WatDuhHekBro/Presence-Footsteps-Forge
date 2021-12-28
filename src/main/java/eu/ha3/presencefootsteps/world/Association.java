package eu.ha3.presencefootsteps.world;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

public class Association {

    public static final Association NOT_EMITTER = new Association();

    private final BlockState blockState;

    private final BlockPos pos;

    private String data = Emitter.NOT_EMITTER;

    private Entity source;

    public Association() {
        this(Blocks.AIR.defaultBlockState(), BlockPos.ZERO);
    }

    public Association(BlockState state, BlockPos pos) {
        blockState = state;
        this.pos = pos;
    }

    public Association at(Entity source) {

        if (!isNull()) {
            this.source = source;
        }

        return this;
    }

    public Association with(@Nullable String data) {

        if (!isNull() && data != null) {
            this.data = data;
        }

        return this;
    }

    public boolean isNull() {
        return this == NOT_EMITTER;
    }

    public boolean isNotEmitter() {
        return isNull() || Emitter.isNonEmitter(data);
    }

    public boolean hasAssociation() {
        return !isNotEmitter() && Emitter.isResult(data);
    }

    public String getAcousticName() {
        return data;
    }

    public Entity getSource() {
        return source;
    }

    public Material getMaterial() {
        return blockState.getMaterial();
    }

    public BlockPos getPos() {
        return pos;
    }

    public SoundType getSoundGroup() {
        return blockState.getSoundType();
    }
}
