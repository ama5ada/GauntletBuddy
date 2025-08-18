package org.gauntletbuddy.config;

import net.runelite.client.config.*;
import org.gauntletbuddy.config.types.*;

import java.awt.*;


@ConfigGroup("Gauntlet Buddy")
public interface GauntletBuddyConfig extends Config
{
	/**
	 * Item Tracking Config Section, holds config values related to whether or not to track items
	 */
	@ConfigSection(
			name = "Item Tracking",
			description = "Settings for items to gather with manual input totals",
			position = 1,
			closedByDefault = true
	)
	String itemTracking = "itemTracking";

	@ConfigItem(
			keyName = "trackingMode",
			name = "Tracking Mode",
			description = "Select mode for tracking items, counting up to or down from goal",
			section = itemTracking,
			position = 1
	)
	default TrackingModeType itemTrackingMode() {
		return TrackingModeType.COUNTDOWN;
	}

	@ConfigItem(
			keyName = "specificationMode",
			name = "Specification Mode",
			description = "Select manual specification of resources to gather or calculator version",
			section = itemTracking,
			position = 2
	)
	default SpecificationModeType itemTrackerSpecificationMode() {
		return SpecificationModeType.MANUAL;
	}

	@ConfigItem(
			keyName = "hideCompleted",
			name = "Hide Completed Items",
			description = "Toggle for hiding item counters for items that you don't need more of",
			section = itemTracking,
			position = 3
	)
	default boolean hideCompleted() { return true; }

	/**
	 * Item Tracking manual input section, holds fields to specify item goals manually
	 */
	@ConfigSection(
			name = "Manual Item Goals",
			description = "Section for manually inputting item goals",
			position = 2,
			closedByDefault = true
	)
	String itemsManual = "itemsManual";

	@ConfigItem(
			keyName = "specifiedCrystalOre",
			name = "Crystal Ore",
			description = "Manually specified amount of Crystal Ore to be gathered",
			section = itemsManual,
			position = 1
	)
	default int specifiedCrystalOre() { return 3; }

	@ConfigItem(
			keyName = "specifiedPhrenBark",
			name = "Phren Bark",
			description = "Manually specified amount of Phren Bark to be gathered",
			section = itemsManual,
			position = 2
	)
	default int specifiedPhrenBark() { return 3; }

	@ConfigItem(
			keyName = "specifiedLinumTirinium",
			name = "Linum Tirinium",
			description = "Manually specified amount of Linum Tirinium to be gathered",
			section = itemsManual,
			position = 3
	)
	default int specifiedLinumTirinium() { return 3; }

	@ConfigItem(
			keyName = "specifiedGrymLeaf",
			name = "Grym Leaf",
			description = "Manually specified amount of Grym Leaf to be gathered",
			section = itemsManual,
			position = 4
	)
	default int specifiedGrymLeaf() { return 3; }

	@ConfigItem(
			keyName = "specifiedWeaponFrames",
			name = "Weapon Frames",
			description = "Manually specified amount of Weapon Frames to be gathered",
			section = itemsManual,
			position = 5
	)
	default int specifiedWeaponFrames() { return 2; }

	@ConfigItem(
			keyName = "specifiedCrystalShards",
			name = "Crystal Shards",
			description = "Manually specified amount of Crystal Shards to be gathered",
			section = itemsManual,
			position = 6
	)
	default int specifiedCrystalShards() { return 350; }

	@ConfigItem(
			keyName = "specifiedPaddlefish",
			name = "Paddlefish",
			description = "Manually specified amount of Paddlefish to be gathered",
			section = itemsManual,
			position = 7
	)
	default int specifiedPaddlefish() { return 24; }

	@ConfigItem(
			keyName = "bowString",
			name = "Bow String",
			description = "Manually specify if you're looking for a Bow String",
			section = itemsManual,
			position = 8
	)
	default boolean specifiedBowString() { return false; }

