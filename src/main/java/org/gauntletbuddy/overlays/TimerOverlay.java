package org.gauntletbuddy.overlays;

import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.*;
import net.runelite.client.ui.overlay.components.LineComponent;
import net.runelite.client.ui.overlay.components.PanelComponent;
import net.runelite.client.ui.overlay.components.TitleComponent;
import org.gauntletbuddy.GauntletBuddy;
import org.gauntletbuddy.config.GauntletBuddyConfig;
import org.gauntletbuddy.utility.TimerStringUtil;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.time.Instant;

import static net.runelite.api.MenuAction.RUNELITE_OVERLAY_CONFIG;
import static net.runelite.client.ui.overlay.OverlayManager.OPTION_CONFIGURE;

@Singleton
public final class TimerOverlay extends OverlayPanel {
    private final GauntletBuddy plugin;
    private final GauntletBuddyConfig config;

    private final PanelComponent timerPanelComponent;
    private final LineComponent mazeTimeComponent;
    private final LineComponent bossTimeComponent;
    private final LineComponent totalTimeComponent;

    private Long lastUpdate;

    @Inject
    TimerOverlay(final GauntletBuddy plugin, final GauntletBuddyConfig config) {
        super(plugin);
        this.plugin = plugin;
        this.config = config;

        // Create the parent panel component
        panelComponent.setBorder(new Rectangle(2, 2, 2, 2));
        panelComponent.getChildren().add(TitleComponent.builder().text("Gauntlet Timer").build());

        // Create component to hold all the timers and add it to the parent panel
        timerPanelComponent = new PanelComponent();
        timerPanelComponent.setBorder(new Rectangle(2, 1, 4, 0));
        timerPanelComponent.setBackgroundColor(null);

        panelComponent.getChildren().add(timerPanelComponent);

        mazeTimeComponent = LineComponent.builder().left("Preparation :").right("").build();
        mazeTimeComponent.setLeftColor(Color.WHITE);
        mazeTimeComponent.setRightColor(Color.WHITE);
        timerPanelComponent.getChildren().add(mazeTimeComponent);

        bossTimeComponent = LineComponent.builder().left("Bossing :").right("").build();
        bossTimeComponent.setLeftColor(Color.LIGHT_GRAY);
        bossTimeComponent.setRightColor(Color.LIGHT_GRAY);
        timerPanelComponent.getChildren().add(bossTimeComponent);

        totalTimeComponent = LineComponent.builder().left("Total :").right("").build();
        totalTimeComponent.setLeftColor(Color.WHITE);
        totalTimeComponent.setRightColor(Color.WHITE);
        timerPanelComponent.getChildren().add(totalTimeComponent);

        lastUpdate = Instant.now().getEpochSecond();

        setClearChildren(false);
        getMenuEntries().add(new OverlayMenuEntry(RUNELITE_OVERLAY_CONFIG, OPTION_CONFIGURE, "Gauntlet Timer"));
        setPosition(OverlayPosition.TOP_LEFT);
        setMovable(true);
        setSnappable(true);
        setPriority(PRIORITY_HIGHEST);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
    }

    @Override
    public Dimension render(final Graphics2D graphics) {
        if (!config.gauntletTimer() || plugin.getMazeStart() == -1)
        {
            return null;
        }

        graphics.setFont(FontManager.getRunescapeSmallFont());

        final long update = Instant.now().getEpochSecond();

        if (update != lastUpdate) {
            // Render
            lastUpdate = update;
            long mazeTime = 0;
            long bossTime = 0;
            long totalTime = 0;

            if (plugin.getBossStart() > -1) {
                mazeTime = plugin.getBossStart() - plugin.getMazeStart();
                bossTime = update - plugin.getBossStart();
                totalTime = update - plugin.getMazeStart();
            } else {
                mazeTime = totalTime = update - plugin.getMazeStart();
            }

            mazeTimeComponent.setRight(TimerStringUtil.formatTimerString(mazeTime));
            bossTimeComponent.setRight(TimerStringUtil.formatTimerString(bossTime));
            totalTimeComponent.setRight(TimerStringUtil.formatTimerString(totalTime));
        }

        return super.render(graphics);
    }
}
