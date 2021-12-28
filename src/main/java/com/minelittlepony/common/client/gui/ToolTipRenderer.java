package com.minelittlepony.common.client.gui;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.util.FormattedCharSequence;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.math.Matrix4f;

/**
 * Renders a stylised tooltip with borders and backgrounds.
 *
 * @author     Sollace
 */
public class ToolTipRenderer extends GuiComponent {

    private final Screen screen;

    /***
     * Screates a new tooltip renderer.
     *
     * @param parent The screen containing this element.
     */
    public ToolTipRenderer(Screen parent) {
        screen = parent;
    }

    /**
     * The default font colour of text inside the tooltip.
     */
    protected int getTextColor() {
        return 0xF000F0;
    }

    /**
     * The background fill for the tooltip.
     */
    protected int getFill() {
        return 0xF0100010;
    }

    /**
     * The top (start) gradient colour of the tooltip's border.
     */
    protected int getOutlineGradientTop() {
        return 0x505000FF;
    }

    /**
     * The bottom (end) gradient colour of the tooltip's border.
     * @return
     */
    protected int getOutlineGradientBottom() {
        return 0x5028007F;
    }

    /**
     * Renders a tooltip with text.
     *
     * @param text Text to display.
     * @param x The left X position (in pixels) of the tooltip
     * @param y The top Y position (in pixels) of the tooltip
     */
    public void render(PoseStack matrices, List<? extends FormattedCharSequence> text, int x, int y) {
        if (text.isEmpty()) {
            return;
        }

        Font font = Minecraft.getInstance().font;
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        RenderSystem.disableRescaleNormal();
        RenderSystem.disableDepthTest();

        int labelWidth = 0;

        for (FormattedCharSequence string : text) {
            labelWidth = Math.max(labelWidth, font.width(string));
        }

        int left = x + 12;
        int top = y - 12;
        int labelHeight = 8;

        if (text.size() > 1) {
            labelHeight += 2 + (text.size() - 1) * 10;
        }

        if (left + labelWidth > screen.width) {
            left -= 28 + labelWidth;
        }

        if (top + labelHeight + 6 > screen.height) {
            top = screen.height - labelHeight - 6;
        }

        setBlitOffset(300);
        itemRenderer.blitOffset = 300;

        int labelFill = getFill();
        fillGradient(matrices, left - 3,              top - 4,               left + labelWidth + 3, top - 3,               labelFill, labelFill);
        fillGradient(matrices, left - 3,              top + labelHeight + 3, left + labelWidth + 3, top + labelHeight + 4, labelFill, labelFill);
        fillGradient(matrices, left - 3,              top - 3,               left + labelWidth + 3, top + labelHeight + 3, labelFill, labelFill);
        fillGradient(matrices, left - 4,              top - 3,               left - 3,              top + labelHeight + 3, labelFill, labelFill);
        fillGradient(matrices, left + labelWidth + 3, top - 3,               left + labelWidth + 4, top + labelHeight + 3, labelFill, labelFill);

        int borderGradientTop = getOutlineGradientTop();
        int borderGradientBot = getOutlineGradientBottom();
        fillGradient(matrices, left - 3,              top - 3 + 1,           left - 3 + 1,          top + labelHeight + 3 - 1, borderGradientTop, borderGradientBot);
        fillGradient(matrices, left + labelWidth + 2, top - 3 + 1,           left + labelWidth + 3, top + labelHeight + 3 - 1, borderGradientTop, borderGradientBot);
        fillGradient(matrices, left - 3,              top - 3,               left + labelWidth + 3, top - 3 + 1,               borderGradientTop, borderGradientTop);
        fillGradient(matrices, left - 3,              top + labelHeight + 2, left + labelWidth + 3, top + labelHeight + 3,     borderGradientBot, borderGradientBot);

        PoseStack stack = new PoseStack();
        MultiBufferSource.BufferSource immediate = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());

        stack.translate(0, 0, itemRenderer.blitOffset);
        Matrix4f matrix = stack.last().pose();

        int color = getTextColor();

        for(int r = 0; r < text.size(); ++r) {
            FormattedCharSequence line = text.get(r);
            if (line != null) {
                font.drawInBatch(line, left, top, -1, true, matrix, immediate, true, 0, color);
            }

            if (r == 0) {
                top += 2;
            }

            top += 10;
        }

        immediate.endBatch();

        setBlitOffset(0);
        itemRenderer.blitOffset = 0;
        RenderSystem.enableDepthTest();
        RenderSystem.enableRescaleNormal();
    }
}