	@ConfigItem(
			keyName = "orb",
			name = "Orb",
			description = "Manually specify if you're looking for an Orb",
			section = itemsManual,
			position = 9
	)
	default boolean specifiedOrb() { return false; }

	@ConfigItem(
			keyName = "spike",
			name = "Spike",
			description = "Manually specify if you're looking for a spike",
			section = itemsManual,
			position = 10
	)
	default boolean specifiedSpike() { return false; }

	/**
	 * Item Calculator Config Section, holds config values for calculating items that need to be tracked
	 */
	@ConfigSection(
			name = "Calculated Item Goals",
			description = "Settings for calculating items to gather based on gear choices",
			position = 3,
			closedByDefault = true
	)
	String itemsCalculator = "itemCalculator";

	@ConfigItem(
			keyName = "calculatorHelm",
			name = "Helm",
			description = "Tier of helm you want to create",
			section = itemsCalculator,
			position = 1
	)
	default GearTierType calculatorHelmetTier() {
		return GearTierType.T1;
	}

	@ConfigItem(
			keyName = "calculatorBody",
			name = "Body",
			description = "Tier of body you want to create",
			section = itemsCalculator,
			position = 2
	)
	default GearTierType calculatorBodyTier() {
		return GearTierType.T1;
	}

	@ConfigItem(
			keyName = "calculatorLegs",
			name = "Legs",
			description = "Tier of legs you want to create",
			section = itemsCalculator,
			position = 3
	)
	default GearTierType calculatorLegsTier() {
		return GearTierType.T1;
	}

	@ConfigItem(
			keyName = "calculatorBow",
			name = "Bow",
			description = "Tier of bow you want to create",
			section = itemsCalculator,
			position = 4
	)
	default GearTierType calculatorBowTier() {
		return GearTierType.T3;
	}

	@ConfigItem(
			keyName = "calculatorStaff",
			name = "Staff",
			description = "Tier of staff you want to create",
			section = itemsCalculator,
			position = 5
	)
	default GearTierType calculatorStaffTier() {
		return GearTierType.T2;
	}

	@ConfigItem(
			keyName = "calculatorHalberd",
			name = "Halberd",
			description = "Tier of halberd you want to create",
			section = itemsCalculator,
			position = 6
	)
	default GearTierType calculatorHalberdTier() {
		return GearTierType.None;
	}

	@ConfigItem(
			keyName = "calculatorPotions",
			name = "Enigol Potions",
			description = "Number of Enigol potions you want to create",
			section = itemsCalculator,
			position = 7
	)
	default int calculatorPotions() { return 3; }

	@ConfigItem(
			keyName = "calculatorTeleportCrystals",
			name = "Teleport Crystals",
			description = "Number of extra Teleport Crystals you want to create",
			section = itemsCalculator,
			position = 8
	)
	default int calculatorTeleportCrystals() { return 1; }

	@ConfigItem(
			keyName = "calculatorPaddlefish",
			name = "Paddlefish",
			description = "Number of Paddlefish you want to gather",
			section = itemsCalculator,
			position = 9
	)
	default int calculatorPaddlefish() { return 24; }

	/**
	 * Resource Highlighting Config Section, holds config values for highlighting different resources in The Gauntlet
	 */
	@ConfigSection(
			name = "Resource Highlights",
			description = "Settings for highlighting resources in The Gauntlet",
			position = 4,
			closedByDefault = true
	)
	String resourceHighlights = "resourceHighlights";

	/**
	 * NPC Highlighting Config Section, holds config values for highlighting different NPCs in The Gauntlet
	 */
	@ConfigSection(
			name = "NPC Highlights",
			description = "Settings for highlighting NPCs in The Gauntlet",
			position = 5,
			closedByDefault = true
	)
	String npcHighlights = "npcHighlights";

	/**
	 * Hunllef Config Section, holds config values for Hunllef fight utilities
	 */
	@ConfigSection(
			name = "Hunllef Utilities",
			description = "Settings for Hunllef fight utilities",
			position = 6,
			closedByDefault = true
	)
	String hunllefUtilities = "hunllefUtilities";

