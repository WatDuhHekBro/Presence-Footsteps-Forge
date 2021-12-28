package eu.ha3.presencefootsteps.mixins;

import eu.ha3.presencefootsteps.PresenceFootsteps;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugScreenOverlay.class)
public abstract class MDebugHud extends GuiComponent {

    @Shadow
    protected HitResult rayTraceBlock;

    @Shadow
    protected HitResult rayTraceFluid;

    @Inject(method = "getDebugInfoRight", at = @At("RETURN"))
    protected void onGetRightText(CallbackInfoReturnable<List<String>> info) {
        PresenceFootsteps.getInstance().getDebugHud().render(rayTraceBlock, rayTraceFluid, info.getReturnValue());
    }
}
