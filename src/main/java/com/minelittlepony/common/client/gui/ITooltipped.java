package com.minelittlepony.common.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.Screen;

/**
 * Interface element that renders a tooltip when hovered.
 *
 * @author     Sollace
 *
 * @param  <T> The subclass element.
 */
public interface ITooltipped<T extends ITooltipped<T>> {
    /**
     * Draws this element's tooltip.
     */
    void renderToolTip(PoseStack matrices, Screen parent, int mouseX, int mouseY);
}