	@ConfigItem(
			keyName = "prayerHighlightMode",
			name = "Prayer Highlight Mode",
			description = "Different options for displaying the prayer highlight outline",
			position = 1,
			section = hunllefUtilities
	)
	default PrayerHighlightModeType hunllefPrayerHighlightMode() {
		return PrayerHighlightModeType.ALWAYS;
	}

	@Alpha
	@ConfigItem(
			keyName = "prayerHighlightColor",
			name = "Prayer Highlight Color",
			description = "Change color of prayer highlight box",
			position = 2,
			section = hunllefUtilities
	)
	default Color prayerHighlightColor()
	{
		return new Color(0, 255, 100, 50);
	}

	@Alpha
	@ConfigItem(
			keyName = "prayerMismatchHighlightColor",
			name = "Prayer MismatchColor",
			description = "Change the color of your prayer highlight when your current overhead is wrong",
			position = 3,
			section = hunllefUtilities
	)
	default Color prayerMismatchHighlightColor()
	{
		return new Color(255, 200, 0, 50);
	}

	@ConfigItem(
			keyName = "hunllefPrayerSwapAlert",
			name = "Hunllef Prayer Swap Alert",
			description = "Make your screen flash when your next attack will change the Hunllef active prayer",
			position = 4,
			section = hunllefUtilities
	)
	default boolean hunllefPrayerSwapAlert() { return true; }

	@ConfigItem(
			keyName = "hunllefHitTrackerMode",
			name = "Hunllef Hit Tracker",
			description = "Option to count hits off prayer on Hunllef",
			position = 5,
			section = hunllefUtilities
	)
	default TrackingModeType hunllefHitTrackerMode() { return TrackingModeType.COUNTDOWN; }

	@ConfigItem(
			keyName = "tornadoHighlightMode",
			name = "Tornado Highlight Mode",
			description = "Mode for highlighting Hunllef tornadoes",
			position = 6,
			section = hunllefUtilities
	)
	default TornadoHighlightType tornadoHighlightType() { return TornadoHighlightType.TRUE_TILE; }

	@Range(max = 3)
	@ConfigItem(
			keyName = "tornadoHighlightWidth",
			name = "Tornado Highlight Width",
			description = "Width of Tornado outline stroke",
			position = 7,
			section = hunllefUtilities
	)
	@Units(Units.PIXELS)
	default int tornadoOutlineWidth() { return 1; }

	@Alpha
	@ConfigItem(
			keyName = "tornadoHighlightColor",
			name = "Tornado Highlight Color",
			description = "Color for the tornado highlight",
			position = 8,
			section = hunllefUtilities
	)
	default Color tornadoHighlightColor() { return new Color(255, 255, 0, 100); }

	@ConfigItem(
			keyName = "tornadoTimer",
			name = "Tornado Timer",
			description = "Options for showing the time left on Hunllef Tornadoes",
			position = 9,
			section = hunllefUtilities
	)
	default TornadoTimerType tornadoTimerType() { return TornadoTimerType.TIMER; }

	@Alpha
	@ConfigItem(
			keyName = "tornadoTimerColor",
			name = "Tornado Timer Color",
			description = "Color of the tornado timer",
			position = 10,
			section = hunllefUtilities
	)
	default Color tornadoTimerColor() { return new Color(255, 50, 50, 150); }

    @ConfigItem(
            keyName = "showTornadoPaths",
            name = "Show Tornado Paths",
            description = "Show tiles that tornadoes can potentially reach",
            position = 11,
            section = hunllefUtilities
    )
    default boolean showTornadoPaths() { return false; }

    @Alpha
    @ConfigItem(
            keyName = "tornadoPathColor",
            name = "Tornado Path Color",
            description = "Color of the area the tornadoes can potentially reach",
            position = 12,
            section = hunllefUtilities
    )
    default Color tornadoPathColor() { return new Color(255, 135, 50, 100); }

