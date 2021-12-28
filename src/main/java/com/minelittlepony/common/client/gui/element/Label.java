package com.minelittlepony.common.client.gui.element;

import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

/**
 * A simple label for drawing text to a gui screen.
 *
 * @author Sollace
 *
 */
public class Label extends Button {

    private boolean center;

    public Label(int x, int y) {
        super(x, y);
    }

    public Label setCentered() {
        this.center = true;

        return this;
    }

    @Override
    public Bounds getBounds() {
        Bounds bounds = super.getBounds();

        Font fonts = Minecraft.getInstance().font;

        bounds.width = fonts.width(getStyle().getText());
        if (this.center) {
            bounds.left = x - bounds.width/2;
        }

        return bounds;
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return false;
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float partialTicks) {
        int textY = (int)(y + Minecraft.getInstance().font.lineHeight/1.5F);

        if (center) {
            drawCenteredLabel(matrices, getStyle().getText(), x, textY, getStyle().getColor(), 0);
        } else {
            drawLabel(matrices, getStyle().getText(), x, textY, getStyle().getColor(), 0);
        }
    }
}
