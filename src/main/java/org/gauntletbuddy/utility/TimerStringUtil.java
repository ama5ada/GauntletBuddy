package org.gauntletbuddy.utility;

public class TimerStringUtil {
    public static String formatTimerString(final long elapsed) {
        final long seconds = elapsed % 60;
        final long minutes = (elapsed % 3600) / 60;

        return String.format("%01d:%02d", minutes, seconds);
    }
}
