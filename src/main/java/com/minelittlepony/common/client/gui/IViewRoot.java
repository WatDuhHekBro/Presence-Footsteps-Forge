package com.minelittlepony.common.client.gui;

import com.minelittlepony.common.client.gui.dimension.Bounds;
import com.minelittlepony.common.client.gui.dimension.IBounded;
import com.minelittlepony.common.client.gui.dimension.Padding;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.gui.components.events.GuiEventListener;

public interface IViewRoot extends IBounded {
    /**
     * Gets the total bounds of all the elements inside this container.
     */
    default Bounds getContentBounds() {
        return ((Bounds)this.getAllBounds().stream().reduce(Bounds.empty(), Bounds::add)).add(this.getContentPadding());
    }

    /**
     * Gets all the bounds of elements found inside this view. Includes sub-views and their contents.
     */
    default Set<Bounds> getAllBounds() {
        Set<Bounds> bounds = new HashSet<>();
        this.getChildElements().forEach((element) -> {
            if (element instanceof IViewRoot) {
                bounds.addAll(((IViewRoot)element).getAllBounds());
            }

            if (element instanceof IBounded) {
                bounds.add(((IBounded)element).getBounds());
            }

        });
        return bounds;
    }

    /**
     * Any extra padding this view adds around its contents.
     */
    Padding getContentPadding();

    List<GuiEventListener> getChildElements();
}
