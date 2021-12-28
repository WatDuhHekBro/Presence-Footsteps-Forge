package com.minelittlepony.common.client.gui;

import java.util.List;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.AbstractWidget;
import com.mojang.blaze3d.platform.InputConstants;
import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.common.client.gui.dimension.Padding;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.chat.Component;

/**
 * Optional root element for a screen using Kirin functionality.
 * <p>
 * This class implements some QOL features, such as bounds, text utilities,
 * and is required to make use of the built-in tooltip rendering capabilities
 * of Kirin UI elements.
 *
 * @author     Sollace
 *
 */
public abstract class GameGui extends Screen implements IViewRoot, ITextContext {

    private final Bounds bounds = new Bounds(0, 0, 0, 0);

    private final Padding padding = new Padding(0, 0, 0, 0);

    private final ToolTipRenderer tooltip = new ToolTipRenderer(this);

    /**
     * The parent screen that existed prior to opening this Screen.
     * If present, this screen will replace this one upon closing.
     */
    @Nullable
    protected final Screen parent;

    /**
     * Creates a new GameGui with the given title, and parent as the screen currently displayed.
     *
     * @param title The screen's title
     */
    protected GameGui(Component title) {
        this(title, Minecraft.getInstance().screen);
    }

    /**
     * Creates a new GameGui with the given title, and parent as the screen currently displayed.
     *
     * @param title The screen's title.
     * @param parent The parent screen.
     */
    protected GameGui(Component title, @Nullable Screen parent) {
        super(title);

        this.parent = parent;
    }

    /**
     * Plays a sound event.
     *
     * @param event The sound event to play.
     */
    public static void playSound(SoundEvent event) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(event, 1));
    }

    /**
     * Determines whether the a key is currently pressed.
     *
     * @param key The GLFW keyCode to check
     * @return True if the key is pressed.
     */
    public static boolean isKeyDown(int key) {
        return InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), key);
    }

    /**
     * Creates a supplier for checking a specific key.
     *
     * @param key The GLFW keyCode to check
     * @return A supplier that returns True if the key is pressed.
     */
    public static Supplier<Boolean> keyCheck(int key) {
        return () -> isKeyDown(key);
    }

    /**
     * The list of buttons present on this screen.
     */
    public List<AbstractWidget> buttons() {
        return buttons;
    }

    /**
     * The list of all child elements, buttons included, present on this screen.
     */
    @Override
    public List<GuiEventListener> children() {
        return children;
    }

    /**
     * Adds a button to this screen.
     * <p>
     * Made public to help with mod development.
     */
    @Override
    public <T extends AbstractWidget> T addButton(T button) {
        return super.addButton(button);
    }

    @Override
    public void init(Minecraft mc, int width, int height) {
        bounds.width = width;
        bounds.height = height;
        super.init(mc, width, height);
    }

    @Override
    public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        buttons.forEach(button -> {
            if (button instanceof ITooltipped && button.isMouseOver(mouseX, mouseY)) {
                ((ITooltipped<?>)button).renderToolTip(matrices, this, mouseX, mouseY);
            }
        });
    }

    /**
     * Closes this screen and returns to the parent.
     *
     * Implementors should explicitly call this method when they want this behavior.
     */
    public void finish() {
        removed();
        minecraft.setScreen(parent);
    }

    @Override
    public Bounds getBounds() {
        return bounds;
    }

    @Override
    public void setBounds(Bounds bounds) {

    }

    @Override
    public Padding getContentPadding() {
        return padding;
    }

    @Override
    public List<GuiEventListener> getChildElements() {
        return children();
    }

    @Override
    public void renderTooltip(PoseStack matrices, List<? extends FormattedCharSequence> text, int x, int y) {
        tooltip.render(matrices, text, x, y);
    }
}
