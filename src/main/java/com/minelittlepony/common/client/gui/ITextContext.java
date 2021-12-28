package com.minelittlepony.common.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.FormattedCharSequence;
import com.mojang.math.Transformation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;

/**
 * Context utility for things that want to render text to the screen.
 * <p>
 * These methods are provided as an alternative to the Vanilla ones,
 * with one slight change to allow text to be rendered over content
 * that would normally be layered on top of it.
 * <p>
 * If you have entities in your screen and can't see text behind them,
 * use this.
 *
 * @author     Sollace
 */
public interface ITextContext {

    /**
     * Gets the global TextRenderer instance.
     */
    default Font getFont() {
        return Minecraft.getInstance().font;
    }

    /**
     * Draws a piece of coloured, left-aligned text to the screen.
     *
     * @param text The text to render
     * @param x The left X position (in pixel)
     * @param y The top Y position (in pixel)
     * @param color The font colour
     * @param zIndex The Z-index used when layering multiple elements.
     */
    default void drawLabel(PoseStack matrices, Component text, int x, int y, int color, double zIndex) {
        MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        matrices.translate(0, 0, zIndex);
        getFont().drawInBatch(text, x, y, color, true, matrices.last().pose(), immediate, true, 0, 0xF000F0);
        immediate.endBatch();
    }

    /**
     * Draws a piece of coloured, centered text to the screen.
     *
     * @param text The text to render
     * @param x The left X position (in pixel)
     * @param y The top Y position (in pixel)
     * @param color The font colour
     * @param zIndex The Z-index used when layering multiple elements.
     */
    default void drawCenteredLabel(PoseStack matrices, Component text, int x, int y, int color, double zIndex) {
        int width = getFont().width(text);

        drawLabel(matrices, text, x - width/2, y, color, zIndex);
    }

    /**
     * Draws a block of text spanning multiple lines. Content is left-aligned,
     * and wrapped to fit in the given page width.
     *
     * @param text The text to render
     * @param x The left X position (in pixel)
     * @param y The top Y position (in pixel)
     * @param maxWidth The maximum page width
     * @param color The font colour
     */
    default void drawTextBlock(PoseStack matrices, FormattedText text, int x, int y, int maxWidth, int color) {
        Transformation.identity().getMatrix();

        for (FormattedCharSequence line : getFont().split(text, maxWidth)) {
            float left = x;
            MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
            getFont().drawInBatch(line, left, y, color, false, matrices.last().pose(), immediate, true, 0, 0xF000F0);
            immediate.endBatch();

            y += 9;
        }
    }
}
