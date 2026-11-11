package com.sandpvp.gui;

import com.sandpvp.Config;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * One ON/OFF button for EVERY particle type registered in the game (read from the registry when the
 * screen opens, so modded particles are included). ON = visible, OFF = not created on this client.
 */
final class ParticleScreen extends BaseScreen {
    private String query = "";
    private int page = 0;
    private int perPage = 4;
    private List<Config.ParticleEntry> filtered = new ArrayList<>();
    private final List<Button> rowButtons = new ArrayList<>();
    private Button masterButton;
    private Button pageLabel;
    private EditBox search;
    private int rowsTop;

    ParticleScreen(Screen parent) {
        super(parent, "Particles");
    }

    @Override
    protected void init() {
        rowButtons.clear();
        int left = left();
        int width = rowWidth();

        masterButton = Button.builder(Ui.toggle("Particle filter", Config.PARTICLES_ON.value), btn -> {
            Config.PARTICLES_ON.value = !Config.PARTICLES_ON.value;
            Config.particleVersion++;
            btn.setMessage(Ui.toggle("Particle filter", Config.PARTICLES_ON.value));
        }).bounds(left, 4, width, 20).build();
        this.addRenderableWidget(masterButton);

        search = new EditBox(this.font, left, 28, width, 20, Component.literal("Search"));
        search.setHint(Component.literal("Search particle..."));
        search.setValue(query);
        search.setResponder(text -> {
            query = text.toLowerCase(Locale.ROOT).trim();
            page = 0;
            refreshRows();
        });
        this.addRenderableWidget(search);

        int third = (width - 4) / 3;
        this.addRenderableWidget(Button.builder(Ui.light("Disable All"), btn -> {
            for (Config.ParticleEntry e : Config.particles()) {
                Config.hiddenParticles.add(e.id());
            }
            Config.particleVersion++;
            refreshRows();
        }).bounds(left, 52, third, 20).build());
        this.addRenderableWidget(Button.builder(Ui.light("Enable All"), btn -> {
            Config.hiddenParticles.clear();
            Config.particleVersion++;
            refreshRows();
        }).bounds(left + third + 2, 52, third, 20).build());
        this.addRenderableWidget(Button.builder(Ui.light("Reset"), btn -> {
            Config.hiddenParticles.clear();
            Config.PARTICLES_ON.reset();
            Config.particleVersion++;
            masterButton.setMessage(Ui.toggle("Particle filter", Config.PARTICLES_ON.value));
            refreshRows();
        }).bounds(left + 2 * (third + 2), 52, width - 2 * (third + 2), 20).build());

        rowsTop = 76;
        int navY = this.height - 26;
        perPage = Math.max(2, (navY - rowsTop - 4) / 24);

        int backW = width * 25 / 100;
        int arrowW = width * 20 / 100;
        int labelW = width - backW - arrowW * 2 - 6;
        int x = left;
        this.addRenderableWidget(Button.builder(Ui.light("Back"), btn -> back())
                .bounds(x, navY, backW, 20).build());
        x += backW + 2;
        this.addRenderableWidget(Button.builder(Ui.orange("<"), btn -> {
            if (page > 0) {
                page--;
                refreshRows();
            }
        }).bounds(x, navY, arrowW, 20).build());
        x += arrowW + 2;
        pageLabel = Button.builder(Ui.light("1/1"), btn -> { })
                .bounds(x, navY, labelW, 20).build();
        this.addRenderableWidget(pageLabel);
        x += labelW + 2;
        this.addRenderableWidget(Button.builder(Ui.orange(">"), btn -> {
            if ((page + 1) * perPage < filtered.size()) {
                page++;
                refreshRows();
            }
        }).bounds(x, navY, arrowW, 20).build());

        query = search.getValue().toLowerCase(Locale.ROOT).trim();
        refreshRows();
    }

    private void refreshRows() {
        for (Button b : rowButtons) {
            this.removeWidget(b);
        }
        rowButtons.clear();

        filtered = new ArrayList<>();
        for (Config.ParticleEntry e : Config.particles()) {
            if (query.isEmpty() || e.name().toLowerCase(Locale.ROOT).contains(query) || e.id().contains(query)) {
                filtered.add(e);
            }
        }
        int pages = Math.max(1, (filtered.size() + perPage - 1) / perPage);
        if (page >= pages) {
            page = pages - 1;
        }
        if (pageLabel != null) {
            pageLabel.setMessage(Ui.light((page + 1) + "/" + pages + "  (" + filtered.size() + ")"));
        }

        int start = page * perPage;
        int end = Math.min(filtered.size(), start + perPage);
        int y = rowsTop;
        for (int i = start; i < end; i++) {
            final Config.ParticleEntry entry = filtered.get(i);
            Button button = Button.builder(rowText(entry), btn -> {
                if (!Config.hiddenParticles.remove(entry.id())) {
                    Config.hiddenParticles.add(entry.id());
                }
                Config.particleVersion++;
                btn.setMessage(rowText(entry));
            }).bounds(left(), y, rowWidth(), 22).build();
            rowButtons.add(button);
            this.addRenderableWidget(button);
            y += 24;
        }
    }

    private static Component rowText(Config.ParticleEntry entry) {
        return Ui.toggle(entry.name(), !Config.hiddenParticles.contains(entry.id()));
    }
}
