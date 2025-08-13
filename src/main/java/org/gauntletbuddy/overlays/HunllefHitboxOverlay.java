package org.gauntletbuddy.overlays;

import net.runelite.api.*;
import net.runelite.api.Point;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.*;
import org.gauntletbuddy.config.GauntletBuddyConfig;
import org.gauntletbuddy.config.types.TornadoHighlightType;
import org.gauntletbuddy.modules.HunllefModule;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.awt.*;
import java.awt.geom.Arc2D;


@Singleton
public class HunllefHitboxOverlay extends Overlay {
    private final Client client;
    private final GauntletBuddyConfig config;
    private final HunllefModule hunllefModule;
    private int HUNLLEF_FILL_OPACITY;
    private final long TORNADO_DURATION = 12000;
    private final int TIMER_RADIUS = 15;

    @Inject
    public HunllefHitboxOverlay(final Client client, final GauntletBuddyConfig config,
                                final HunllefModule hunllefModule)
    {
        this.client = client;
        this.config = config;
        this.hunllefModule = hunllefModule;

        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.UNDER_WIDGETS);
    }

    @Override
    public Dimension render(final Graphics2D graphics) {
        HUNLLEF_FILL_OPACITY = config.hunllefFillOpacity();
        renderTornadoes(graphics);
        renderHunleffBox(graphics);
        return null;
    }

    private void renderHunleffBox(final Graphics2D graphics) {
        final NPC hunllef = hunllefModule.getHunllef();

        if (hunllef == null || hunllef.isDead() || !config.hunllefTilesHighlight()) return;

        final Polygon tiles = hunllef.getCanvasTilePoly();

        if (tiles == null) return;

        Color outlineColor = config.hunllefHighlightColor();
        Color fillColor = new Color(outlineColor.getRed(), outlineColor.getGreen(), outlineColor.getBlue(), HUNLLEF_FILL_OPACITY);

        OverlayUtil.renderPolygon(graphics, tiles, outlineColor, fillColor, new BasicStroke(config.hunllefHighlightWidth()));
    }

    private void renderTornadoes(final Graphics2D graphics) {
        if (config.tornadoHighlightType() == TornadoHighlightType.OFF || hunllefModule.getTornadoList().isEmpty()) return;

        final boolean tileMode = config.tornadoHighlightType() == TornadoHighlightType.TRUE_TILE;
        final boolean showTimer = config.tornadoTimer();

        final Color tornadoHighlightColor = config.tornadoHighlightColor();
        final Color tornadoFillColor = new Color(tornadoHighlightColor.getRed(), tornadoHighlightColor.getGreen(), tornadoHighlightColor.getBlue(), HUNLLEF_FILL_OPACITY);
        final Color tornadoTimerColor = config.tornadoTimerColor();
        final Color timerFillColor = new Color(tornadoTimerColor.getRed(), tornadoTimerColor.getGreen(), tornadoTimerColor.getBlue(), HUNLLEF_FILL_OPACITY);
        final BasicStroke tornadoStroke = new BasicStroke(config.tornadoOutlineWidth());

        final int worldPlane = client.getLocalPlayer().getWorldLocation().getPlane();

        double timerAngle = 0;

        if (config.tornadoTimer()) {
            timerAngle = 360 * Math.max(0, 1.0 - (double) (System.currentTimeMillis() - hunllefModule.getTornadoSpawned()) / TORNADO_DURATION);
        }

        for (final NPC tornado : hunllefModule.getTornadoList())
        {
            Polygon outline;
            LocalPoint tornadoLocation;
            final int size = tornado.getComposition().getSize();

            if (tileMode)
            {
                final WorldPoint worldLocation = tornado.getWorldLocation();
                if (worldLocation == null) continue;

                // Using this deprecated method is the best way to get the true tile
                tornadoLocation = LocalPoint.fromWorld(client, worldLocation);
                if (tornadoLocation == null) continue;

                outline = Perspective.getCanvasTilePoly(client, tornadoLocation);
                if (outline == null) continue;

            } else {
                tornadoLocation = tornado.getLocalLocation();
                if (tornadoLocation == null) continue;

                outline = Perspective.getCanvasTilePoly(client, tornadoLocation);
            }

            OverlayUtil.renderPolygon(graphics, outline, tornadoHighlightColor, tornadoFillColor, tornadoStroke);
            //graphics.setColor(Color.BLACK);
            //graphics.draw(outline.getBounds());
            //graphics.setColor(Color.BLUE);
            //graphics.draw(outline);

            if (!showTimer) continue;

            final Point tornadoPoint = Perspective.localToCanvas(client, tornadoLocation, worldPlane);
            int radius = 4;
            graphics.fillOval(tornadoPoint.getX() - radius, tornadoPoint.getY() - radius, radius * 2, radius * 2);

            if (tornadoPoint == null) continue;

            Shape timerArc = getTornadoTimer(timerAngle, tornadoPoint, outline);
            OverlayUtil.renderPolygon(graphics, timerArc, tornadoTimerColor, timerFillColor, tornadoStroke);
        }
    }

    private Shape getTornadoTimer(double angle, Point drawLocation, Polygon outline) {
        Point sw = new Point(outline.xpoints[0], outline.ypoints[0]);
        Point se = new Point(outline.xpoints[1], outline.ypoints[1]);
        Point nw = new Point(outline.xpoints[3], outline.ypoints[3]);

        double height = Math.max(Math.abs(sw.getY() - nw.getY()), Math.abs(sw.getY() - se.getY()));
        double width = Math.max(Math.abs(sw.getX() - se.getX()), Math.abs(sw.getX() - nw.getX()));

        height *= .8;
        width *= .8;

        double offsetY = height / 2;
        double offsetX = width / 2;

        return new Arc2D.Double(
                drawLocation.getX() - offsetX, drawLocation.getY() - offsetY,
                width, height,
                90, -angle, Arc2D.PIE
        );
    }
}
