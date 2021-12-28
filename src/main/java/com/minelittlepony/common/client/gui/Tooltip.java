package com.minelittlepony.common.client.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import com.google.common.base.Splitter;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public interface Tooltip {
    Splitter LINE_SPLITTER = Splitter.onPattern("\r?\n|\\\\n");

    List<Component> getLines();

    default CharSequence getString() {
        StringBuilder builder = new StringBuilder();
        getLines().forEach(line -> {
            if (builder.length() > 0) {
                builder.append('\n');
            }
            builder.append(line.getContents());
        });
        return builder;
    }

    default Stream<Component> stream() {
        return getLines().stream();
    }

    static Tooltip of(String text) {
        return of(new TranslatableComponent(text));
    }

    static Tooltip of(List<Component> lines) {
        List<Component> flines = lines.stream()
                .map(Tooltip::of)
                .flatMap(Tooltip::stream)
                .collect(Collectors.toList());
        return () -> flines;
    }

    static Tooltip of(Component text) {

        List<Component> lines = new ArrayList<>();
        lines.add(new TextComponent(""));

        text.visit((style, part) -> {
            List<Component> parts = LINE_SPLITTER.splitToList(part)
                    .stream()
                    .map(i -> new TextComponent(i).withStyle(style))
                    .collect(Collectors.toList());

            lines.add(((MutableComponent)lines.remove(lines.size() - 1)).append(parts.remove(0)));
            lines.addAll(parts);

            return Optional.empty();
        }, text.getStyle());

        return () -> lines;
    }
}