    @ConfigItem(
            keyName = "hunllefTilesHighlight",
            name = "Hunllef Tiles Highlight",
            description = "Highlight the tiles that the hunllef occupies",
            position = 13,
            section = hunllefUtilities
    )
    default boolean hunllefTilesHighlight() { return true; }

	@Range(max = 3)
	@ConfigItem(
			keyName = "hunllefHighlightWidth",
			name = "Hunllef Tiles Highlight Width",
			description = "Width of Hunllef tile outline stroke",
			position = 14,
			section = hunllefUtilities
	)
	@Units(Units.PIXELS)
	default int hunllefHighlightWidth() { return 1; }

	@Alpha
	@ConfigItem(
			keyName = "hunllefHighlightColor",
			name = "Hunllef Highlight Color",
			description = "Color of the tiles Hunllef stands on",
			position = 15,
			section = hunllefUtilities
	)
	default Color hunllefHighlightColor() { return new Color(255, 255, 255, 150); }

	@Range(max = 255)
	@ConfigItem(
			keyName = "fillOpacity",
			name = "Fill Opacity",
			description = "Fill opacity for highlights in hunllef fights",
			position = 16,
			section = hunllefUtilities
	)
	default int hunllefFillOpacity() { return 50;}

	/**
	 * Misc Config Section, holds config values for uncategorized utilities
	 */
	@ConfigSection(
			name = "Misc Utilities",
			description = "Settings for miscellaneous Gauntlet utilities",
			position = 7,
			closedByDefault = true
	)
	String miscUtilities = "miscUtilities";

	@ConfigItem(
			keyName = "debugList",
			name = "Debug List",
			description = "Show the list of private variables used by the plugin in the upper left",
			position = 1,
			section = miscUtilities
	)
	default boolean debugList() { return false; }

	@ConfigItem(
			keyName = "hideMissingItems",
			name = "Hide Missing Items",
			description = "Hide items not in the inventory from the debug list",
			position = 2,
			section = miscUtilities
	)
	default boolean hideMissingItems() { return true; }

	@ConfigItem(
			keyName = "gauntletTimer",
			name = "Gauntlet Timer",
			description = "Display a timer that counts up from entering The Gauntlet to track total kill time on screen",
			position = 3,
			section = miscUtilities
	)
	default boolean gauntletTimer() { return true; }

    @ConfigItem(
            keyName = "chatTimer",
            name = "Chat Timer",
            description = "Shows a message in the chat box summarizing your last Gauntlet run",
            position = 4,
            section = miscUtilities
    )
    default boolean chatTimer() { return true; }

	@ConfigItem(
			keyName = "mustCookFish",
			name = "Must Cook Fish",
			description = "Remove options for entering the boss room if you have raw fish in your inventory",
			position = 5,
			section = miscUtilities
	)
	default boolean mustCookFish() { return true; }

	@ConfigItem(
			keyName = "highlightStations",
			name = "Highlight Stations",
			description = "Outline starting room crafting stations to easily tell where they are from outside the room",
			position = 6,
			section = miscUtilities
	)
	default boolean highlightStations() { return false; }

	@Alpha
	@ConfigItem(
			keyName = "highlightStationsColor",
			name = "Station Color",
			description = "Color for the outline of starting room stations",
			position = 7,
			section = miscUtilities
	)
	default Color highlightStationsColor()
	{
		return new Color(255, 0, 255, 100);
	}

	@Range( max = 4 )
	@ConfigItem(
			keyName = "highlightStationsWidth",
			name = "Outline Width",
			description = "Width for the outline of starting room stations",
			position = 8,
			section = miscUtilities
	)
	@Units(Units.PIXELS)
	default int stationHighlightWidth()
	{
		return 2;
	}

    @Range ( max = 6 )
    @ConfigItem(
            keyName = "textOffset",
            name = "Text Offset",
            description = "Temporary counter text offset",
            position = 9,
            section = miscUtilities
    )
    default int textOffset() { return 2; }

}
