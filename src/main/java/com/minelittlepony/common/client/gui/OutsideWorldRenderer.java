package com.minelittlepony.common.client.gui;

import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.ObjectUtils;

/**
 * Utility for rendering objects such as ItemStacks, Entities, and BlockEntities, when there is no client world running.
 * <p>
 * This class performs all the neccessary setup to ensure the above objects render correctly.
 *
 * @author     Sollace
 *
 */
public class OutsideWorldRenderer {
    /**
     * Gets a pre-configured TileEntityRendererDispatcher
     * for rendering BlockEntities outside of the world.
     * <p>
     *
     * @param world An optional World instance to configure the renderer against. May be null.
     *
     * @return a pre-configured TileEntityRendererDispatcher
     */
    /*
    public static BlockEntityRenderDispatcher configure(@Nullable Level world) {
        BlockEntityRenderDispatcher dispatcher = BlockEntityRenderDispatcher.instance;
        Minecraft mc = Minecraft.getInstance();

        world = ObjectUtils.firstNonNull(dispatcher.level, world, mc.level);

        dispatcher.prepare(world,
                mc.gameRenderer.getMainCamera(),
                mc.hitResult);

        mc.getEntityRenderDispatcher().prepare(world,
                mc.gameRenderer.getMainCamera(),
                mc.crosshairPickEntity);

        return dispatcher;
    }
    */

    /**
     * Renders a ItemStack to the screen.
     *
     * @param stack The stack to render.
     * @param x The left-X position (in pixels)
     * @param y The top-Y position (in pixels)
     */
    public static void renderStack(ItemStack stack, int x, int y) {
        //configure(null);
        Minecraft.getInstance().getItemRenderer().renderGuiItem(stack, x, y);
    }
}